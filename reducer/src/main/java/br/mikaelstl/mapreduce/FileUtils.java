package br.mikaelstl.mapreduce;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FileUtils {
  private HashMap<String, Integer> words = new HashMap<>();

  private final Logger logger = LoggerFactory.getLogger("FileUtils");

  public void process(File file) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      
      TypeReference<HashMap<String, List<Integer>>> ref = new TypeReference<>(){};
      
      HashMap<String, List<Integer>> jsonFile = mapper.readValue(file, ref);
    
      jsonFile.forEach((key, value) -> {
        words.merge(key, value.stream().mapToInt(Integer::intValue).sum(), Integer::sum);
      });
    } catch (IOException e) {
      logger.error("ERRO ao gerar resultados do reducer:"+Enviroment.REDUCER_ID+": ", e);
    }
  }
  
  public void write(String output) {
    File file = Enviroment.SHARED_DIR.resolve("routput").resolve(output).toFile();

    if (file.exists() && file.length() > 0) {
      return;
    }

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
      words.forEach((key, value) -> {
        try {
          writer.write(key+": "+value);
          writer.newLine();
        } catch (IOException e) {
          logger.error("ERRO ao escrever novo arquivo: ", e);
        }
      });
    } catch (IOException e) {
      logger.error("ERRO ao escrever novo arquivo: ", e);
    }
  }

  public boolean haveWords() {
    return !words.isEmpty();
  }
}
