package com.madr.external_dictionaries.wordmaster.kafka.serdes;

import com.madr.external_dictionaries.mongomodel.model.Example;
import com.madr.external_dictionaries.mongomodel.model.Sense;
import com.madr.external_dictionaries.mongomodel.model.Word;
import com.madr.external_dictionaries.protobuf.Responses;
import java.util.List;
import org.apache.kafka.common.serialization.Serializer;

public class WiktionaryResponseSerializer implements Serializer<List<Word>> {
    @Override
    public byte[] serialize(String s, List<Word> words) {
        return Responses.WiktionaryResponse
            .newBuilder()
            .addAllWords(
                words.stream()
                    .map(
                        this::serializeWord
                    ).toList()
            )
            .build()
            .toByteArray();
    }

    private Responses.BakedWiktionaryWord serializeWord(Word word) {
        return Responses.BakedWiktionaryWord
            .newBuilder()
            .setId(
                Responses.WiktionaryMain.newBuilder()
                    .setWord(word.getWord())
                    .setPartOfSpeech(word.getPartOfSpeech())
                    .setEtymology(word.getEtymology())
                    .setIpa(word.getIpa())
            )
            .addAllSenses(
                word.getSenses().stream()
                    .map(this::serializeSense)
                    .toList()
            )
            .build();
    }

    private Responses.WiktionarySense serializeSense(Sense sense) {
        return Responses.WiktionarySense
            .newBuilder()
            .addAllExamples(
                sense.getExamples().stream()
                    .map(this::serializeExample)
                    .toList()
            )
            .addAllGlosses(
                sense.getGlosses().stream()
                    .map(this::serializeGloss)
                    .toList()
            )
            .build();
    }

    private Responses.WiktionaryExample serializeExample(Example example) {
        return Responses.WiktionaryExample
            .newBuilder()
            .setText(example.getText())
            .setRef(example.getRef())
            .setType(example.getType())
            .build();
    }

    private Responses.Gloss serializeGloss(String gloss) {
        return Responses.Gloss.newBuilder()
            .setGloss(gloss)
            .build();
    }
}
