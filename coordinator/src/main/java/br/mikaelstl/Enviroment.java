package br.mikaelstl;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Enviroment {
  static final Path SHARED_DIR = Paths.get("/app", "shared", "data");
  static final String TASKS_QUEUE = "map_tasks_queue";
}
