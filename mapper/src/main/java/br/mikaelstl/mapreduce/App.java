package br.mikaelstl.mapreduce;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;

public class App 
{
    public static void main( String[] args ) throws InterruptedException
    {
        final String channel = "mapper:"+Enviroment.MAPPER_ID;

        final Logger logger = LoggerFactory.getLogger("MAPPER"+Enviroment.MAPPER_ID);
        
        FileUtils fileUtils = new FileUtils();

        try (Jedis jedis = new Jedis(Enviroment.REDIS_HOST, Enviroment.REDIS_PORT);) {
            logger.info("Mapper " + Enviroment.MAPPER_ID + " aguardando tarefas na fila...");
            
            while (true) {
                List<String> result = jedis.brpop(5, Enviroment.TASKS_QUEUE);

                if (result == null) {
                    logger.info("Fila vazia " + channel + " encerrando.");
                    break;
                }

                String filename = result.get(1);
                File chunk = Enviroment.SHARED_DIR.resolve("chunks").resolve(filename).toFile();

                logger.info("Mapper " + Enviroment.MAPPER_ID + " recebeu tarefa: " + filename);
                logger.info("Processando...");
            
                fileUtils.process(chunk);
            }

            fileUtils.write("intermediate"+Enviroment.MAPPER_ID+".json");
        
            jedis.publish(Enviroment.MAPPER_DONE_FLAG, Enviroment.MAPPER_ID);
        }
    }
}
