package com.madr.external_dictionaries.monctionary.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Sense {
    private List<Example> examples;
    private List<String> glosses;
}
