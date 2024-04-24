package com.madr.external_dictionaries.wordmaster.service.wiktionary;

import com.madr.external_dictionaries.mongomodel.model.Word;
import com.madr.external_dictionaries.protobuf.Requests;
import com.madr.external_dictionaries.wordmaster.domain.wiktionary.WordRepository;
import com.madr.external_dictionaries.wordmaster.kafka.properties.KafkaConfiguration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Log4j2
public class WiktionaryService {
    private final WordRepository wordRepository;
    private final KafkaTemplate<String, List<Word>> kafkaTemplate;
    @Value("#{kafkaConfiguration.topics.bakedWords()}")
    private KafkaConfiguration.TopicConfiguration updates;

    @KafkaListener(topics = "${spring.kafka.topics.wiktionary-requests.name}")
    public void listen(Requests.WiktionaryRequest request) {
        processRequest(request);

    }

    public void processRequest(Requests.WiktionaryRequest request) {
        List<Word> words = pullWord(request);
        CompletableFuture<SendResult<String, List<Word>>> future =
            kafkaTemplate.send(updates.name(), words);
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info(
                    "Words sent in amount =[{}] with offset=[{}]",
                    words.size(),
                    result.getRecordMetadata().offset()
                );
            } else {
                log.info("Unable to send words in amount=[{}] due to : {}", words.size(), ex.getMessage());
            }
            log.info("AFTER");
            System.exit(0);
        });
    }

    private List<Word> pullWord(Requests.WiktionaryRequest request) {
        var word = request.getWord();
        var result = wordRepository.findAll(Example.of(new Word(
            word.getWord(),
            word.getPartOfSpeech().isBlank() ? null : word.getPartOfSpeech(),
            null,
            null,
            null
        )));
        log.info("Word: " + word.getWord());
        log.info("PartOfSpeech: " + word.getPartOfSpeech());
        log.info("Found: " + result.size());
        if (!request.getContents().getEtymology()) {
            result.forEach(x -> x.setEtymology(null));
        }
        if (!request.getContents().getIpa()) {
            result.forEach(x -> x.setIpa(null));
        }
        return result;
    }
}
