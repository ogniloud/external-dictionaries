package com.madr.external_dictionaries.mongomodel.model;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Sense {
    private List<Example> examples;
    private List<String> glosses;
    private List<String> tags;
}
