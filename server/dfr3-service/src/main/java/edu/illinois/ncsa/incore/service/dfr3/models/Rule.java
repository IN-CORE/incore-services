package edu.illinois.ncsa.incore.service.dfr3.models;

import dev.morphia.annotations.Embedded;

import java.util.List;

@Embedded
public class Rule {
    public List<String> condition;
    public String expression;

    public Rule() {

    }

    public Rule(List<String> condition, String expression) {
        this.condition = condition;
        this.expression = expression;
    }
}
