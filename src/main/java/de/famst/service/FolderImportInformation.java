package de.famst.service;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Container for tracking DICOM folder import operations.
 * Stores the root path being imported and collects messages (typically SOP Instance UIDs)
 * for all successfully imported DICOM instances.
 *
 * <p>This class is thread-safe and can be used in concurrent import operations.
 * The messages list uses a concurrent implementation to handle multiple threads
 * adding import results simultaneously.
 *
 * @author jens
 * @since 2016-10-03
 */
public class FolderImportInformation
{
    private final Path rootPath;
    private final List<String> messages;
    private final LocalDateTime startTime;

    /**
     * Constructs a new FolderImportInformation for the specified root path.
     *
     * @param rootPath the root path of the folder being imported
     * @throws IllegalArgumentException if rootPath is null
     */
    public FolderImportInformation(Path rootPath)
    {
        if (rootPath == null)
        {
            throw new IllegalArgumentException("Root path cannot be null");
        }

        this.rootPath = rootPath;
        this.messages = new CopyOnWriteArrayList<>();
        this.startTime = LocalDateTime.now();
    }

    /**
     * Adds an informational message (typically a SOP Instance UID) to the import results.
     *
     * <p>This method is thread-safe and can be called concurrently by multiple threads.
     *
     * @param message the message to add, typically a SOP Instance UID of an imported instance
     * @throws IllegalArgumentException if message is null or empty
     */
    public synchronized void addInfo(String message)
    {
        if (message == null || message.trim().isEmpty())
        {
            throw new IllegalArgumentException("Message cannot be null or empty");
        }

        messages.add(message);
    }

    /**
     * Returns the root path being imported.
     *
     * @return the root path, never null
     */
    public Path getRootPath()
    {
        return rootPath;
    }

    /**
     * Returns an unmodifiable view of the messages collected during the import.
     *
     * <p>The returned list is a snapshot and will not reflect subsequent additions.
     *
     * @return an unmodifiable list of messages, never null but may be empty
     */
    public List<String> getMessages()
    {
        return List.copyOf(messages);
    }

    /**
     * Returns the number of messages (imported instances) collected so far.
     *
     * @return the count of messages
     */
    public int getMessageCount()
    {
        return messages.size();
    }

    /**
     * Returns the timestamp when this import operation was started.
     *
     * @return the start time, never null
     */
    public LocalDateTime getStartTime()
    {
        return startTime;
    }

    /**
     * Checks if any messages have been collected.
     *
     * @return true if at least one message has been added, false otherwise
     */
    public boolean hasMessages()
    {
        return !messages.isEmpty();
    }

    /**
     * Clears all collected messages.
     * This method is useful for reusing the import information object.
     */
    public synchronized void clearMessages()
    {
        messages.clear();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FolderImportInformation that = (FolderImportInformation) o;
        return Objects.equals(rootPath, that.rootPath);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(rootPath);
    }

    @Override
    public String toString()
    {
        return "FolderImportInformation{" + "rootPath=" + rootPath + ", messageCount=" + messages.size() + ", startTime=" + startTime + '}';
    }

}
