package com.polyglot.lucene.chp01;

import io.vavr.Tuple;
import io.vavr.Tuple3;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Optional;

/**
 * @author Rajiv Singla . Creation Date: 10/19/2017.
 */
public class AppMain {

    private static final Logger LOG = LoggerFactory.getLogger(AppMain.class);

    private static final Tuple3<String, String, String> DATA_DIR_OPTION = Tuple.of("d", "dataDir",
            "Source directory containing text files that need to be indexed");
    private static final Tuple3<String, String, String> INDEX_DIR_OPTION = Tuple.of("i", "indexDir",
            "Location of directory where file indexes will be stored");

    public static void main(String[] args) throws IOException {

        final Optional<CommandLine> commandLineOptional = generateCommandLine(args);
        if (!commandLineOptional.isPresent()) {
            LOG.debug("Early termination due to invalid arguments");
            return;
        }
        final CommandLine commandLine = commandLineOptional.get();
        final String dataDirLocation = commandLine.getOptionValue(DATA_DIR_OPTION._2);
        final String indexDirLocation = commandLine.getOptionValue(INDEX_DIR_OPTION._2);

        LOG.debug("Command Line arguments. DataDir: {}, IndexDir: {}", dataDirLocation, indexDirLocation);

        final File dataDirFile = new File(dataDirLocation);
        final File indexDirFile = new File(indexDirLocation);

        if (!dataDirFile.exists() && !dataDirFile.isDirectory()) {
            throw new IllegalArgumentException("Invalid data directory location: " + dataDirLocation);
        }

        if (!indexDirFile.exists()) {
            Files.createDirectories(indexDirFile.toPath());
        }

        final long indexedDocumentsCount = Indexer.indexDocuments(dataDirFile, indexDirFile,
                (f) -> f.getName().endsWith(".txt"));

        LOG.info("Index documents count: {}", indexedDocumentsCount);

    }


    private static Optional<CommandLine> generateCommandLine(final String[] args) {
        final Options options = generateOptions();
        final CommandLineParser commandLineParser = new DefaultParser();
        CommandLine commandLine = null;
        try {
            commandLine = commandLineParser.parse(options, args);
        } catch (ParseException e) {
            LOG.debug("Error while parsing command line arguments: {}, Parse Exception: {}", Arrays.toString(args), e);
            printHelp(options);
            return Optional.empty();
        }
        return Optional.of(commandLine);
    }


    private static void printHelp(final Options options) {
        final HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("AppMain", options);
    }


    private static Options generateOptions() {
        final Option datDirOption = Option.builder(DATA_DIR_OPTION._1)
                .longOpt(DATA_DIR_OPTION._2)
                .desc(DATA_DIR_OPTION._3)
                .required()
                .hasArg(true)
                .build();
        final Option indexDirOption = Option.builder(INDEX_DIR_OPTION._1)
                .longOpt(INDEX_DIR_OPTION._2)
                .desc(INDEX_DIR_OPTION._3)
                .required()
                .hasArg(true)
                .build();

        final Options options = new Options();
        options.addOption(datDirOption);
        options.addOption(indexDirOption);
        return options;
    }

}
