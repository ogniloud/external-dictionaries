package com.madr.external_dictionaries.wordmaster.service.wiktionary;

import com.madr.external_dictionaries.mongomodel.model.Example;
import com.madr.external_dictionaries.mongomodel.model.Sense;
import com.madr.external_dictionaries.mongomodel.model.Sound;
import com.madr.external_dictionaries.mongomodel.model.Word;
import com.madr.external_dictionaries.mongomodel.protobuf.Common;
import com.madr.external_dictionaries.mongomodel.protobuf.Requests;
import com.madr.external_dictionaries.mongomodel.protobuf.Responses;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class WiktionaryServiceUtils {
    private WiktionaryServiceUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static Responses.WiktionaryResponse formResponse(List<Word> words, Requests.WiktionaryRequest request) {
        var builder = Responses.WiktionaryResponse
            .newBuilder()
            .setSource(request.getSource());
        if (request.getContents().getSingle()) {
            builder.addWords(formWord(words.get(ThreadLocalRandom.current().nextInt(words.size())), request));
        } else {
            builder.addAllWords(
                words.stream()
                    .map(word -> formWord(word, request))
                    .toList()
            );
        }
        return builder.build();
    }

    private static Responses.BakedWiktionaryWord formWord(Word word, Requests.WiktionaryRequest request) {
        var builder = Responses.BakedWiktionaryWord
            .newBuilder()
            .setWord(
                Common.WordId.newBuilder()
                    .setWord(word.getWord())
                    .setPartOfSpeech(word.getPartOfSpeech())
                    .build()
            );
        var contents = request.getContents();
        if (contents.getEtymology() || contents.getIpa()) {
            builder.setContents(formContents(word, contents));
        }
        if (contents.getDefinition() || contents.getExamples()) {
            if (contents.getSingle()) {
                builder.addSenses(
                    formSense(
                        word.getSenses().get(ThreadLocalRandom.current().nextInt(word.getSenses().size())),
                        contents
                    )
                );
            } else {
                builder.addAllSenses(
                    word.getSenses()
                        .stream()
                        .map(sense -> formSense(sense, contents))
                        .toList()
                );
            }
        }
        return builder.build();
    }

    private static Responses.WordContents formContents(
        Word word,
        Requests.RequestedContents requestedContents
    ) {
        var builder = Responses.WordContents.newBuilder();
        if (requestedContents.getIpa() && word.getSounds() != null) {
            if (requestedContents.getSingle()) {
                builder.addIpa(
                    formSound(word.getSounds().get(ThreadLocalRandom.current().nextInt(word.getSounds().size())))
                );
            } else {
                builder.addAllIpa(
                    word.getSounds()
                        .stream()
                        .map(WiktionaryServiceUtils::formSound)
                        .toList()
                );
            }
        }
        if (requestedContents.getEtymology() && word.getEtymology() != null) {
            builder.setEtymology(word.getEtymology());
        }
        return builder.build();
    }

    private static Responses.Sound formSound(Sound sound) {
        var builder = Responses.Sound
            .newBuilder()
            .setIpa(sound.getIpa());
        if (sound.getTags() != null) {
            builder.addAllTags(sound.getTags());
        }
        return builder.build();
    }

    private static Responses.WiktionarySense formSense(
        Sense sense,
        Requests.RequestedContents requestedContents
    ) {
        var builder = Responses.WiktionarySense.newBuilder();
        if (requestedContents.getDefinition() && sense.getGlosses() != null) {
            builder.addAllGlosses(
                sense.getGlosses()
            );
            if (sense.getTags() != null) {
                builder.addAllTags(sense.getTags());
            }
        }
        if (requestedContents.getExamples() && sense.getExamples() != null) {
            builder.addAllExamples(
                sense.getExamples()
                    .stream()
                    .map(WiktionaryServiceUtils::formExample)
                    .toList()
            );
        }
        return builder.build();
    }

    private static Responses.WiktionaryExample formExample(Example example) {
        var builder = Responses.WiktionaryExample
            .newBuilder()
            .setText(example.getText());
        if (example.getRef() != null) {
            builder.setRef(example.getRef());
        }
        if (example.getType() != null) {
            builder.setType(example.getType());
        }
        return builder.build();
    }
}
