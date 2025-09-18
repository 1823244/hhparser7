package com.ens.hhparser5.controller;

import com.ens.hhparser5.model.OpenVacancy;
import com.ens.hhparser5.service.BlackListService;
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
@RequestMapping("blacklist")
public class BlackListController {
    @Autowired
    private Navbar navbar;
    @Autowired
    private BlackListService blService;
    @Autowired
    private VacancyService vacancyService;

    @PostMapping("add/{id}")
    public String addToBlackList(Model model,
                                 @PathVariable("id") long vacancyId){
        blService.addToBlackList(vacancyId);
        return "redirect:/vacancies/"+vacancyService.findById(vacancyId).getHhid();
    }

    @GetMapping
    public String showList(Model model){
        model.addAttribute("navbarItems", navbar.getItems());
        model.addAttribute("today", Calendar.getInstance().getTime());
        model.addAttribute("pageTitle", "Black list");
        List<OpenVacancy> fullList = blService.findAll();
        model.addAttribute("itemsList", fullList);
        return "blacklist";
    }

    @PostMapping("remove/{id}")
    public String removeItem(Model model,
                             @PathVariable("id") long vacancyId){
        blService.removeFromBlackList(vacancyId);
        return "redirect:/blacklist";
    }
}
