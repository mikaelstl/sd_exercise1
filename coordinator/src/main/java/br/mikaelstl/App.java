package br.mikaelstl;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;

public class App 
{
    public static void main( String[] args )
    {
        FileUtils fileUtils = new FileUtils();

        String host = System.getenv().getOrDefault("REDIS_HOST", "localhost");

        final Logger logger = LoggerFactory.getLogger("COORDINATOR");
        
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
            // String mappersReady = jedis.get("mappers_ready");

            for (File chunk : chunks) {
                jedis.rpush(Enviroment.TASKS_QUEUE, chunk.getName());
            }

            logger.info(chunks.length + " chunks enfileirados em " + Enviroment.TASKS_QUEUE);
            
            jedis.del("mappers_ready");
        } catch (Exception e) {
            logger.error("ERROR to send message to mapper", e);
        }
    }

    void shuffle() {
        // TO-DO
    }
}
