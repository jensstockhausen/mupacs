package de.famst.controller;

import de.famst.data.SeriesEty;
import de.famst.data.SeriesRepository;
import jakarta.inject.Inject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jens on 10/10/2016.
 */
@Controller
public class SeriesListController
{
    @Inject
    SeriesRepository seriesRepository;

    private SeriesListController()
    {
    }

    @RequestMapping(value = "/serieslist", method = RequestMethod.GET)
    public String getListOfSeriesForStudy(@RequestParam("studyId") long studyId, Model model)
    {
        List<SeriesEty> seriesEtyList = seriesRepository.findByStudyId(studyId);

        List<SeriesModel> series = new ArrayList<>();

        seriesEtyList.forEach(seriesEty ->
            series.add(SeriesModel.fromSeriesEty(seriesEty))
        );

        model.addAttribute("series", series);

        return "seriesList";
    }


}
