package com.madr.external_dictionaries.monctionary;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.madr.external_dictionaries.monctionary.model.Word;
import com.madr.external_dictionaries.monctionary.repository.WordRepository;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Runner implements ApplicationRunner {
    private final WordRepository wordRepository;

    @Override
    public void run(ApplicationArguments args) {
        fillDb();
        terminateApplication();
    }

    private void fillDb() {
        File enToEnFile = new File("./mongo-entrypoint/en_to_en.json");
        try (BufferedReader br = new BufferedReader(new FileReader(enToEnFile))) {
            String line;
            ObjectMapper objectMapper =
                new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            while ((line = br.readLine()) != null) {
                Word word = objectMapper.readValue(line, Word.class);
                wordRepository.save(word);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void terminateApplication() {
        System.exit(0);
    }
}
