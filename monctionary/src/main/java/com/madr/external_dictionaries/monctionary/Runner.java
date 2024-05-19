package com.madr.external_dictionaries.monctionary;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.madr.external_dictionaries.monctionary.repository.WordRepository;
import com.madr.external_dictionaries.mongomodel.model.Word;
import com.madr.external_dictionaries.mongomodel.protobuf.Common;
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
    private final DictionariesConfiguration configuration;

    @Override
    public void run(ApplicationArguments args) {
        if (configuration.getDictionaries() != null) {
            for (var dictionary : configuration.getDictionaries()) {
                fillDb(dictionary.dictionary(), dictionary.wiktionaryLanguage(), dictionary.wordsLanguage());
            }
        }
        terminateApplication();
    }

    private void fillDb(
        File file,
        Common.SupportedLanguage wiktionaryLanguage,
        Common.SupportedLanguage wordsLanguage
    ) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            ObjectMapper objectMapper =
                new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            while ((line = br.readLine()) != null) {
                Word word = objectMapper.readValue(line, Word.class);
                word.trimSounds();
                word.setWordLanguage(wordsLanguage);
                word.setWiktionaryLanguage(wiktionaryLanguage);
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
