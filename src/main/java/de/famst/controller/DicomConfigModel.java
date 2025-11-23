package de.famst.controller;

/**
 * Model for displaying DICOM service configuration
 */
public class DicomConfigModel
{
    private final String aeTitle;
    private final String host;
    private final int port;
    private final boolean isRunning;
    private final String deviceName;
    private final int registeredServices;

    public DicomConfigModel(String aeTitle, String host, int port, boolean isRunning,
                           String deviceName, int registeredServices)
    {
        this.aeTitle = aeTitle;
        this.host = host;
        this.port = port;
        this.isRunning = isRunning;
        this.deviceName = deviceName;
        this.registeredServices = registeredServices;
    }

    public String getAeTitle()
    {
        return aeTitle;
    }

    public String getHost()
    {
        return host;
    }

    public int getPort()
    {
        return port;
    }

    public boolean isRunning()
    {
        return isRunning;
    }

    public String getDeviceName()
    {
        return deviceName;
    }

    public int getRegisteredServices()
    {
        return registeredServices;
    }

    public String getConnectionString()
    {
        return host + ":" + port;
    }

    public String getStatusLabel()
    {
        return isRunning ? "Running" : "Stopped";
    }

    public String getStatusClass()
    {
        return isRunning ? "label-success" : "label-default";
    }
}

