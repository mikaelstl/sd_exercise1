package br.mikaelstl.mapreduce;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;

public class Reducer 
{
    public static void main( String[] args ) throws InterruptedException
    {
        final String channel = "reducer:"+Enviroment.REDUCER_ID;

        final Logger logger = LoggerFactory.getLogger("REDUCER"+Enviroment.REDUCER_ID);

        FileUtils fileUtils = new FileUtils();
        
        try (Jedis jedis = new Jedis(Enviroment.REDIS_HOST, Enviroment.REDIS_PORT);) {
            logger.info(channel);

            int init = 0;

            while (init == 0) {
                try {
                    init = Integer.parseInt(jedis.get("init_reducers"));
                } catch (Exception e) {
                    init = 0;
                }

                Thread.sleep(1000);
            }

            String input = "reducer_"+Enviroment.REDUCER_ID+"_input.json";
            File reducerInput = Enviroment.SHARED_DIR.resolve("rinputs").resolve(input).toFile();
        
            if (!reducerInput.exists()) {
                logger.error("ERRO nenhum arquivo registrado.");
                System.exit(1);
            }

            fileUtils.process(reducerInput);
        }
    }
}
