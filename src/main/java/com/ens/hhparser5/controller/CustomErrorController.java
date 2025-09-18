package com.ens.hhparser5.controller;

import com.ens.hhparser5.ui.Navbar;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;

@Controller
public class CustomErrorController implements ErrorController {

    @Autowired
    private Navbar navbar;

    @RequestMapping("error")
    public String errorHandler(Model model, HttpServletRequest request){
        model.addAttribute("navbarItems", navbar.getItems());
        model.addAttribute("today", Calendar.getInstance().getTime());
        model.addAttribute("pageTitle", "Error");

        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (status != null){
            Integer statusCode = Integer.valueOf(status.toString());

            if (statusCode == HttpStatus.SC_NOT_FOUND){
                return "error404";
            }
        }
        return "error";
    }
}
