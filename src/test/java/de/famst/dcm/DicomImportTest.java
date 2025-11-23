package de.famst.dcm;

import de.famst.data.InstanceEty;
import de.famst.data.PatientEty;
import de.famst.data.SeriesEty;
import de.famst.data.StudyEty;
import org.dcm4che3.data.Attributes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Created by jens on 08/10/2016.
 */
public class DicomImportTest
{
    private DicomReader dicomReader;
    private Attributes dcm;

    @BeforeEach
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

        assertThat(patientEty.getPatientName(),
            is(equalTo("MAGIX^NAME")));
    }

    @Test
    public void canCreateStudyEtyFromDicom() throws Exception
    {
        StudyEty studyEty = dicomReader.readStudy(dcm);

        assertThat(studyEty, is(notNullValue()));
        assertThat(studyEty.getStudyInstanceUID(),
            is(equalTo("2.16.840.1.113669.632.20.1211.10000329900")));
    }

    @Test
    public void canCreateSeriesEtyFromDicom() throws Exception
    {
        SeriesEty seriesEty = dicomReader.readSeries(dcm);

        assertThat(seriesEty, is(notNullValue()));
        assertThat(seriesEty.getSeriesInstanceUID(),
            is(equalTo("1.3.12.2.1107.5.1.4.54693.30000006102508593206200000001")));
    }

    @Test
    public void canCreateInstanceEtyFromDicom() throws Exception
    {
        InstanceEty instanceEty = dicomReader.readInstance(dcm);

        assertThat(instanceEty, is(notNullValue()));
        assertThat(instanceEty.getInstanceUID(),
            is(equalTo("1.3.12.2.1107.5.1.4.54693.30000006101906583670300011284")));
    }
}
