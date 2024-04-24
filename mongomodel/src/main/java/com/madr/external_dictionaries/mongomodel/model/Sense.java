package com.madr.external_dictionaries.mongomodel.model;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Sense {
    private List<Example> examples;
    private List<String> glosses;
}