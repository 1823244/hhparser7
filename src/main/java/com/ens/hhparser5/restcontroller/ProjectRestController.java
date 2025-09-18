package com.ens.hhparser5.restcontroller;

import com.ens.hhparser5.model.Project;
import com.ens.hhparser5.model.User;
import com.ens.hhparser5.repository.ProjectRepo;
import com.ens.hhparser5.service.ProjectService;
import com.ens.hhparser5.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;

@SuppressWarnings(value = "unused")
@RestController
@RequestMapping("project")
public class ProjectRestController {

    @Autowired
    private ProjectService projectService;
    @Autowired
    private ProjectRepo projectRepo;
    @Autowired
    private UserService userService;


    @GetMapping("add/{name}")
    public String addProject(@PathVariable String name){
        Project project = new Project();
        project.setName(name);
        projectRepo.save(project);
        return "created: "+name;
    }

    /**
     *  process all projects
     */
    @GetMapping("searchall")
    public String searchAll(@AuthenticationPrincipal User user){
        String st =  "search started at "+ LocalDateTime.now().toString();
        Thread thread = new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        projectService.searchAllProjects(user);
                    }
                }
        );
        thread.start();

        return st+"\nsearch finished at "+ LocalDateTime.now().toString();
    }

    /**
     *  process one project
     */
    @GetMapping("search/{projectname}")
    public String searchOneProject(@PathVariable("projectname") String projectName, Principal principal){
        String st =  "search started at "+ LocalDateTime.now().toString();
        User user = userService.getPrincipalUser(principal);

        Thread thread = new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            projectService.searchOneProject(projectRepo.findByName(projectName), user);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
        );
        thread.start();
        return st+"\nsearch finished at "+ LocalDateTime.now().toString();
    }
    @PostMapping("searchone")
    public void searchOneAjax(Principal principal, @RequestBody String json){
        User user = userService.getPrincipalUser(principal);

        System.out.println(json);

    }

}
