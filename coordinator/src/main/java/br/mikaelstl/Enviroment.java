package br.mikaelstl;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Enviroment {
  static final Path SHARED_DIR = Paths.get("/app", "shared", "data");
  static final String TASKS_QUEUE = "map_tasks_queue";
  static final String MAPPER_DONE_FLAG = "mappers_done";

  static final String REDIS_HOST = System.getenv().getOrDefault("REDIS_HOST", "localhost");
  static final int REDIS_PORT = Integer.parseInt(System.getenv().getOrDefault("REDIS_PORT", "6379"));

  static final int MAPPERS_AMOUNT = Integer.parseInt(System.getenv().getOrDefault("MAPPERS_AMOUNT", "5"));
  static final int REDUCERS_AMOUNT = Integer.parseInt(System.getenv().getOrDefault("REDUCERS_AMOUNT", "2"));
}
