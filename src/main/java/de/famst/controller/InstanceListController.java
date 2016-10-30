package de.famst.controller;

import de.famst.data.InstanceEty;
import de.famst.data.InstanceRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.inject.Inject;
import javax.ws.rs.GET;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jens on 10/10/2016.
 */
@Controller
public class InstanceListController
{
    @Inject
    InstanceRepository instanceRepository;

    private InstanceListController()
    {
    }

    @GET
    @RequestMapping("/instancelist")
    public String getListOfSeriesForStudy(@RequestParam("seriesId") long seriesId, Model model)
    {
        List<InstanceEty> instanceEtyList = instanceRepository.findBySeriesId(seriesId);

        List<InstanceModel> instances = new ArrayList<>();

        instanceEtyList.forEach(instanceEty ->
                instances.add(InstanceModel.fromInstanceEty(instanceEty))
        );

        model.addAttribute("instances", instances);

        return "instanceList";
    }


}
