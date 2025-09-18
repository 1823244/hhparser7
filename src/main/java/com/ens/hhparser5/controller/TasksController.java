package com.ens.hhparser5.controller;

import com.ens.hhparser5.model.Task;
import com.ens.hhparser5.model.User;
import com.ens.hhparser5.service.TaskService;
import com.ens.hhparser5.service.UserService;
import com.ens.hhparser5.ui.Navbar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.security.Principal;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Spring MVC web-controller for Thymeleaf templates
 */
@Controller
@RequestMapping("tasks")
public class TasksController {

    @Autowired
    private UserService userService;

    private final Logger logger = LoggerFactory.getLogger(TasksController.class);

    @Autowired
    private Navbar navbar;
    @Autowired
    private TaskService taskService;


    @GetMapping
    public ModelAndView listE(ModelMap model, Principal principal){
        model.addAttribute("navbarItems", navbar.getItems());
        model.addAttribute("today", Calendar.getInstance().getTime());
        model.addAttribute("pageTitle", "Tasks list");

        User user = userService.getPrincipalUser(principal);
        List<Task> list = taskService.findAll(user);
        List<Task> descList = list.stream().sorted(
                (p,q)->{
                    //comparing second parameter with first to obtain descending order
                    return q.getStartTime().compareTo(p.getStartTime());
                }
        ).toList();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss");

        List<Map<String,String>> mapList = new ArrayList<>();
        for (Task task:descList) {
            Map<String,String> element = new HashMap<>();
            element.put("taskName", task.getName());
            element.put("taskId", String.valueOf(task.getId()));
            element.put("projectId", String.valueOf(task.getProjectId()));
            if (task.getProject() == null){
                element.put("projectName", "");
                element.put("projectId", "0");
            } else {
                element.put("projectName", task.getProject().getName());
                element.put("projectId", String.valueOf(task.getProject().getId()));
            }

            //String pattern = "yyyy-MMMM-dd HH:mm:ss";
            //SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

            String taskStart = "";

            if (task.getStartTime()!=null){
                taskStart = task.getStartTime().format(formatter);
            }

            element.put("startTime", taskStart);

            String taskEnd = "";
            if (task.getEndTime()!=null){
                taskEnd = task.getEndTime().format(formatter);
            }
            element.put("endTime", taskEnd);

            mapList.add(element);
        }

        model.addAttribute("itemsList", mapList);

        return new ModelAndView("tasks", model);
    }

}
