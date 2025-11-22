package de.famst.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for the home/index page
 */
@Controller
public class HomeController
{
    @RequestMapping("/")
    public String index()
    {
        return "index";
    }
}

