package br.mikaelstl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class FileUtils {
  private final Logger logger = LoggerFactory.getLogger("FileUtils");

  public FileUtils() {
    mkdirs();
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

  public void process(File file, String output) {
    try {
      BufferedReader reader = new BufferedReader(new FileReader(file));

      long lines = reader.lines().count();

      reader.close();
      reader = new BufferedReader(new FileReader(file));

      File chunk = Paths.get("data", "intermediate", output).toFile();
      for (int i = 0; i < lines; i++) {
        try (
          BufferedWriter writer = new BufferedWriter(new FileWriter(chunk))
        ) {
          String line = reader.readLine();
          if (line == null) break;
          
          writer.write(line+", 1");
          writer.newLine();
        }
      }

      reader.close();
    } catch (IOException e) {
      logger.error("Error to generate chunks: ", e);
    }
  }

  private void mkdirs() {
    File dir = Enviroment.SHARED_DIR.toFile();

    logger.info("Directory exists: "+dir.exists());

    if (!dir.exists()) {
      dir.mkdirs();
    }
  }

  public void write(String output, String key, List<Integer> values) {
    File file = Enviroment.SHARED_DIR.resolve("rinputs").resolve(output).toFile();

    if (!file.exists()) {
      try {
        file.createNewFile();      
      } catch (IOException e) {
        logger.error("ERRO ao criar arquivo: ", e);
      }
    }

    HashMap<String, List<Integer>> word = new HashMap<>();
    word.put(key, values);

    try {
      ObjectMapper mapper = new ObjectMapper();
    
      mapper.writeValue(file, word);
      logger.info("Arquivo "+file.getName()+" gerado com sucesso.");
    } catch (IOException e) {
      logger.error("ERROR to write JSON: ", e);
    }
  }
}
