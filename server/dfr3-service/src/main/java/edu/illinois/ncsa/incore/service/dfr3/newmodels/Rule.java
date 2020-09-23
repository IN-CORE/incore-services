package edu.illinois.ncsa.incore.service.dfr3.newmodels;

import java.util.List;

public class Rule {
    public List<String> condition;
    public String expression;

    public Rule() {

    }

    public Rule(List<String> condition, String expression){
        this.condition = condition;
        this.expression = expression;
    }
}
