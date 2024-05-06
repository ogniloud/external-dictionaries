package com.madr.external_dictionaries.wordmaster.service.wiktionary;

import com.madr.external_dictionaries.mongomodel.model.Example;
import com.madr.external_dictionaries.mongomodel.model.Form;
import com.madr.external_dictionaries.mongomodel.model.Sense;
import com.madr.external_dictionaries.mongomodel.model.Sound;
import com.madr.external_dictionaries.mongomodel.model.Word;
import com.madr.external_dictionaries.mongomodel.protobuf.Common;
import com.madr.external_dictionaries.mongomodel.protobuf.Requests;
import com.madr.external_dictionaries.mongomodel.protobuf.Responses;
import com.madr.external_dictionaries.mongomodel.protobuf.Spanish;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("MultipleStringLiterals")
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
        if (requestedContents.getInflections() && word.getForms() != null) {
            if (word.getWordLanguage().equals(Common.SupportedLanguage.ES)) {
                if (word.getPartOfSpeech().equals("verb")) {
                    builder.setSpanishVerbInflections(formSpanishVerbInflections(word.getForms()));
                }
            }
        }
        return builder.build();
    }

    private static Spanish.SpanishVerbInflections formSpanishVerbInflections(List<Form> forms) {
        return Spanish.SpanishVerbInflections.newBuilder()
            .setInfinitive(findFormByTags(forms, List.of("infinitive")))
            .setGerund(findFormByTags(forms, List.of("gerund")))
            .setPastParticiple(findFormByTags(forms, List.of("past", "participle", "masculine")))
            .setIndicative(formSpanishVerbIndicativeInflections(findAllFormsByTag(forms, "indicative")))
            .setSubjunctive(formSpanishVerbSubjunctiveInflections(findAllFormsByTag(forms, "subjunctive")))
            .setImperative(formSpanishVerbImperativeInflections(findAllFormsByTag(forms, "imperative")))
            .build();
    }

    private static Spanish.SpanishVerbIndicativeInflections formSpanishVerbIndicativeInflections(
        List<Form> indicative
    ) {
        return Spanish.SpanishVerbIndicativeInflections.newBuilder()
            .setPresent(formSpanishVerbPersonInflections(findAllFormsByTag(indicative, "present")))
            .setImperfect(formSpanishVerbPersonInflections(findAllFormsByTag(indicative, "imperfect")))
            .setPreterite(formSpanishVerbPersonInflections(findAllFormsByTag(indicative, "preterite")))
            .setFuture(formSpanishVerbPersonInflections(findAllFormsByTag(indicative, "future")))
            .setConditional(formSpanishVerbPersonInflections(findAllFormsByTag(indicative, "conditional")))
            .build();
    }

    private static Spanish.SpanishVerbSubjunctiveInflections formSpanishVerbSubjunctiveInflections(
        List<Form> subjunctive) {
        return Spanish.SpanishVerbSubjunctiveInflections.newBuilder()
            .setPresent(formSpanishVerbPersonInflections(findAllFormsByTag(subjunctive, "present")))
            .setImperfectSe(formSpanishVerbPersonInflections(findAllFormsByTag(subjunctive, "imperfect-se")))
            .setImperfectRa(formSpanishVerbPersonInflections(findAllFormsByTag(subjunctive.stream()
                .filter(form -> !form.getTags().contains("imperfect-se")).toList(), "imperfect")))
            .setFuture(formSpanishVerbPersonInflections(findAllFormsByTag(subjunctive, "future")))
            .build();
    }

    private static Spanish.SpanishVerbImperativeInflections formSpanishVerbImperativeInflections(
        List<Form> imperative) {
        return Spanish.SpanishVerbImperativeInflections.newBuilder()
            .setAffirmative(formSpanishVerbPersonInflections(imperative.stream()
                .filter(form -> !form.getTags().contains("negative")).toList()))
            .setNegative(formSpanishVerbPersonInflections(findAllFormsByTag(imperative, "negative")))
            .build();
    }

    private static Spanish.SpanishVerbPersonInflections formSpanishVerbPersonInflections(List<Form> forms) {
        return Spanish.SpanishVerbPersonInflections.newBuilder()
            .setYo(findFormByTags(forms, List.of("first-person", "singular"), "-"))
            .setVos(findFormByTags(forms, List.of("second-person", "singular", "vos-form"), "same as tú"))
            .setTu(findFormByTags(
                forms.stream().filter(form -> !form.getTags().contains("vos-form")).toList(),
                List.of("second-person", "singular")
            ))
            .setElloUsted(findFormByTags(forms, List.of("third-person", "singular")))
            .setNosotros(findFormByTags(forms, List.of("first-person", "plural")))
            .setVosotros(findFormByTags(forms, List.of("second-person", "plural")))
            .setEllosUstedes(findFormByTags(forms, List.of("third-person", "plural")))
            .build();
    }

    private static String findFormByTags(List<Form> forms, List<String> tags) {
        return findFormByTags(forms, tags, "unknown");
    }

    private static String findFormByTags(List<Form> forms, List<String> tags, String defaultUnknown) {
        return forms.stream()
            .filter(form -> form.getTags().containsAll(tags))
            .findFirst()
            .map(Form::getForm)
            .orElse(defaultUnknown);
    }

    private static List<Form> findAllFormsByTag(List<Form> forms, String tag) {
        return forms.stream()
            .filter(form -> form.getTags().contains(tag))
            .toList();
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
