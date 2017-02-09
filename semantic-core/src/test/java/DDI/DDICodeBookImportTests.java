package DDI;

import Metadata.Specifications.DDI.CodeBook.CodeBook;
import org.junit.jupiter.api.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

public class DDICodeBookImportTests {

    @Test
    public void testImportFile() throws JAXBException {
        // arrange
        ClassLoader loader = this.getClass().getClassLoader();
        File ddiFile = new File(loader.getResource("ddi/codebook/icpsr metadata record.xml").getFile());

        // act
        JAXBContext jaxbContext = JAXBContext.newInstance("Metadata.Specifications.DDI.xHTML:" +
                                                          "Metadata.Specifications.DublinCore.elements:" +
                                                          "Metadata.Specifications.DublinCore.terms:" +
                                                          "Metadata.Specifications.DDI.CodeBook");

        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        CodeBook ddiCodeBook = (CodeBook) jaxbUnmarshaller.unmarshal(ddiFile);

        // assert
        System.out.println(ddiCodeBook);
    }
}
