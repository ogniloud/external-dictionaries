package com.madr.external_dictionaries.mongomodel.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Sound {
    private String ipa;
    private List<String> tags;
}
