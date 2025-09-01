package br.mikaelstl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FileUtils {
  List<HashMap<String, List<Integer>>> data = new ArrayList<HashMap<String,List<Integer>>>();

  private final Logger logger = LoggerFactory.getLogger("FileUtils");

  public FileUtils() {
    mkdirs();
    data.add(new HashMap<>());
    data.add(new HashMap<>());
  }

  public void split(String filename) {
    File filepath = Enviroment.SHARED_DIR.resolve(filename).toFile();
    logger.info(filepath.getAbsolutePath());
    int parts = 10;

    try {
      BufferedReader reader = new BufferedReader(new FileReader(filepath));
   
      long lines = reader.lines().count();
      long chunkLines = lines / parts;
      long rest = lines % parts;

      reader.close();
      reader = new BufferedReader(new FileReader(filepath));

      for (int i = 0; i < parts; i++) {
        String chunkName = "chunk"+i+".txt";
        Path chunkDir = Enviroment.SHARED_DIR.resolve("chunks");
        File chunk = chunkDir.resolve(chunkName).toFile();

        try (
          BufferedWriter writer = new BufferedWriter(new FileWriter(chunk))
        ) {
          long linesToWrite = chunkLines + (i == parts - 1 ? rest : 0);

          for (int l = 0; l < linesToWrite; l++) {
            String line = reader.readLine();
            if (line == null) break;

            writer.write(line.replaceAll("[^\\p{L}\\p{Nd}]", " "));
            writer.newLine();
          }
        }
      }

      reader.close();
    } catch (IOException e) {
      logger.error("ERRO ao gerar chunks: ", e);
      System.exit(1);
    }
  }

  public void process(int reducerId, String key, List<Integer> values) {
    var words = data.get(reducerId);

    words.computeIfAbsent(key, k -> values).addAll(values);
  }

  private void mkdirs() {
    File dir = Enviroment.SHARED_DIR.toFile();

    logger.info("Directory exists: "+dir.exists());

    if (!dir.exists()) {
      dir.mkdirs();
    }
  }

  public void write() {
    ObjectMapper mapper = new ObjectMapper();

    data.stream().forEach(
      (words) -> {
        int reducerId = data.indexOf(words);

        String output = "reducer_"+reducerId+"_input.json";

        File file = Enviroment.SHARED_DIR.resolve("rinputs").resolve(output).toFile();
      
        try {
          if (!file.exists()) {
            file.createNewFile();
          }

          mapper.writeValue(file, words);
        } catch (IOException e) {
          logger.error("ERROR ao gerar JSON: ", e);
        }
      }
    );
  }
}
