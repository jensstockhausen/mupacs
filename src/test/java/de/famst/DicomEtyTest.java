package de.famst;

import de.famst.data.InstanceEty;
import de.famst.data.InstanceRepository;
import de.famst.data.SeriesEty;
import de.famst.data.SeriesRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

import static de.famst.AssertException.ThrowableAssertion.assertThrown;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Created by jens on 08/10/2016.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class DicomEtyTest
{
    @Inject
    InstanceRepository instanceRepository;

    @Inject
    SeriesRepository seriesRepository;

    @Test
    public void can_insert_instance() throws Exception
    {
        InstanceEty instance  = new InstanceEty();
        instance.setInstanceUID("1.2.48.1234.1");

        InstanceEty instanceEtyStored = instanceRepository.save(instance);
        InstanceEty instanceEtyLoaded = instanceRepository.findOne(instanceEtyStored.getId());

        assertThat(instanceEtyStored.getInstanceUID(), is(equalTo(instance.getInstanceUID())));
        assertThat(instanceEtyLoaded.getInstanceUID(), is(equalTo(instance.getInstanceUID())));
    }


    @Test
    public void cannot_insert_same_instance_twice() throws Exception
    {
        InstanceEty instanceEtyA = new InstanceEty();
        instanceEtyA.setInstanceUID("1.2.3.4");

        InstanceEty instanceEtyB = new InstanceEty();
        instanceEtyB.setInstanceUID("1.2.3.4");

        instanceRepository.save(instanceEtyA);

        assertThrown(() -> instanceRepository.save(instanceEtyB))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @Transactional
    public void instances_are_added_to_series() throws Exception
    {
        SeriesEty series = new SeriesEty();
        series.setSeriesInstanceUID("1.2.3.4");
        series = seriesRepository.save(series);

        InstanceEty instanceEtyA = new InstanceEty();
        instanceEtyA.setInstanceUID("1.2.3.4.1");
        instanceEtyA.setSeries(series);
        instanceEtyA = instanceRepository.save(instanceEtyA);

        InstanceEty instanceEtyB = new InstanceEty();
        instanceEtyB.setInstanceUID("1.2.3.4.2");
        instanceEtyB.setSeries(series);
        instanceEtyB = instanceRepository.save(instanceEtyB);

        series.addInstance(instanceEtyA);
        series.addInstance(instanceEtyB);
        seriesRepository.save(series);

        InstanceEty i = instanceRepository.findOne(instanceEtyA.getId());
        assertThat(i.getSeries().getSeriesInstanceUID(), is(equalTo("1.2.3.4")));

        SeriesEty s = seriesRepository.findOne(series.getId());
        assertThat(s.getSeriesInstanceUID(), is(equalTo("1.2.3.4")));

        assertThat(s.getInstances(), hasSize(2));
        assertThat(s.getInstances(), hasItem(instanceEtyA));
        assertThat(s.getInstances(), hasItem(instanceEtyB));
    }


}
