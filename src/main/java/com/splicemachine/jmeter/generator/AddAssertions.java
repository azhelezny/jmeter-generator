package com.splicemachine.jmeter.generator;

import com.splicemachine.jmeter.generator.parser.ParserUtils;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

/**
 * Main class to add expected values to the .jmx file
 *
 * @author Andrey Paslavsky
 */
public class AddAssertions {
    public static final Options OPTIONS = new Options().addOption(
            Option.builder("i").hasArg().argName("input file").optionalArg(false).type(File.class).desc("Input .jmx file").build()
    ).addOption(
            Option.builder("o").hasArg().argName("output file").optionalArg(false).type(File.class).desc("Output .jmx file").build()
    ).addOption(
            Option.builder("e").hasArg().argName("xml").optionalArg(false).type(File.class).desc("XML file with expected results").build()
    ).addOption(
            Option.builder("h").longOpt("help").desc("Print help").build()
    );

    public static void main(String[] args) {
        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine cl = parser.parse(OPTIONS, args);
            if (cl.hasOption('h')) {
                printHelp();
            } else if (!cl.hasOption('i')) {
                throw new ParseException("Input file is required");
            } else if (!cl.hasOption('o')) {
                throw new ParseException("Output file is required");
            } else if (!cl.hasOption('e')) {
                throw new ParseException("File with expected results is required");
            } else {
                File outputFile = getFile(cl, "o");
                File inputFile = getFile(cl, "i");
                File expectedResults = getFile(cl, "e");
                addAssertions(inputFile, outputFile, expectedResults);
            }
        } catch (Exception e) {
            System.err.println("Please use --help for more information");
            Logger.getLogger(TestGenerator.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            System.exit(1);
        }
    }

    private static void addAssertions(File inputFile, File outputFile, File expectedResults) throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
        ByteArrayOutputStream stream = null;
        FileOutputStream out = null;
        try {
            stream = ParserUtils.mergeJmxAndResponse(inputFile.toPath(), expectedResults.toPath());
            stream.writeTo((out = new FileOutputStream(outputFile)));
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ignored) {
                }
            }

            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private static File getFile(CommandLine commandLine, String option) throws ParseException {
        return (File) commandLine.getParsedOptionValue(option);
    }

    private static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(Integer.MAX_VALUE);
        formatter.printHelp("add-assertions.jar (-i <input file> -o <output file> -e <xml>)|--help", OPTIONS);
    }
}
