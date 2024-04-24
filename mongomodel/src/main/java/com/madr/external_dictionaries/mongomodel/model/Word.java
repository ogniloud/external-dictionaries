package com.madr.external_dictionaries.mongomodel.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Getter
@Setter
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

    @PersistenceCreator
    public Word(String word, String partOfSpeech, String etymology, String ipa, List<Sense> senses, List<Sound> sounds) {
        this.word = word;
        this.partOfSpeech = partOfSpeech;
        this.etymology = etymology;
        this.senses = senses;
        this.sounds = sounds;
    }

    public Word() {
    }
}