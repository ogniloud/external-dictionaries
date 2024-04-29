package com.madr.external_dictionaries.wordmaster.service.wiktionary;

import com.madr.external_dictionaries.mongomodel.model.Word;
import com.madr.external_dictionaries.mongomodel.protobuf.Requests;
import com.madr.external_dictionaries.mongomodel.protobuf.Responses;
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
import static com.madr.external_dictionaries.wordmaster.service.wiktionary.WiktionaryServiceUtils.formResponse;

@RequiredArgsConstructor
@Service
@Log4j2
public class WiktionaryService {
    private final WordRepository wordRepository;
    private final KafkaTemplate<String, Responses.WiktionaryResponse> kafkaTemplate;
    @Value("#{kafkaConfiguration.topics.bakedWords()}")
    private KafkaConfiguration.TopicConfiguration updates;

    @KafkaListener(topics = "${spring.kafka.topics.wiktionary-requests.name}")
    public void listen(Requests.WiktionaryRequest request) {
        processRequest(request);
    }

    public void processRequest(Requests.WiktionaryRequest request) {
        List<Word> words = pullWord(request);
        Responses.WiktionaryResponse response;
        try {
            response = formResponse(words, request);
        } catch (Exception e) {
            log.error("Error forming response for words =[{}]", words);
            return;
        }
        CompletableFuture<SendResult<String, Responses.WiktionaryResponse>> future =
            kafkaTemplate.send(updates.name(), response);
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info(
                    "Words =[{}] sent in amount =[{}] with offset=[{}]",
                    words,
                    words.size(),
                    result.getRecordMetadata().offset()
                );
            } else {
                log.info("Unable to send words in amount=[{}] due to : {}", words.size(), ex.getMessage());
            }
        });
    }

    private List<Word> pullWord(Requests.WiktionaryRequest request) {
        var word = request.getWord();
        return wordRepository.findAll(Example.of(new Word(
            word.getWiktionaryLanguage(),
            word.getWordLanguage(),
            word.getWordId().getWord(),
            word.getWordId().getPartOfSpeech().isBlank() ? null : word.getWordId().getPartOfSpeech(),
            null,
            null,
            null
        )));
    }
}
