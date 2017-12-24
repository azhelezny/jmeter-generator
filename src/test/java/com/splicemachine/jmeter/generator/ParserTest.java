package com.splicemachine.jmeter.generator;

import com.splicemachine.jmeter.generator.parser.ParserUtils;
import com.splicemachine.jmeter.generator.parser.QueryHolder;
import com.splicemachine.jmeter.generator.parser.QueryType;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;

/**
 * Simple query parser tests
 * Created by akorotenko on 1/26/16.
 */
public class ParserTest {

    public final static String RESOURCES_PATH = "src/test/resources/";
    public final static String SQL_RESOURCES_PATH = RESOURCES_PATH + "sql/";
    public final static String JMX_RESOURCES_PATH = RESOURCES_PATH + "jmx/";
    public final static String GEN_RESOURCES_PATH = RESOURCES_PATH + "generated/";


    @Test
    public void testInnerJoinSql() throws IOException {
        Path pathToQuery = Paths.get(SQL_RESOURCES_PATH + "innerjoin.sql");
        Collection<QueryHolder> queries = ParserUtils.getQueries(pathToQuery);

        int numberOfSelect = 0;
        int numberOfCreate = 0;
        int numberOfInsert = 0;
        int numberOfDrop = 0;
        int numberOfUpdate = 0;

        for (QueryHolder queryHolder : queries) {
//            System.out.println(queryHolder.getjMeterQueryType());
//            System.out.println(queryHolder.getType().name());
//            System.out.println(queryHolder.getValue());

            System.out.println("\n");
            if (queryHolder.getType().equals(QueryType.SELECT)) {
                numberOfSelect++;
            } else if (queryHolder.getType().equals(QueryType.INSERT)) {
                numberOfInsert++;
            } else if (queryHolder.getType().equals(QueryType.CREATE)) {
                numberOfCreate++;
            } else if (queryHolder.getType().equals(QueryType.DROP)) {
                numberOfDrop++;
            } else if (queryHolder.getType().equals(QueryType.UPDATE)) {
                numberOfUpdate++;
            }
        }

        Assert.assertEquals(numberOfSelect, 31, "Wrong number of SELECT");
        Assert.assertEquals(numberOfInsert, 5, "Wrong number of INSERT");
        Assert.assertEquals(numberOfDrop, 8, "Wrong number of DROP");
        Assert.assertEquals(numberOfCreate, 4, "Wrong number of CREATE");
        Assert.assertEquals(numberOfUpdate, 1, "Wrong number of UPDATE");
    }

    @Test
    public void testMergeJmxAndXmlResponse() throws ParserConfigurationException, SAXException, XPathExpressionException, IOException {
        if (!Files.exists(Paths.get(GEN_RESOURCES_PATH))) {
            Files.createDirectories(Paths.get(GEN_RESOURCES_PATH));
        }
        Path pathToGenFolder = Paths.get(GEN_RESOURCES_PATH);
        Files.walkFileTree(pathToGenFolder, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException {
                System.out.println("Deleting file: " + file);
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }
        });


        Path pathToJmx = Paths.get(JMX_RESOURCES_PATH + "template.jmx");
        Path pathToXml = Paths.get(JMX_RESOURCES_PATH + "assertion-results.txt");

        Path pathToGenerated = Paths.get(GEN_RESOURCES_PATH + System.currentTimeMillis() + "_merged.jmx");

        ByteArrayOutputStream result = ParserUtils.mergeJmxAndResponse(pathToJmx, pathToXml);

        Files.write(pathToGenerated, result.toByteArray());

        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpathJmx = xPathfactory.newXPath();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // factory.setValidating(true);
        factory.setIgnoringElementContentWhitespace(true);
        DocumentBuilder jmxBuilder = factory.newDocumentBuilder();
        Document jmxDoc = jmxBuilder.parse(pathToGenerated.toFile());

        XPathExpression assertionExpr = xpathJmx.compile("//ResponseAssertion/collectionProp/stringProp");

        NodeList jmxNodeList = (NodeList) assertionExpr.evaluate(jmxDoc, XPathConstants.NODESET);

        Assert.assertEquals(jmxNodeList.getLength(), 8);

        Assert.assertEquals(jmxNodeList.item(0).getTextContent(), "Table/View 'FOO' does not exist.");

    }

}
