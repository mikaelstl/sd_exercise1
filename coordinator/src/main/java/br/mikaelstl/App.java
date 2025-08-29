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
        int mappersAmmount = Integer.parseInt(System.getenv().getOrDefault("MAPPERS_AMOUNT", "5"));
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
            String mappersReady = jedis.get("mappers_ready");

            while (
                mappersReady == null
                ||
                Integer.parseInt(mappersReady) < mappersAmmount
            ) {
                Thread.sleep(1000);
            }

            for (File chunk : chunks) {
                String filename = chunk.getName().replace(".txt", "");

                int fileNumber = Integer.parseInt(filename.replace("chunk", ""));
                logger.info("chunk number >>>>> "+fileNumber);
                
                String targetMapper = "mapper:"+(fileNumber%5);
                
                jedis.publish(targetMapper, filename);
            }

            jedis.del("mappers_ready");
        } catch (Exception e) {
            logger.error("ERROR to send message to mapper", e);
        }
    }

    void shuffle() {
        // TO-DO
    }
}
