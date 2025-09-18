package com.ens.hhparser5.controller;

import com.ens.hhparser5.configuration.AppConfig;
import com.ens.hhparser5.dto.ProjectDto;
import com.ens.hhparser5.dto.TaskDto;
import com.ens.hhparser5.model.*;
import com.ens.hhparser5.service.*;
import com.ens.hhparser5.ui.Navbar;
import com.ens.hhparser5.ui.NavbarItem;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.security.Principal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Spring MVC web-controller for Thymeleaf templates
 */
@Controller
@RequestMapping("projects")
public class ProjectsController {

    private final Logger logger = LoggerFactory.getLogger(ProjectsController.class);

    @Autowired
    private Navbar navbar;
    @Autowired
    private AppConfig appConfig;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private VacancyService vacancyService;
    @Autowired
    private SearchTextService searchTextService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private UserService userService;

    @ModelAttribute("navbarItems")
    private List<NavbarItem> navbarItems(){
        return navbar.getItems();
    }
    @ModelAttribute("today")
    private Date getTime(){
        return Calendar.getInstance().getTime();
    }

    /**
     * Show the page with list of projects
     * @param model
     * @return
     */
    @GetMapping
    public ModelAndView listProjects(ModelMap model,
                                     //@AuthenticationPrincipal User user
                                     Principal principal
                                     ){
        //model.addAttribute("navbarItems", navbar.getItems());
        //model.addAttribute("today", Calendar.getInstance().getTime());
        model.addAttribute("pageTitle", "Project's list");

        User user = userService.getPrincipalUser(principal);
        List<Project> projects = projectService.findAll(user);

        // List of Maps нужен, чтобы хранить количество вакансий в разрезе проектов
        // в виде
        //  * проект - это Map (элемент списка), далее поля мэпы:
        //      ** project - объект проекта
        //      ** total_vacs - число, количество вакансий по проекту
        List<Map<String,Object>> totalsByProject = new ArrayList<>();

        // todo потом оптимизировать - убрать запрос в цикле
        //  (можно написать отдельный, специально для этого контроллера)
        for (Project prj : projects){
            // этот Map содержит вакансии по одному проекту (плюс статистику)
            Map<String, Object> mapAllVacancies =
                    vacancyService.findAllOpenByProjectIdPagination(prj.getId(), 1);

            // дальше соберем статистику по количеству вакансий в проектах
            Map<String,Object> projectTotals = new HashMap<>();
            projectTotals.put("project",prj);
            projectTotals.put("total_vacs",mapAllVacancies.get("total"));
            totalsByProject.add(projectTotals);
        }

        model.addAttribute("itemsList", totalsByProject);

        Task task = taskService.obtainLastTask(user);
        if (task == null){
            task = new Task();
            task.setName("You didn't run any task yet.");
        }
        model.addAttribute("task", task);

        return new ModelAndView("projects", model);
    }


