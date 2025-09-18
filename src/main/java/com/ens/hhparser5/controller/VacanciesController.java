package com.ens.hhparser5.controller;

import com.ens.hhparser5.dto.VacancyDto;
import com.ens.hhparser5.mappers.VacancyToDtoMapper;
import com.ens.hhparser5.model.Employer;
import com.ens.hhparser5.model.Vacancy;
import com.ens.hhparser5.model.VacancySource;
import com.ens.hhparser5.service.*;
import com.ens.hhparser5.ui.Navbar;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Calendar;
import java.util.Map;

/**
 * Spring MVC web-controller for Thymeleaf templates
 */
@Controller
@RequestMapping("vacancies")
public class VacanciesController {

    private final Logger logger = LoggerFactory.getLogger(VacanciesController.class);

    @Autowired
    private Navbar navbar;
    @Autowired
    private VacancyService vacancyService;
    @Autowired
    private EmployerService employerService;
    @Autowired
    private BlackListService blackListService;
    @Autowired
    private HhruService hhruService;
    @Autowired
    private VacancySourceService vacancySourceService;



    @GetMapping("{hhid}")
    public ModelAndView showVacancy(ModelMap model, @PathVariable String hhid) throws IOException {

        model.addAttribute("navbarItems", navbar.getItems());
        model.addAttribute("today", Calendar.getInstance().getTime());
        model.addAttribute("pageTitle", "Vacancy "+hhid);
        Vacancy vacancy = vacancyService.findByHhid(hhid);
        VacancyDto vacancyDto = VacancyToDtoMapper.INSTANCE.toDto(vacancy);
        model.addAttribute("vacancyDto", vacancyDto);
        Employer employer = employerService.findById(vacancy.getEmployer());
        model.addAttribute("employerDto", employer);
        //check whether vacancy in blacklist or not
        Vacancy blacklistVacancy = blackListService.findById(vacancy.getId());
        if (blacklistVacancy == null){
            model.addAttribute("ToBlackListCaption", "To blacklist");
        } else {
            model.addAttribute("ToBlackListCaption", "Already in blacklist!");
        }
        Map<String,String> details = null;
                // first - search in VacancySource
        VacancySource vacancySource = vacancySourceService.findById(hhid);
        if (vacancySource == null) {
            // todo Pretty format of vacancy. Now - raw HTML code. Need - human readable rendered text
            details = hhruService.getVacancyDetailsByAPI(hhid);
        } else {
            // todo Pretty format of vacancy. Now - raw HTML code. Need - human readable rendered text
            details = vacancySourceService.getVacancyDetails(hhid);
        }
        model.addAttribute("details", details);
        //var pathTF = Files.createTempFile("details",".html").getFileName();
        //Files.write(pathTF, details.get("description").getBytes());
        //model.addAttribute("descrHtml", pathTF);
        model.addAttribute("descr", details.get("description"));

        return new ModelAndView("vacancy", model);
    }

}
