package br.mikaelstl.mapreduce;

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

import com.fasterxml.jackson.databind.ObjectMapper;

public class FileUtils {
  private HashMap<String, List<Integer>> words = new HashMap<>();

  private final Logger logger = LoggerFactory.getLogger("FileUtils");

  public void process(File file) {
    try {
      BufferedReader reader = new BufferedReader(new FileReader(file));

      long lines = reader.lines().count();

      reader.close();
      reader = new BufferedReader(new FileReader(file));

      for (int i = 0; i < lines; i++) {  
        String line = reader.readLine();
        if (line == null) break;
        
        String[] words = line.split("\\s+");
        for (String w : words) {
          this.words.computeIfAbsent(w, k -> new ArrayList<>()).add(1);
        }
      }      
      reader.close();
    } catch (IOException e) {
      logger.error("ERROR to generate intermediate files: ", e);
    }
  }
  
  public void write(String output) {
    File file = Enviroment.SHARED_DIR.resolve("intermediate").resolve(output).toFile();
    
    try {
      ObjectMapper mapper = new ObjectMapper();
    
      mapper.writeValue(file, words);
      logger.info("Arquivo "+file.getName()+" gerado com sucesso.");
    } catch (IOException e) {
      logger.error("ERROR to write JSON: ", e);
    }
  }
}
