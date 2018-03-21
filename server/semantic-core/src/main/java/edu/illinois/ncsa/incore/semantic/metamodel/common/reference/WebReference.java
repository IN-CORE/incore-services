
package edu.illinois.ncsa.incore.semantic.metamodel.common.reference;

public class WebReference extends Reference {
    public String url;

    public WebReference(String url) {
        this.url = url;
    }

    public String getUrl() {
        return this.url;
    }
}
