package com.splicemachine.jmeter.generator;

import com.splicemachine.jmeter.generator.parser.ParserUtils;
import com.splicemachine.jmeter.generator.parser.QueryHolder;
import org.apache.commons.cli.*;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.*;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main class to generate tests from .sql files
 *
 * @author Andrey Paslavsky
 */
public class TestGenerator {
    public static final Options OPTIONS = new Options().addOption(
            Option.builder("s").longOpt("sql").hasArg().argName("SQL file").optionalArg(false).type(File.class).desc("Source .sql file to generate JMeter test").build()
    ).addOption(
            Option.builder("j").longOpt("jmx").hasArg().argName("JMX file").optionalArg(false).type(File.class).desc("Output .jmx file").build()
    ).addOption(
            Option.builder("t").longOpt("template").hasArg().argName("Template file").optionalArg(false).type(File.class).desc("Template file").build()
    ).addOption(
            Option.builder("h").longOpt("help").desc("Print help").build()
    );

    public static void main(String[] args) {
        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine commandLine = parser.parse(OPTIONS, args);
            if (commandLine.hasOption('h')) {
                printHelp();
            } else if (!commandLine.hasOption('s')) {
                throw new ParseException("SQL file is required");
            } else if (!commandLine.hasOption('j')) {
                throw new ParseException("JMX file is required");
            } else {
                InputStreamReader template = getTemplate(commandLine);
                try {
                    generateTest(getFile(commandLine, "s"), getFile(commandLine, "j"), template);
                } finally {
                    template.close();
                }
            }
        } catch (Exception e) {
            System.err.println("Please use --help for more information");
            Logger.getLogger(TestGenerator.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            System.exit(1);
        }
    }

    private static File getFile(CommandLine commandLine, String option) throws ParseException {
        return (File) commandLine.getParsedOptionValue(option);
    }

    private static InputStreamReader getTemplate(CommandLine commandLine) throws Exception {
        if (commandLine.hasOption("t")) {
            File file = getFile(commandLine, "t");
            if (!file.exists()) {
                throw new IllegalArgumentException("Template " + file.getPath() + " not exists");
            } else if (!file.isFile()) {
                throw new IllegalArgumentException("Path " + file.getPath() + " refer to the directory");
            }
            return new InputStreamReader(new FileInputStream(file), "UTF-8");
        } else {
            InputStream input = TestGenerator.class.getClassLoader().getResourceAsStream("templates/default.xml");
            return new InputStreamReader(input, "UTF-8");
        }
    }

    private static void generateTest(File sql, File jmx, InputStreamReader template) throws Exception {
        Collection<QueryHolder> queries = ParserUtils.getQueries(sql.toPath());
        VelocityEngine engine = new VelocityEngine();
        VelocityContext context = new VelocityContext();
        context.put("queries", queries);

        BufferedWriter writer = new BufferedWriter(new FileWriter(jmx, false));
        try {
            if (!engine.evaluate(context, writer, "jmx-template", template)) {
                throw new RuntimeException("Failed to generate test file");
            }
        } finally {
            writer.flush();
            writer.close();
        }
    }

    private static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(Integer.MAX_VALUE);
        formatter.printHelp("jmeter-test-generator.jar (-j <JMX file> -s <SQL file> [-t <template>])|--help", OPTIONS);
    }
}