    /**
     * Shows the page with details of one project
     * @param model
     * @param id
     * @return
     */
    @GetMapping("{id}")
    private String showProject(Model model, @PathVariable long id, Principal principal){

        //model.addAttribute("today", Calendar.getInstance().getTime());
        model.addAttribute("currentDate",java.sql.Date.valueOf(LocalDate.now()));

        Project project = projectService.findById(id);
        User user = userService.getPrincipalUser(principal);
        if (project.getUserId() != user.getId()) {
            throw new RuntimeException("You are trying to access the project that doesn't belong to you!");
        }

        model.addAttribute("project", project);

        // Для универсального заполнения таблицы атрибутов будем использовать Map
        Map<String, String> attr = new HashMap<>();
        attr.put("name", project.getName());
        attr.put("id", String.valueOf(project.getId()));

        // entrySet() проще обойти в шаблоне
        model.addAttribute("attributesMap", attr.entrySet());

        model.addAttribute("pageTitle", "Project: "+project.getName());

        // Навбар нужно заполнять для каждой страницы (возможно, потом применим иной подход)
        List<NavbarItem> navbarItems = new Navbar().getItems();
        model.addAttribute("navbarItems", navbarItems);

        // Получаем статистику количества вакансий в разрезе строк поиска
        // todo В будущем сделать один запрос вида SELECT searchText, Count(vacs) GROUP BY searchText
        //
        // сейчас это список мэпов формата: SearchTextDto-КоличествоВакансий
        // можно в принципе и туда добавить этот метод, но хочется выполнить один запрос,
        // который посчитает открытые вакансии по всем строка поиска, а не гонять запрос в цикле
        //model.addAttribute("searchTextsList", searchTextRepo.findAllByProject(project));
        List<Map<String,Object>> vacanciesCountBySearchText = new ArrayList<>();
        List<SearchText> searchTextsList = searchTextService.findAllByProject(project);
        for (SearchText oneSearchText:searchTextsList) {
            List<OpenVacancy> listAllVacancies = vacancyService.findAllOpenByProjectIdAndSearchTextId(project.getId(), oneSearchText.getId());
            Map<String,Object> oneSearchTextStatistics = new HashMap<>();
            oneSearchTextStatistics.put("searchTextDto",oneSearchText);
            oneSearchTextStatistics.put("vacCount",listAllVacancies.size());
            vacanciesCountBySearchText.add(oneSearchTextStatistics);
        }
        model.addAttribute("searchTextsList", vacanciesCountBySearchText);

        Task task = taskService.obtainLastTask(user);
        if (task == null){
            task = new Task();
            task.setName("You didn't run any task yet.");
        }
        model.addAttribute("task", task);

        // Получим все вакансии по проекту - только для статистики по оплатам в шапке
        Map<String, Object> mapAllVacancies = vacancyService.findAllOpenByProjectIdPagination(project.getId(), 1);

        // дальше соберем статистику по количеству вакансий в нескольких разрезах оплат
        model.addAttribute("total_vacs", mapAllVacancies.get("total"));
        model.addAttribute("over500", mapAllVacancies.get("over500"));
        model.addAttribute("over400", mapAllVacancies.get("over400"));
        model.addAttribute("over350", mapAllVacancies.get("over350"));
        model.addAttribute("over300", mapAllVacancies.get("over300"));
        model.addAttribute("over250", mapAllVacancies.get("over250"));
        model.addAttribute("over200", mapAllVacancies.get("over200"));
        model.addAttribute("hiddensalary", mapAllVacancies.get("hiddensalary"));

        // Получим новые вакансии на ближайшую к "сегодня" дату в прошлом.
        List<OpenVacancy> newVacanciesList = vacancyService
                .findNewVacanciesForToday(project, java.sql.Date.valueOf(LocalDate.now()));
        model.addAttribute("newVacanciesList", newVacanciesList);
        // Получим последнюю дату, на которую есть открытые вакансии. Это не обязательно "сегодня".
        model.addAttribute("dateOfNewVacancies",
                vacancyService.getDateOfNewVacancies(project, java.sql.Date.valueOf(LocalDate.now())));
        // Аналогично с закрытыми "сегодня" вакансиями.
        List<OpenVacancy> closedVacanciesList = vacancyService
                .findClosedVacanciesForToday(project, java.sql.Date.valueOf(LocalDate.now()));
        model.addAttribute("closedVacanciesList", closedVacanciesList);

        return "project";
    }

    /**
     *  Shows the page with open vacancies by one project (now with pagination)
     * @param model
     * @param project_id
     * @param pageNumber
     * @return
     */
    @GetMapping("openvacs/{project_id}")
    public ModelAndView openVacs(ModelMap model, @PathVariable long project_id, @RequestParam("page") int pageNumber){
        model.addAttribute("navbarItems", navbar.getItems());

        logger.info("page number from @RequestParam = {}", pageNumber);

        model.addAttribute("today", Calendar.getInstance().getTime());

        Project project = projectService.findById(project_id);
        model.addAttribute("pageTitle", "Open vacancies by project: "+project.getName());
        model.addAttribute("pageNumber", pageNumber);
        model.addAttribute("pageNumberText", "current page is "+pageNumber);

        //List<OpenVacancyDto> vacs = vacancyService.findAllOpenByProjectId(project_id);
        // если pageNumber выходит за пределы существующих страниц, то в этой мэпе
        // не будет вакансий, но все равно будет статистика, которую мы используем
        // для вычисления показателя "total pages"
        Map<String, Object> mapAllVacancies = vacancyService.findAllOpenByProjectIdPagination(project_id, pageNumber);
        List<OpenVacancy> vacanciesForOnePage = (List<OpenVacancy>) mapAllVacancies.get("vacs");

        int totalPages = (Integer)mapAllVacancies.get("total")/appConfig.getPagination();
        int residual = (Integer)mapAllVacancies.get("total")%appConfig.getPagination();
        if (residual != 0) {
            totalPages++;
        }

        // дальше соберем статистику по количеству вакансий в нескольких разрезах оплат
        model.addAttribute("total_vacs", mapAllVacancies.get("total"));
        model.addAttribute("over500", mapAllVacancies.get("over500"));
        model.addAttribute("over400", mapAllVacancies.get("over400"));
        model.addAttribute("over350", mapAllVacancies.get("over350"));
        model.addAttribute("over300", mapAllVacancies.get("over300"));
        model.addAttribute("over250", mapAllVacancies.get("over250"));
        model.addAttribute("over200", mapAllVacancies.get("over200"));
        model.addAttribute("hiddensalary", mapAllVacancies.get("hiddensalary"));

        model.addAttribute("total_pages", totalPages);

        model.addAttribute("vacanciesList", vacanciesForOnePage);
        model.addAttribute("project_id", project_id);
        model.addAttribute("projectDto", project);

        return new ModelAndView("openvacancies", model);
    }

