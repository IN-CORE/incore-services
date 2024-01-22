package edu.illinois.ncsa.incore.service.semantics.model;

import dev.morphia.annotations.Embedded;
import java.util.Map;

@Embedded
public class Context {
    private String namespace;

    private Map<String, String> vocabularies;

    public Context() {
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public Map<String, String> getVocabularies() {
        return vocabularies;
    }

    public void setVocabularies(Map<String, String> vocabularies) {
        this.vocabularies = vocabularies;
    }

}
