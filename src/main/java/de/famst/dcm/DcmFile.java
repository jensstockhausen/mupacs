package de.famst.dcm;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.io.DicomInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by jens on 03/10/2016.
 */
public class DcmFile
{
    private static Logger LOG = LoggerFactory.getLogger(DcmFile.class);

    private DcmFile()
    {

    }

    public static Attributes readContent(File file)
    {
        Attributes dcm;

        LOG.info("opening DICOM file {}", file.getAbsolutePath());

        try( DicomInputStream dis = new DicomInputStream(file); )
        {
            dis.setIncludeBulkData(DicomInputStream.IncludeBulkData.NO);
            dcm = dis.readDataset(-1, -1);
            dis.close();
        }
        catch (IOException e)
        {
            LOG.error("reading DICOM file [{}]", e);
            return null;
        }

        return dcm;
    }

    public static boolean isDCMFile(File file)
    {
        try (FileInputStream inStream = new FileInputStream(file))
        {
            byte[] tag = new byte[]{'D', 'I', 'C', 'M'};
            byte[] buffer = new byte[4];

            if (128 != inStream.skip(128))
            {
                LOG.error("reading bytes");
                return false;
            }

            if (4 != inStream.read(buffer))
            {
                LOG.error("reading bytes");
                return false;
            }

            inStream.close();

            for (int i = 0; i < 4; i++)
            {
                if (buffer[i] != tag[i])
                {
                    return false;
                }
            }

        }
        catch (IOException e)
        {
            LOG.warn("error reading file [{}]", e);
            return false;
        }

        return true;
    }


}
