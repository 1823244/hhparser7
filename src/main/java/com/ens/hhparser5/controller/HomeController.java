package com.ens.hhparser5.controller;

import com.ens.hhparser5.model.Role;
import com.ens.hhparser5.model.User;
import com.ens.hhparser5.service.UserService;
import com.ens.hhparser5.ui.Navbar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Spring MVC web-controller for Thymeleaf templates
 */
@Controller
public class HomeController {

    @Autowired
    private Navbar navbar;
    @Autowired
    private UserService userService;

    @GetMapping("/")
    public ModelAndView index(ModelMap model) {
        model.addAttribute("today", Calendar.getInstance().getTime());
        model.addAttribute("navbarItems", navbar.getItems());
        return new ModelAndView("index", model);
    }

    @GetMapping("login")
    public ModelAndView login (ModelMap model){
        return new ModelAndView("login", model);
    }

    @PostMapping("login")
    public String loginSuccessful(){
        return "redirect:/projects";
    }

    @PostMapping("logout")
    public ModelAndView logout(ModelMap model){
    return new ModelAndView("login", model);
    }

}
