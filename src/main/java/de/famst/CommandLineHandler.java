package de.famst;

import de.famst.service.FolderImportManager;
import jakarta.inject.Inject;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by jens on 05/10/2016.
 */
@Component
public class CommandLineHandler implements CommandLineRunner
{
    private static Logger LOG = LoggerFactory.getLogger(CommandLineHandler.class);

    @Inject
    private FolderImportManager importManager;

    private  CommandLineHandler()
    {
    }

    @Override
    public void run(String... args) throws Exception
    {
        LOG.info("handling command line arguments");

        Options options = new Options();
        options.addOption("i", "import", true, "folder to be imported");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse( options, args);

        for(String arg:cmd.getArgList())
        {
            LOG.info(" argument [{}]", arg);
        }

        if (cmd.hasOption("i"))
        {
            Path folder = Paths.get(cmd.getOptionValue("i"));

            if (Files.exists(folder))
            {
                LOG.info("trigger import for [{}]", folder.toAbsolutePath().toString());
                importManager.addImport(folder);
            }
        }
    }

}
