package de.famst.dcm;

import de.famst.data.PatientEty;
import org.dcm4che3.data.Attributes;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by jens on 08/10/2016.
 */
public class DicomImportTest
{

  private DicomReader dicomReader;
  private Attributes dcm;

  @Before
  public void setUp() throws Exception
  {
    dicomReader = new DicomReader();

    LoadDICOMFromJSON loadDICOMFromJSON = new LoadDICOMFromJSON();
    dcm = loadDICOMFromJSON.fromResource("dcm.json");
  }

  @Test
  public void canCreatePatientEtyFromDicom() throws Exception
  {
    PatientEty patientEty = dicomReader.readPatient(dcm);

    assertThat(patientEty.getPatientName(), is(equalTo("MAGIX^NAME")));
  }
}
