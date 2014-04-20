package com.github.dkharrat.nexusdata.modelgen;

import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Startup {
    public static void main(String[] args) throws IOException {

        CommandLine line = parseCommandLineOptions(args);
        if (line != null) {
            String modelPath = line.getOptionValue(Cli.file.getOpt());
            File outputPath = new File(line.getOptionValue(Cli.output.getOpt(), "./out"));

            new ModelGenerator().generateModels(modelPath, outputPath);
        }
    }

    private static class Cli {
        private static Option help = OptionBuilder
                .hasArgs(0)
                .withLongOpt("help")
                .withDescription("print this message")
                .isRequired(false)
                .create("h");

        private static Option version = OptionBuilder
                .hasArgs(0)
                .withLongOpt("version")
                .withDescription("version number of modelgen")
                .isRequired(false)
                .create("v");

        private static Option file = OptionBuilder
                .hasArg()
                .withLongOpt("file")
                .withDescription("the model file")
                .isRequired(true)
                .create("f");

        private static Option output = OptionBuilder
                .hasArg()
                .withArgName("output")
                .withDescription("the output directory")
                .isRequired(false)
                .create("O");

        static Options getMainOptions() {
            Options options = new Options();
            options.addOption(Cli.file);
            options.addOption(Cli.output);
            options.addOption(Cli.version);
            options.addOption(Cli.help);
            return options;
        }

        static Options getHelpOptions() {
            Options options = new Options();
            options.addOption(Cli.help);
            options.addOption(Cli.version);
            return options;
        }
    }

    private static CommandLine parseCommandLineOptions(String[] args) {

        Options helpOptions = Cli.getHelpOptions();
        Options mainOptions = Cli.getMainOptions();

        try {
            CommandLine cmdLine = new BasicParser().parse(helpOptions, args);
            if (cmdLine.hasOption(Cli.help.getOpt())) {
                printHelpMessage(mainOptions);
                return null;
            } else if (cmdLine.hasOption(Cli.version.getOpt())) {
                printVersion();
                return null;
            }
        } catch( ParseException exp ) {
            // ignore
        }

        try {
            CommandLine cmdLine = new BasicParser().parse(mainOptions, args);
            return cmdLine;
        } catch( ParseException exp ) {
            System.err.println("Error parsing command-line: " + exp.getMessage());
            return null;
        }
    }

    private static void printHelpMessage(Options mainOptions) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java modelgen.jar", mainOptions);
    }

    private static void printVersion() {
        System.out.println("modelgen version " + getVersion());
    }

    private static String getVersion() {
        InputStream stream = Startup.class.getResourceAsStream("/version.properties");
        if (stream == null) {
            return "<UNKNOWN>";
        }

        Properties props = new Properties();
        try {
            props.load(stream);
            stream.close();
            return (String) props.get("version");
        } catch (IOException e) {
            return "<UNKNOWN>";
        }
    }
}
