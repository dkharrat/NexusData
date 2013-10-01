package org.nexusdata.modelgen;

import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;

public class Startup {
    public static void main(String[] args) throws IOException {

        CommandLine line = parseCommandLineOptions(args);
        if (line != null) {
            String modelPath = line.getOptionValue("f");
            File outputPath = new File(line.getOptionValue("O", "./out"));

            new ModelGenerator().generateModels(modelPath, outputPath);
        }
    }

    private static CommandLine parseCommandLineOptions(String[] args) {

        //TODO use option groups instead
        Options helpOptions = getHelpOptions();
        Options mainOptions = getMainOptions();

        try {
            CommandLine cmdLine = new BasicParser().parse(helpOptions, args);
            if (cmdLine.hasOption("h")) {
                printHelpMessage(mainOptions);
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

    private static Options getMainOptions() {
        Options options = new Options();
        options.addOption(OptionBuilder
                .hasArg()
                .withArgName("file")
                .withDescription("the model file")
                .isRequired(true)
                .create("f"));
        options.addOption(OptionBuilder
                .hasArg()
                .withArgName("output")
                .withDescription("the output directory")
                .isRequired(false)
                .create("O"));
        options.addOption(OptionBuilder
                .hasArgs(0)
                .withArgName("help")
                .withDescription("print this message")
                .isRequired(false)
                .create("h"));

        return options;
    }

    private static Options getHelpOptions() {
        Options options = new Options();
        options.addOption(OptionBuilder
                .hasArgs(0)
                .withArgName("help")
                .withDescription("print this message")
                .isRequired(false)
                .create("h"));

        return options;
    }

    private static void printHelpMessage(Options mainOptions) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java modelgen.jar", mainOptions);
    }
}
