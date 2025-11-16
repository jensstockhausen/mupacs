package de.famst;

import de.famst.service.FolderImportManager;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Handles command line arguments for the MuPACS application.
 * Supports importing DICOM folders via the -i/--import option.
 * <p>
 * Created by jens on 05/10/2016.
 */
@Component
public class CommandLineHandler implements CommandLineRunner
{
    private static final Logger LOG = LoggerFactory.getLogger(CommandLineHandler.class);

    private static final String IMPORT_OPTION = "i";
    private static final String IMPORT_LONG_OPTION = "import";

    private final FolderImportManager importManager;

    public CommandLineHandler(FolderImportManager importManager)
    {
        this.importManager = importManager;
    }

    @Override
    public void run(String... args)
    {
        if (args == null || args.length == 0)
        {
            LOG.debug("No command line arguments provided");
            return;
        }

        LOG.info("Processing command line arguments");

        try
        {
            CommandLine cmd = parseArguments(args);
            logArguments(cmd);
            processImportOption(cmd);
        }
        catch (ParseException e)
        {
            LOG.error("Failed to parse command line arguments: {}", e.getMessage());
        }
    }

    private CommandLine parseArguments(String... args) throws ParseException
    {
        Options options = createOptions();
        CommandLineParser parser = new DefaultParser();
        return parser.parse(options, args);
    }

    private Options createOptions()
    {
        Options options = new Options();
        options.addOption(IMPORT_OPTION, IMPORT_LONG_OPTION, true,
            "Path to folder containing DICOM files to be imported");
        return options;
    }

    private void logArguments(CommandLine cmd)
    {
        if (LOG.isDebugEnabled())
        {
            cmd.getArgList().forEach(arg -> LOG.debug("Argument: [{}]", arg));
        }
    }

    private void processImportOption(CommandLine cmd)
    {
        if (!cmd.hasOption(IMPORT_OPTION))
        {
            return;
        }

        String importPath = cmd.getOptionValue(IMPORT_OPTION);
        if (importPath == null || importPath.trim().isEmpty())
        {
            LOG.warn("Import option specified but no path provided");
            return;
        }

        Path folder = Paths.get(importPath);

        if (!Files.exists(folder))
        {
            LOG.error("Import folder does not exist: [{}]", folder.toAbsolutePath());
            return;
        }

        if (!Files.isDirectory(folder))
        {
            LOG.error("Import path is not a directory: [{}]", folder.toAbsolutePath());
            return;
        }

        if (!Files.isReadable(folder))
        {
            LOG.error("Import folder is not readable: [{}]", folder.toAbsolutePath());
            return;
        }

        LOG.info("Triggering import for folder: [{}]", folder.toAbsolutePath());
        try
        {
            importManager.addImport(folder);
        }
        catch (InterruptedException e)
        {
            LOG.error("Import was interrupted for folder: [{}]", folder.toAbsolutePath(), e);
            Thread.currentThread().interrupt();
        }
    }

}
