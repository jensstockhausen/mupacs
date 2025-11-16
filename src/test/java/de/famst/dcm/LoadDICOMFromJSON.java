package de.famst.dcm;

import jakarta.json.Json;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.json.JSONReader;

import java.io.InputStream;

/**
 * Created by jens on 27/11/2016.
 */
public class LoadDICOMFromJSON
{
  public LoadDICOMFromJSON()
  {
  }

  public Attributes fromResource(String resource)
  {
    ClassLoader classLoader = getClass().getClassLoader();
    InputStream inputStream = classLoader.getResourceAsStream(resource);

    JSONReader reader = new JSONReader(Json.createParser(inputStream));

    return reader.readDataset(null);
  }

}
