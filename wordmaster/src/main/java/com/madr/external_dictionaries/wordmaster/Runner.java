package com.madr.external_dictionaries.wordmaster;

import com.madr.external_dictionaries.protobuf.Requests;
import com.madr.external_dictionaries.wordmaster.service.wiktionary.WiktionaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Log4j2
public class Runner implements ApplicationRunner {
    private final WiktionaryService service;

    @Override
    public void run(ApplicationArguments args) {
        log.info("BEFORE");
        service.processRequest(Requests.WiktionaryRequest
            .newBuilder()
            .setWord(
                Requests.WiktionaryId.newBuilder()
                    .setWord("word")
                    .setPartOfSpeech("noun")
                    .setWiktionaryLanguage(Requests.SupportedLanguage.EN)
                    .setWordLanguage(Requests.SupportedLanguage.EN)
                    .build()
            )
            .setContents(
                Requests.WiktionaryContents.newBuilder()
                    .setIpa(false)
                    .setEtymology(false)
                    .build()
            )
            .build()
        );
    }
}
