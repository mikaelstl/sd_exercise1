package br.mikaelstl.mapreduce;

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
    public static void main( String[] args ) throws InterruptedException
    {
        String host = System.getenv().getOrDefault("REDIS_HOST", "localhost");
        int port = Integer.parseInt(System.getenv().getOrDefault("REDIS_PORT", "6379"));
        String mapperId = System.getenv().getOrDefault("MAPPER_ID", "0");
        
        final String channel = "mapper:"+mapperId;

        final Logger logger = LoggerFactory.getLogger("MAPPER"+mapperId);
        
        try (Jedis jedis = new Jedis(host, port);) {
            jedis.incr("mappers_ready");
            logger.info(channel + " is ready.");
            
            jedis.subscribe(new JedisPubSub() {
                @Override
                public void onMessage(String channel, String message) {
                    logger.info("Mapper " + mapperId + " recebeu tarefa: " + message);
                }
            }, channel);
        }
    }
}
