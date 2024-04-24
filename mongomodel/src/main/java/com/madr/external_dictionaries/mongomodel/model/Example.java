package com.madr.external_dictionaries.mongomodel.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Example {
    private String text;
    private String ref;
    private String type;
}
