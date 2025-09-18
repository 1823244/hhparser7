package com.ens.hhparser5.controller;

import com.ens.hhparser5.model.Inwork;
import com.ens.hhparser5.model.Vacancy;
import com.ens.hhparser5.service.InworkService;
import com.ens.hhparser5.service.VacancyService;
import com.ens.hhparser5.ui.Navbar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Calendar;
import java.util.List;

@Controller
@RequestMapping("inwork")
public class InworkController {

    @Autowired
    private Navbar navbar;
    @Autowired
    private InworkService inworkService;
    @Autowired
    private VacancyService vacancyService;

    @GetMapping
    public String listInWork(Model model){
        model.addAttribute("navbarItems", navbar.getItems());
        model.addAttribute("today", Calendar.getInstance().getTime());
        model.addAttribute("pageTitle", "In work");

        List<Inwork> inworkList = inworkService.findAll();
        model.addAttribute("inworkList", inworkList);
        return "inworklist";
    }

    @PostMapping("{id}")
    public String toInwork(@PathVariable long id){
        Inwork inwork = new Inwork();
        inwork.setVacancyId(id);
        Vacancy vacancy = vacancyService.findById(id);
        inwork.setVacancyHhid(vacancy.getHhid());
        inworkService.save(inwork);
        return "redirect:/inwork";
    }

}
