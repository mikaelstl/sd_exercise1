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
    public static void main( String[] args )
    {
        String host = System.getenv().getOrDefault("REDIS_HOST", "localhost");
        int port = Integer.parseInt(System.getenv().getOrDefault("REDIS_PORT", "6379"));
        int mappersAmount = Integer.parseInt(System.getenv().getOrDefault("MAPPERS_AMOUNT", "5"));
        
        final Logger logger = LoggerFactory.getLogger("COORDINATOR");
        
        try (Jedis jedis = new Jedis(host, port);) {
            for (int i = 0; i < mappersAmount; i++) {
                String channel = "mapper"+i+"_stasks";
                jedis.publish(channel, "Executando tarefas");
                logger.info("index >>>>> "+i);
            }
        }
    }
}
