package ch.dreyeck.zettelkasten.xml;

import java.io.FileReader;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

public class Demo {

    public static void main(String[] args) throws Exception {
        JAXBContext jc = JAXBContext.newInstance(Element.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();

        XMLInputFactory xif = XMLInputFactory.newFactory();
        XMLStreamReader xsr = xif.createXMLStreamReader(new FileReader("input.xml"));
        xsr.nextTag();
        xsr.nextTag();
        while(xsr.hasNext()) {
            Element element = (Element) unmarshaller.unmarshal(xsr);
            System.out.println(element.getAttribute());
            if(xsr.nextTag() != XMLStreamReader.START_ELEMENT) {
                break;
            }
        }
    }

}