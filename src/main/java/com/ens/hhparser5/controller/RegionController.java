package com.ens.hhparser5.controller;

import com.ens.hhparser5.service.RegionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.AccessType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.sql.SQLException;

@Controller
@RequestMapping("regions")
public class RegionController {

    @Autowired
    private RegionService regionService;

    @GetMapping
    public String showMain(){
        return "regions";
    }

    @PostMapping
    public String doImport() throws SQLException, JsonProcessingException {
        regionService.doImport();
        return "redirect:/regions";
    }
}
