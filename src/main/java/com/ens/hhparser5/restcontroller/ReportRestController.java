package com.ens.hhparser5.restcontroller;

import com.ens.hhparser5.service.VacanciesListJson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SuppressWarnings(value = "unused")
@RestController
@RequestMapping("report")
public class ReportRestController {

    @Autowired
    private VacanciesListJson vacanciesListJson;

    //https://qna.habr.com/q/1084786
    // If "produces" is not specified - cyrillyc text will be looking like "????????????"
    @GetMapping(value="vacflatlistbyproject/{projectname}/{reportdate}",
            produces = "text/json; charset=utf-8")
    public String vacFlatListByProject(
            @PathVariable("projectname") String projectName,
            @PathVariable("reportdate") String reportDate) {
        return vacanciesListJson.vacListByDateAndProject(projectName, java.sql.Date.valueOf(reportDate));
    }
}
