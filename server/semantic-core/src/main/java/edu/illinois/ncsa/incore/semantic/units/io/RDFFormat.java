
package edu.illinois.ncsa.incore.semantic.units.io;

public enum RDFFormat {
    TURTLE("TURTLE"),
    NTRIPLES("N-TRIPLES"),
    JSONLD("JSON-LD"),
    RDFXMLABBREV("RDF/XML-ABBREV"),
    RDFXML("RDF/XML"),
    N3("N3"),
    RDFJSON("RDF/JSON");

    private String value;

    private RDFFormat(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
