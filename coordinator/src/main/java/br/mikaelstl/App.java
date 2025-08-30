package br.mikaelstl;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public class App 
{
    private static final Logger logger = LoggerFactory.getLogger("COORDINATOR");
    
    public static void main( String[] args )
    {
        FileUtils fileUtils = new FileUtils();

        String host = System.getenv().getOrDefault("REDIS_HOST", "localhost");

        
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
        }
        
        try (Jedis jedis = new Jedis(host, 6379)) {
            jedis.del(Enviroment.TASKS_QUEUE);

            for (File chunk : chunks) {
                jedis.rpush(Enviroment.TASKS_QUEUE, chunk.getName());
            }

            logger.info(chunks.length + " chunks enfileirados em " + Enviroment.TASKS_QUEUE);

            
            jedis.subscribe(new JedisPubSub() {
                int mappersDone = 0;

                @Override
                public void onMessage(String channel, String message) {
                    logger.info("Mapper :"+message+" finished");
                    
                    mappersDone+=1;
                    
                    if (mappersDone == Enviroment.MAPPERS_AMOUNT) {
                        shuffle();
                    }

                }
            }, Enviroment.MAPPER_DONE_FLAG);
        } catch (Exception e) {
            logger.error("ERROR to send message to mapper", e);
        }
    }

    static void shuffle() {
        logger.info("Entrando na fase shuffle");
    }
}
