
package edu.illinois.ncsa.incore.semantic.metamodel.common.reference;

public class PdfWebReference extends WebReference {
    public int pageNumber;

    public PdfWebReference(String url, int pageNumber) {
        super(url);
        this.pageNumber = pageNumber;
    }

    @Override
    public String getUrl() {
        return url + "#page=" + pageNumber;
    }
}
