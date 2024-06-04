package ch.dreyeck.zettelkasten.xml;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;

public class Validator {

    public static boolean validateXML(String xmlFilePath, String xsdFilePath) {
        try {
            // Create a SchemaFactory and specify XML schema language
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            // Load the XSD schema
            Schema schema = factory.newSchema(new File(xsdFilePath));

            // Create a validator instance
            javax.xml.validation.Validator validator = schema.newValidator();

            // Validate XML against XSD
            Source source = new StreamSource(new File(xmlFilePath));
            validator.validate(source);

            // If validation succeeds, return true
            return true;
        } catch (Exception e) {
            // Validation failed, print error message
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {
        String xmlFilePath = "/Users/rgb/rgb~Zettelkasten/Zettelkasten-Dateien/rgb/zknFile.xml";
        String xsdFilePath = "/Users/rgb/Projects/ZettelkastenFX/src/main/resources/ch/dreyeck/zettelkasten/xml/zknFile.xsd";

        boolean isValid = validateXML(xmlFilePath, xsdFilePath);
        if (isValid) {
            System.out.println("XML is valid against XSD.");
        } else {
            System.out.println("XML is not valid against XSD.");
        }
    }
}

