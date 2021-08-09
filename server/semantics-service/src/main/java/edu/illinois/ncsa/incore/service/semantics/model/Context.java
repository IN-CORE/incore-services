package edu.illinois.ncsa.incore.service.semantics.model;

import dev.morphia.annotations.Embedded;
import dev.morphia.annotations.Property;

@Embedded
public class Context {

    @Property("@language")
    private String language;

    private String gml;
    private String incore;
    private String iwfs;
    private String xlink;
    private String xsd;

    public Context() {
    }

    public Context(String language, String gml, String incore, String iwfs,
                   String xlink, String xsd) {
        this.language = language;
        this.gml = gml;
        this.incore = incore;
        this.iwfs = iwfs;
        this.xlink = xlink;
        this.xsd = xsd;
    }

    public String getGml() {
        return gml;
    }

    public String getIncore() {
        return incore;
    }

    public String getIwfs() {
        return iwfs;
    }

    public String getLanguage() {
        return language;
    }

    public String getXlink() {
        return xlink;
    }

    public String getXsd() {
        return xsd;
    }
}
