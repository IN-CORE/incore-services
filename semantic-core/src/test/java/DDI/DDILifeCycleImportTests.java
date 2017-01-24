package DDI;

import Metadata.Specifications.DDI.LifeCycle.instance.FragmentInstanceType;
import org.junit.jupiter.api.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class DDILifeCycleImportTests {
    @Test
    public void testImportFile() throws JAXBException {
        // arrange
        ClassLoader loader = this.getClass().getClassLoader();
        File ddiFile = new File(loader.getResource("ddi/lifecycle/ICPSR2079variables.xml").getFile());

        // act
        JAXBContext jaxbContext = JAXBContext.newInstance("Metadata.Specifications.DDI.LifeCycle.archive:" +
                                                          "Metadata.Specifications.DDI.LifeCycle.comparative:" +
                                                          "Metadata.Specifications.DDI.LifeCycle.conceptualcomponent:" +
                                                          "Metadata.Specifications.DDI.LifeCycle.datacollection:" +
                                                          "Metadata.Specifications.DDI.LifeCycle.dataset:" +
                                                          "Metadata.Specifications.DDI.LifeCycle.ddiprofile:" +
                                                          "Metadata.Specifications.DDI.LifeCycle.group:" +
                                                          "Metadata.Specifications.DDI.LifeCycle.instance:" +
                                                          "Metadata.Specifications.DDI.LifeCycle.logicalproduct:" +
                                                          "Metadata.Specifications.DDI.LifeCycle.physicalinstance:" +
                                                          "Metadata.Specifications.DDI.LifeCycle.physicaldataproduct:" +
                                                          "Metadata.Specifications.DDI.LifeCycle.physicaldataproduct.ncube.inline:" +
                                                          "Metadata.Specifications.DDI.LifeCycle.physicaldataproduct.ncube.normal:" +
                                                          "Metadata.Specifications.DDI.LifeCycle.physicaldataproduct.ncube.tabular:" +
                                                          "Metadata.Specifications.DDI.LifeCycle.physicaldataproduct.proprietary:" +
                                                          "Metadata.Specifications.DDI.LifeCycle.reusable:" +
                                                          "Metadata.Specifications.DDI.LifeCycle.studyunit:" +
                                                          "Metadata.Specifications.DublinCore.elements:" +
                                                          "Metadata.Specifications.DublinCore.terms:" +
                                                          "Metadata.Specifications.DDI.xHTML");

        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        JAXBElement<FragmentInstanceType> ddiDocument = (JAXBElement<FragmentInstanceType>) jaxbUnmarshaller.unmarshal(ddiFile);
        FragmentInstanceType ddiRoot = ddiDocument.getValue();

        // assert
        System.out.println(ddiRoot);
    }
}
