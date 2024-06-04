package ch.dreyeck.zettelkasten.xml;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.swing.*;
import javax.swing.tree.*;

public class XMLViewer extends JFrame {
    private JTree tree;

    public XMLViewer(String xmlFile) {
        super("XML Viewer");

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);
            Element root = doc.getDocumentElement();

            DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(getNodeLabel(root));
            populateTree(root, rootNode);

            tree = new JTree(rootNode);
            JScrollPane scrollPane = new JScrollPane(tree);
            getContentPane().add(scrollPane);

            setSize(400, 300);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);
            setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void populateTree(Node node, DefaultMutableTreeNode treeNode) {
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node currentNode = nodeList.item(i);
            if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(getNodeLabel(currentNode));
                treeNode.add(childNode);
                populateTree(currentNode, childNode);
                NamedNodeMap attributes = currentNode.getAttributes();
                for (int j = 0; j < attributes.getLength(); j++) {
                    Node attribute = attributes.item(j);
                    childNode.add(new DefaultMutableTreeNode(attribute.getNodeName() + ": " + attribute.getNodeValue()));
                }
            }
        }
    }

    private String getNodeLabel(Node node) {
        String label = node.getNodeName();
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            String textContent = node.getTextContent().trim();
            if (!textContent.isEmpty()) {
                label += " - " + textContent;
            }
        }
        return label;
    }

    public static void main(String[] args) {
        // Example usage
        String xmlFile = "/Users/rgb/rgb~Zettelkasten/Zettelkasten-Dateien/tekom/zknFile.xml"; // Path to your XML file
        new XMLViewer(xmlFile);
    }
}
