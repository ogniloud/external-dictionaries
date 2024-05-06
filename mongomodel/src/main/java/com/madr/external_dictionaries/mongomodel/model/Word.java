package com.madr.external_dictionaries.mongomodel.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.madr.external_dictionaries.mongomodel.protobuf.Common;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Getter
@Setter
@ToString
public class Word {
    @Indexed(direction = IndexDirection.ASCENDING)
    private String word;
    @Indexed(direction = IndexDirection.ASCENDING)
    @JsonProperty("pos")
    private String partOfSpeech;
    @JsonProperty("etymology_text")
    private String etymology;
    private List<Sense> senses;
    private List<Sound> sounds;
    @Indexed
    private Common.SupportedLanguage wiktionaryLanguage;
    @Indexed
    private Common.SupportedLanguage wordLanguage;

    @PersistenceCreator
    public Word(
        Common.SupportedLanguage wiktionaryLanguage,
        Common.SupportedLanguage wordLanguage,
        String word,
        String partOfSpeech,
        String etymology,
        List<Sense> senses,
        List<Sound> sounds
    ) {
        this.word = word;
        this.partOfSpeech = partOfSpeech;
        this.etymology = etymology;
        this.senses = senses;
        this.sounds = sounds;
        this.wiktionaryLanguage = wiktionaryLanguage;
        this.wordLanguage = wordLanguage;
    }

    public Word() {
    }

    public void trimSounds() {
        if (sounds != null) {
            sounds.removeIf(sound -> sound.getIpa() == null);
            if (sounds.isEmpty()) {
                sounds = null;
            }
        }
    }
}
