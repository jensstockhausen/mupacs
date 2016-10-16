package de.famst.dcm;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.io.DicomInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by jens on 03/10/2016.
 */
public class DcmFile
{
    private static Logger LOG = LoggerFactory.getLogger(DcmFile.class);

    public static Attributes readContent(File file)
    {
        DicomInputStream dis;
        Attributes dcm;

        try
        {
            LOG.info("opening file {}", file.getAbsolutePath());

            dis = new DicomInputStream(file);
        }
        catch (IOException e)
        {
            LOG.error("reading dicom file {}", e.getMessage());

            return null;
        }

        dis.setIncludeBulkData(DicomInputStream.IncludeBulkData.NO);

        try
        {
            dcm = dis.readDataset(-1, -1);
            dis.close();


        }
        catch (IOException e)
        {
            LOG.error("parsing dicom file {}", e.getMessage());

            return null;
        }

        return dcm;
    }

    public static boolean isDCMFile(File file)
    {
        FileInputStream inStream;

        try
        {
            inStream = new FileInputStream(file);
        }
        catch (FileNotFoundException e)
        {
            LOG.warn("file does not exist");
            return false;
        }

        byte[] tag = new byte[] { 'D', 'I', 'C', 'M'};
        byte[] buffer = new byte[4];

        try
        {
            inStream.skip(128);
            inStream.read(buffer);
            inStream.close();
        }
        catch (IOException e)
        {
            LOG.warn("error reading file {}", e.getMessage());
            return false;
        }

        for (int i = 0; i<4; i++)
        {
            if (buffer[i] != tag[i])
            {
                return false;
            }
        }

        return true;
    }






}