    /**
     * Show the page with open vacancies by one project without pagination
     * @param model
     * @param project_id
     * @return
     */
    @GetMapping("openvacs/nopagination/{project_id}")
    public ModelAndView openVacsNoPag(ModelMap model, @PathVariable long project_id){
        model.addAttribute("navbarItems", navbar.getItems());

        model.addAttribute("today", Calendar.getInstance().getTime());

        Project project = projectService.findById(project_id);
        model.addAttribute("pageTitle", "Open vacancies by project: "+project.getName());

        //List<OpenVacancyDto> vacs = vacancyService.findAllOpenByProjectId(project_id);
        // если pageNumber выходит за пределы существующих страниц, то в этой мэпе
        // не будет вакансий, но все равно будет статистика, которую мы используем
        // для вычисления показателя "total pages"
        final java.sql.Date currentDate = java.sql.Date.valueOf(LocalDate.now());
        List<OpenVacancy> allVacancies = vacancyService.findAllOpenByProjectId(project_id, currentDate);

        // дальше соберем статистику по количеству вакансий в нескольких разрезах оплат
        model.addAttribute("total_vacs", allVacancies.size());
        model.addAttribute("over500", allVacancies.stream().filter((vac)-> vac.getSalary_netto() >= 500000)
                .collect(Collectors.toList()).size());
        model.addAttribute("over400", allVacancies.stream().filter((vac)-> vac.getSalary_netto() >= 400000)
                .collect(Collectors.toList()).size());
        model.addAttribute("over350", allVacancies.stream().filter((vac)-> vac.getSalary_netto() >= 350000)
                .collect(Collectors.toList()).size());
        model.addAttribute("over300", allVacancies.stream().filter((vac)-> vac.getSalary_netto() >= 300000)
                .collect(Collectors.toList()).size());
        model.addAttribute("over250", allVacancies.stream().filter((vac)-> vac.getSalary_netto() >= 250000)
                .collect(Collectors.toList()).size());
        model.addAttribute("over200", allVacancies.stream().filter((vac)-> vac.getSalary_netto() >= 200000)
                .collect(Collectors.toList()).size());
        model.addAttribute("hiddensalary", allVacancies.stream().filter((vac)-> vac.getSalary_netto() == 0)
                .collect(Collectors.toList()).size());

        model.addAttribute("itemsList", allVacancies);
        model.addAttribute("project_id", project_id);
        model.addAttribute("projectDto", project);

        return new ModelAndView("openvacanciesnopagination", model);
    }

    /**
     * Shows the page where we create a new project
     * @param model
     * @return
     */
    @GetMapping("new")
    public String getFormForNewProject(Model model,
                                       @ModelAttribute("project") ProjectDto projectDto){
        //model.addAttribute("project", new ProjectDto());
        model.addAttribute("pageTitle", "Create new project");
        return "project-new";
    }

    /**
     * Handler of the page where we create a new project (see @GetMapping("new))
     * @param projectDto
     * @param principal
     * @return
     */
    @PostMapping
    public String create(@ModelAttribute("project") ProjectDto projectDto,
                         //@AuthenticationPrincipal User user
                         Principal principal){
        User user = userService.getPrincipalUser(principal);
        Project project = new Project();
        project.setUserId(user.getId());
        project.setName(projectDto.getName());
        projectService.save(project);
        if (projectDto.getSearchText() != null && !projectDto.getSearchText().isEmpty()){
            SearchText searchText = new SearchText();
            searchText.setProjectId(project.getId());
            searchText.setText(projectDto.getSearchText());
            searchTextService.save(searchText, project);
        }
        //return "project-create-success";
        return "redirect:/projects";
    }

