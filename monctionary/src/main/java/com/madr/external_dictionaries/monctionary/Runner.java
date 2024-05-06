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

    @Override
    public void run(ApplicationArguments args) {
        fillDb(new File("./mongo-entrypoint/en_to_en.json"), Common.SupportedLanguage.EN, Common.SupportedLanguage.EN);
        fillDb(new File("./mongo-entrypoint/ru_to_en.json"), Common.SupportedLanguage.EN, Common.SupportedLanguage.RU);
        fillDb(new File("./mongo-entrypoint/es_to_en.json"), Common.SupportedLanguage.EN, Common.SupportedLanguage.ES);
        fillDb(new File("./mongo-entrypoint/en_to_ru.json"), Common.SupportedLanguage.RU, Common.SupportedLanguage.EN);
        fillDb(new File("./mongo-entrypoint/ru_to_ru.json"), Common.SupportedLanguage.RU, Common.SupportedLanguage.RU);
        fillDb(new File("./mongo-entrypoint/es_to_ru.json"), Common.SupportedLanguage.RU, Common.SupportedLanguage.ES);
        fillDb(new File("./mongo-entrypoint/en_to_es.json"), Common.SupportedLanguage.ES, Common.SupportedLanguage.EN);
        fillDb(new File("./mongo-entrypoint/ru_to_es.json"), Common.SupportedLanguage.ES, Common.SupportedLanguage.RU);
        fillDb(new File("./mongo-entrypoint/es_to_es.json"), Common.SupportedLanguage.ES, Common.SupportedLanguage.ES);
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
