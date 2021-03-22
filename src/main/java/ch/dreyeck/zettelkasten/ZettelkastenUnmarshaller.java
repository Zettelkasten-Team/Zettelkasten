package ch.dreyeck.zettelkasten;

import ch.dreyeck.zettelkasten.xml.Zettelkasten;
import jakarta.xml.bind.JAXBException;
import org.xml.sax.SAXException;

public class ZettelkastenUnmarshaller {

    /**
     * @param args
     * @throws JAXBException
     * @throws SAXException
     */
    public static void main(String[] args) throws JAXBException, SAXException {
        if (args.length != 2) {
            System.out.println("\nBitte zknFile-XSD-Schema und zknfile-XML-Dokument angeben, z.B.:\n"
                    + "java -cp ./target/classes/ ch.ZettelkastenUnmarshaller zknFile.xsd zknFile.xml");
            return;
        }
        Zettelkasten zettelkasten = JaxbMarshalUnmarshalUtil.unmarshal(args[0], args[1], Zettelkasten.class);
        zeigeZettelkasten(zettelkasten);
    }

    static void zeigeZettelkasten(Zettelkasten zettelkasten) {
        System.out.println("Zettel:");
        zettelkasten.getZettel().forEach((z) -> {
            System.out.println("  Title:   " + z.getTitle());
        });
    }
}
