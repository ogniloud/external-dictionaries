package com.madr.external_dictionaries.mongomodel.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Sound {
    @JsonProperty(required = true)
    private String ipa;
    private List<String> tags;
}
