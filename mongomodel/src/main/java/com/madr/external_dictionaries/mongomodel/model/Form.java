package com.madr.external_dictionaries.mongomodel.model;

import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Form {
    private String form;
    private Set<String> tags;
}
