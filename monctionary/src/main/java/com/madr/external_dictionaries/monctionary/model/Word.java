package com.madr.external_dictionaries.monctionary.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    @PersistenceCreator
    public Word(String word, String partOfSpeech, String etymology, List<Sense> senses) {
        this.word = word;
        this.partOfSpeech = partOfSpeech;
        this.etymology = etymology;
        this.senses = senses;
    }

    public Word() {
    }
}