    /**
     * Shows the page where we can change project's parameters (EDIT project)
     * @param model
     * @param id
     * @return
     */
    @GetMapping("{id}/edit")
    public String editProject(Model model, @PathVariable("id") long id){
        Project project = projectService.findById(id);
        model.addAttribute("project", project);
        model.addAttribute("searchTextsList", searchTextService.findAllByProject(project));
        return "project-edit";
    }

    /**
     * Handler of EDIT-page (see @GetMapping("{id}/edit"))
     * @param project
     * @return
     */
    @PatchMapping("/{id}")
    public String update(@ModelAttribute("project") Project project){
        projectService.update(project);
        return "redirect:/projects";
    }

    /**
     * Delete the project - handler of the project's details page (see @GetMapping("{id}"))
     * @param project
     * @return
     */
    @DeleteMapping("{id}")
    //public String delete(@PathVariable("id") long id){
    public String delete(@ModelAttribute("project") Project project){
        //projectService.delete(projectService.findById(id));
        projectService.delete(project);
        return "redirect:/projects";
    }

    /**
     * Handler of button Search on the project's details page (see @GetMapping("{id}"))
     * @param project_id
     * @return
     */
    @PostMapping("{id}/search")
    public String searchProject(@PathVariable("id") long project_id, Principal principal){

        User user = userService.getPrincipalUser(principal);

        new Thread(
                () -> {
                    try {
                        projectService.searchOneProject(projectService.findById(project_id), user);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }
        ).start();

        //return "project-create-success";
        return "redirect:/projects/"+project_id;
    }

    /**
     * Handler of button <Search ALL> on the project's list page (see @GetMapping())
     * @return
     */
    @PostMapping("search")
    public String searchAllProjects(Principal principal){
        User user = userService.getPrincipalUser(principal);

        new Thread(
                () -> {
                    projectService.searchAllProjects(user);
                }
        ).start();

        //return "project-create-success";
        return "redirect:/projects";
    }

    /**
     * Shows the page with all new vacancies for a project
     * @param model
     * @param project_id
     * @return
     */
    @GetMapping("newvacancies/{project_id}")
    public ModelAndView allNewVacancies(ModelMap model, @PathVariable long project_id){
        model.addAttribute("navbarItems", navbar.getItems());
        model.addAttribute("today", Calendar.getInstance().getTime());

        Project project = projectService.findById(project_id);
        model.addAttribute("pageTitle", "New vacancies for project: " + project.getName());
        model.addAttribute("project", project);

        // Get all new vacancies for today
        List<OpenVacancy> newVacanciesList = vacancyService
                .findNewVacanciesForToday(project, java.sql.Date.valueOf(LocalDate.now()));
        model.addAttribute("newVacanciesList", newVacanciesList);
        
        // Get the date of new vacancies
        model.addAttribute("dateOfNewVacancies",
                vacancyService.getDateOfNewVacancies(project, java.sql.Date.valueOf(LocalDate.now())));

        return new ModelAndView("newvacancies", model);
    }

    /**
     * Shows the page with all closed vacancies for a project
     * @param model
     * @param project_id
     * @return
     */
    @GetMapping("closedvacancies/{project_id}")
    public ModelAndView allClosedVacancies(ModelMap model, @PathVariable long project_id){
        model.addAttribute("navbarItems", navbar.getItems());
        model.addAttribute("today", Calendar.getInstance().getTime());

        Project project = projectService.findById(project_id);
        model.addAttribute("pageTitle", "Closed vacancies for project: " + project.getName());
        model.addAttribute("project", project);

        // Get all closed vacancies for today
        List<OpenVacancy> closedVacanciesList = vacancyService
                .findClosedVacanciesForToday(project, java.sql.Date.valueOf(LocalDate.now()));
        model.addAttribute("closedVacanciesList", closedVacanciesList);
        
        // Get the date of new/closed vacancies
        model.addAttribute("dateOfNewVacancies",
                vacancyService.getDateOfNewVacancies(project, java.sql.Date.valueOf(LocalDate.now())));

        return new ModelAndView("closedvacancies", model);
    }
}
