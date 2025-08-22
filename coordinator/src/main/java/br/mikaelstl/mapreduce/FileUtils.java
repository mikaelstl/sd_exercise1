package br.mikaelstl.mapreduce;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtils {
  private final Logger logger = LoggerFactory.getLogger("FileUtils");

  public void split(String filename) {
    File filepath = Paths.get("data", filename).toFile();
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
        File chunk = Paths.get("data", "chunks", chunkName).toFile();

        try (
          BufferedWriter writer = new BufferedWriter(new FileWriter(chunk))
        ) {
          long linesToWrite = chunkLines + (i == parts - 1 ? rest : 0);

          for (int l = 0; l < linesToWrite; l++) {
            String line = reader.readLine();
            if (line == null) break;

            writer.write(line);
            writer.newLine();
          }
        }
      }

      reader.close();
    } catch (IOException e) {
      logger.error("Error to generate chunks: ", e);
    }
  }
}
