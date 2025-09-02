package br.mikaelstl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class Coordinator 
{
    private static final Logger logger = LoggerFactory.getLogger("COORDINATOR");
    
    private static final FileUtils fileUtils = new FileUtils();

    private static Jedis jedis = new Jedis(Enviroment.REDIS_HOST, Enviroment.REDIS_PORT);

    public static void main( String[] args )
    {

        Path chunksDir = Enviroment.SHARED_DIR.resolve("chunks");
        
        FilenameFilter txtFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".txt");
            }
        };

        File[] chunks = chunksDir.toFile().listFiles(txtFilter);
        if (chunks.length == 0) {
            fileUtils.split("input_mapreduce.txt");
            logger.info("Chunks generated");
            chunks = chunksDir.toFile().listFiles(txtFilter);
        }
        
        try {
            jedis.incr("init_mappers");
            jedis.del(Enviroment.TASKS_QUEUE);

            for (File chunk : chunks) {
                jedis.rpush(Enviroment.TASKS_QUEUE, chunk.getName());
            }

            logger.info(chunks.length + " chunks enfileirados em " + Enviroment.TASKS_QUEUE);

            int finishedMappers = 0;
            while (finishedMappers < Enviroment.MAPPERS_AMOUNT) {
                try {
                    finishedMappers = Integer.parseInt(jedis.get(Enviroment.MAPPER_DONE_FLAG));
                } catch (NumberFormatException e) {
                    finishedMappers = 0;
                } catch (JedisConnectionException jedisConnectionException) {
                    logger.error("ERRO de conexão com o Redis: ", jedisConnectionException);
                    logger.info("Tentando novamente.");
                    jedis = new Jedis(Enviroment.REDIS_HOST, Enviroment.REDIS_PORT);
                    finishedMappers = 0;
                }
                Thread.sleep(1000);
            }

            logger.info("Todos os mappers finalizaram. Iniciando fase de shuffle...");
            
            shuffle();

            int finishedReducers = 0;
            while (finishedReducers < Enviroment.REDUCERS_AMOUNT) {
                try {
                    finishedReducers = Integer.parseInt(jedis.get("reducer_finished"));
                } catch (NumberFormatException e) {
                    finishedReducers = 0;
                } catch (JedisConnectionException jedisConnectionException) {
                    logger.error("ERRO de conexão com o Redis: ", jedisConnectionException);
                    logger.info("Tentando novamente.");
                    jedis = new Jedis(Enviroment.REDIS_HOST, Enviroment.REDIS_PORT);
                    finishedReducers = 0;
                }
                Thread.sleep(1000);
            }

            joinResults();

        } catch (Exception e) {
            logger.error("ERRO ao enviar mensagem para mapper: ", e);
        } finally {
            jedis.close();
        }
    }

    static void shuffle() {
        Path intermediateDir = Enviroment.SHARED_DIR.resolve("intermediate");

        FilenameFilter txtFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".json");
            }
        };

        File[] intermediates = intermediateDir.toFile().listFiles(txtFilter);

        if (intermediates.length == 0) {
            logger.error("ERRO nenhum arquivo encontrado. Encerrando");
            System.exit(1);
        }

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            for (File file : intermediates) {
                TypeReference<HashMap<String, List<Integer>>> ref = new TypeReference<>(){};

                HashMap<String, List<Integer>> jsonFile = objectMapper.readValue(file, ref);

                jsonFile.forEach((key, value) -> {
                    int reducer = Math.abs(key.hashCode()) % Enviroment.REDUCERS_AMOUNT;

                    fileUtils.process(reducer, key, value);
                });
            }
            fileUtils.write();
            logger.info("Arquivos criados. Sinalizando para reducers.");
            
            jedis.incr("init_reducers");
        } catch (IOException e) {
            logger.error("ERRO ao ler arquivos JSON:", e);
        }
    }

    static void joinResults() {
        File result = Enviroment.SHARED_DIR.resolve("result").resolve("final_result.txt").toFile();

        File routputsDir = Enviroment.SHARED_DIR.resolve("routput").toFile();

        File[] outputs = routputsDir.listFiles();

        if (outputs.length == 0) {
            logger.error("ERRO nenhum arquivo retornado dos reducers. Encerrando...");
            System.exit(1);
        }

        try (
            BufferedWriter writer = new BufferedWriter(new FileWriter(result));
        ) {
            for (File file : outputs) {
                try (
                    BufferedReader reader = new BufferedReader(new FileReader(file));
                ) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        writer.write(line);
                        writer.newLine();
                    }

                } catch (Exception e) {
                    logger.error("ERRO ao ler arquivo "+file.getName()+": ", e);
                }
            }
            logger.info("Arquivo final gerado em "+result.getAbsolutePath());
            logger.info("Encerrando...");
        } catch (Exception e) {
            logger.error("ERRO ao gerar arquivo final.");
        }
    }
}
