package com.splicemachine.jmeter.generator.parser;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Class-helper
 * Created by akorotenko on 1/26/16.
 */
public class ParserUtils {

    public final static String XPATH_TO_ASSERTION = "//ResponseAssertion";
    public final static String XPATH_TO_RESPONSE = "//sample/responseData";


    public static Collection<QueryHolder> getQueries(Path path) throws IOException {
        List<QueryHolder> queries = new ArrayList<QueryHolder>();

        if (!Files.exists(path)) {
            throw new IllegalArgumentException("File " + path + " doesn't exists!");
        }

        List<String> lines = Files.readAllLines(path, Charset.defaultCharset());

        StringBuilder commentsHolder = new StringBuilder();
        StringBuilder queryHolder = new StringBuilder();

        QueryType type = null;

        for (String line : lines) {
            line = line.trim();

            // ignore empty rows
            if (line.length() == 0) {
                continue;
            }

            // append comments
            if (line.startsWith("--")) {
                commentsHolder.append(line);
                commentsHolder.append("\n");
                continue;
            }

            // get type
            type = type == null ? QueryType.getType(line) : type;
            if (type != null) {

                // append query
                queryHolder.append(line + " ");

                // if ';' - push query and comments to result and clear query and comment holders
                if (line.contains(";")) {
                    String q = queryHolder.toString().replace(";", "");
                    queries.add(new QueryHolder(type, q, commentsHolder.toString()));
                    commentsHolder.setLength(0);
                    queryHolder.setLength(0);
                    type = null;
                }
            }
        }

        return queries;
    }

    public static ByteArrayOutputStream mergeJmxAndResponse(Path jmx, Path xml) throws IOException,
            ParserConfigurationException, SAXException, XPathExpressionException {

        // Check files existing
        if (!Files.exists(jmx)) {
            throw new IllegalArgumentException("File JMX doesn't exists!");
        }

        if (!Files.exists(xml)) {
            throw new IllegalArgumentException("File XML doesn't exists!");
        }

        XPathFactory xPathfactory = XPathFactory.newInstance();

        XPath xpathJmx = xPathfactory.newXPath();
        XPath xpathXml = xPathfactory.newXPath();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // factory.setValidating(true);
        factory.setIgnoringElementContentWhitespace(true);
        DocumentBuilder jmxBuilder = factory.newDocumentBuilder();
        Document jmxDoc = jmxBuilder.parse(jmx.toFile());


        DocumentBuilder xmlBuilder = factory.newDocumentBuilder();
        Document xmlDoc = xmlBuilder.parse(xml.toFile());

        XPathExpression assertionExpr = xpathJmx.compile(XPATH_TO_ASSERTION);

        XPathExpression responseExpr = xpathXml.compile(XPATH_TO_RESPONSE);
        NodeList xmlNodeList = (NodeList) responseExpr.evaluate(xmlDoc, XPathConstants.NODESET);
        int xmlCursor = 0;

        NodeList jmxNodeList = (NodeList) assertionExpr.evaluate(jmxDoc, XPathConstants.NODESET);

        for (int i = 0; i < jmxNodeList.getLength(); i++) {
            Node node = jmxNodeList.item(i);
            Node enabled = node.getAttributes().getNamedItem("enabled");
            if (enabled != null && "true".equals(enabled.getNodeValue())) {
                NodeList list = node.getChildNodes();
                for (int j = 0; j < list.getLength(); j++) {
                    Node collectionProp = list.item(j);
                    if (collectionProp.getNodeName().equals("collectionProp")) {
                        Node responseData = xmlNodeList.item(xmlCursor++);
                        if (responseData != null) {
                            addStringPropNode(collectionProp, responseData.getTextContent());
                        } else {
                            System.out.println("Couldn't find response data for ResponseAssertion");
                        }
                        break;
                    }
                }
            } else {
                System.out.println("ResponseAssertion is disabled: " + node.getNodeName());
            }
        }

        // serialize JMX to stream
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        xmlToStream(jmxDoc, result);

        return result;
    }

    private static void addStringPropNode(Node parent, String value) {
        Node stringProp = parent.getOwnerDocument().createElement("stringProp");
        Attr nameAttr = parent.getOwnerDocument().createAttribute("name");
        nameAttr.setNodeValue(String.valueOf(System.currentTimeMillis()));
        stringProp.setTextContent(value);
        stringProp.getAttributes().setNamedItem(nameAttr);

        if (parent.getChildNodes().getLength() == 0) {
            parent.appendChild(stringProp);
        } else {
            parent.insertBefore(stringProp, parent.getFirstChild());
        }
    }

    private static void xmlToStream(Document doc, OutputStream stream) throws IOException {
        OutputFormat format = new OutputFormat(doc);
        format.setIndenting(true);
        XMLSerializer serializer = new XMLSerializer(stream, format);

        serializer.serialize(doc);
    }
}
