package de.famst.service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jens on 03/10/2016.
 */
public class FolderImportInformation
{
    private Path rootPath;
    private List<String> messages;

    public FolderImportInformation(Path rootPath)
    {
        this.rootPath = rootPath;
        messages = new ArrayList<>();
    }

    public void addInfo(String message)
    {
        messages.add(message);
    }

    public Path getRootPath()
    {
        return rootPath;
    }

    public List<String> getMessages()
    {
        return messages;
    }

}
