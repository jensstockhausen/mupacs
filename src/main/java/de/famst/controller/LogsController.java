package de.famst.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Controller for displaying log files
 */
@Controller
public class LogsController
{
    private static final Logger LOG = LoggerFactory.getLogger(LogsController.class);
    private static final String LOG_FILE_PATH = "./log/mupacs.log";
    private static final int DEFAULT_LINES = 500; // Number of lines to display

    @GetMapping("/logs")
    public String getLogs(Model model)
    {
        model.addAttribute("lines", DEFAULT_LINES);
        return "logs";
    }

    /**
     * REST endpoint to fetch the latest log lines as plain text
     *
     * @param lines number of lines to fetch
     * @return log content as plain text
     */
    @GetMapping("/logs/content")
    @ResponseBody
    public String getLogContent(@RequestParam(defaultValue = "500") int lines)
    {
        try
        {
            Path logPath = Paths.get(LOG_FILE_PATH);

            if (!Files.exists(logPath))
            {
                return "Log file not found: " + LOG_FILE_PATH;
            }

            List<String> logLines = readLastLines(logPath, lines);
            return String.join("\n", logLines);
        }
        catch (Exception e)
        {
            LOG.error("Error reading log file: {}", e.getMessage(), e);
            return "Error reading log file.";
        }
    }

    /**
     * Reads the last N lines from a file efficiently using RandomAccessFile
     *
     * @param path the path to the log file
     * @param numberOfLines number of lines to read from the end
     * @return list of log lines (in correct order, oldest to newest)
     * @throws IOException if file reading fails
     */
    private List<String> readLastLines(Path path, int numberOfLines) throws IOException
    {
        List<String> lines = new ArrayList<>();

        try (RandomAccessFile file = new RandomAccessFile(path.toFile(), "r"))
        {
            long fileLength = file.length();
            if (fileLength == 0)
            {
                return lines;
            }

            long position = fileLength - 1;
            int lineCount = 0;
            StringBuilder sb = new StringBuilder();

            // Read file backwards
            while (position >= 0 && lineCount < numberOfLines)
            {
                file.seek(position);
                int readByte = file.read();
                char c = (char) readByte;

                if (c == '\n')
                {
                    if (sb.length() > 0)
                    {
                        lines.add(sb.reverse().toString());
                        sb = new StringBuilder();
                        lineCount++;
                    }
                }
                else
                {
                    sb.append(c);
                }

                position--;
            }

            // Add the last line if we reached the beginning of the file
            if (sb.length() > 0)
            {
                lines.add(sb.reverse().toString());
            }

            // Reverse the list to get correct order (oldest to newest)
            Collections.reverse(lines);
        }

        return lines;
    }
}

