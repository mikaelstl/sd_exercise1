package br.mikaelstl;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        String host = System.getenv().getOrDefault("REDIS_HOST", "localhost");
        final Logger logger = LoggerFactory.getLogger("COORDINATOR");
        
        FileUtils fileUtils = new FileUtils();
        fileUtils.split("input_mapreduce.txt");

        logger.info("Chunks generated");
        
        Path chunksDir = Paths.get("data", "chunks");
        
        try (Jedis jedis = new Jedis(host, 6379)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(chunksDir)) {
                for (Path chunk : stream) {
                    File file = chunk.toFile();
                    String filename = file.getName().replace(".txt", "");

                    int fileNumber = Integer.parseInt(filename.replace("chunk", ""));
                    String targetMapper = "mapper:"+(fileNumber%5);

                    if (file.getAbsolutePath().contains(".txt")) {
                        jedis.publish(targetMapper, filename);
                    }
                }
            } catch (IOException err) {
                logger.info("Error to read directory: ", err);
            }
        } catch (Exception e) {
            logger.error("ERROR to send message to mapper", e);
        }
    }

    void shuffle() {
        // TO-DO
    }
}
