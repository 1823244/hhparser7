package com.ens.hhparser5.restcontroller;

import com.ens.hhparser5.service.VacancySourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("vacancysource")
public class VacancySourceController {

    @Autowired
    private VacancySourceService vacancySourceService;

    @GetMapping("{hhid}")
    public String showVacancySource(@PathVariable (name = "hhid") String hhid){
        String json = vacancySourceService.getVacancySourceJson(hhid);
        return json;
    }

}
