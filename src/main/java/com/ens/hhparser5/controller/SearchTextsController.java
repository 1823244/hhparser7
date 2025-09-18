package com.ens.hhparser5.controller;

import com.ens.hhparser5.configuration.AppConfig;
import com.ens.hhparser5.model.OpenVacancy;
import com.ens.hhparser5.model.Project;
import com.ens.hhparser5.model.SearchText;
import com.ens.hhparser5.service.ProjectService;
import com.ens.hhparser5.service.SearchTextService;
import com.ens.hhparser5.service.VacancyService;
import com.ens.hhparser5.ui.Navbar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * Spring MVC web-controller for Thymeleaf templates
 */
@Controller
@RequestMapping("searchtexts")
public class SearchTextsController {

    private final Logger logger = LoggerFactory.getLogger(SearchTextsController.class);
    @Autowired
    private AppConfig appConfig;
    @Autowired
    private Navbar navbar;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private SearchTextService searchTextService;
    @Autowired
    private VacancyService vacancyService;

    /**
     * Открыть форму строки поиска - существующей
     * @param model
     * @param projectId
     * @return
     */
    @GetMapping("{sid}/project/{id}")
    public String getFormSearchText(Model model, @PathVariable("id") long projectId, @PathVariable("sid") long stextId){
        model.addAttribute("navbarItems", navbar.getItems());

        Project project = projectService.findById(projectId);
        model.addAttribute("project", project);

        SearchText searchText = searchTextService.findById(projectId, stextId);

        model.addAttribute("searchtext", searchText);
        model.addAttribute("pageTitle", "Search text");
        return "searchtext";
    }

    /**
     * Открыть форму создания новой строки поиска
     * @param model
     * @param projectId
     * @return
     */
    @GetMapping("new/project/{id}")
    public String getFormForNewSearchText(Model model, @PathVariable("id") long projectId){

        model.addAttribute("navbarItems", navbar.getItems());

        Project project = projectService.findById(projectId);
        model.addAttribute("project", project);

        SearchText searchText = new SearchText();
        searchText.setProjectId(projectId);

        model.addAttribute("searchtext", searchText);
        model.addAttribute("pageTitle", "Create new search text");
        return "searchtext-new";
    }

    /**
     * Создать новую строку поиска - обработчик формы создания нового
     * @param searchText
     * @param projectId
     * @return
     */
    @PostMapping("new/project/{id}")
    public String create(@ModelAttribute("searchtext") SearchText searchText, @PathVariable("id") long projectId){
        Project project = projectService.findById(projectId);
        searchTextService.save(searchText, project);
        //return "project-create-success";
        return "redirect:/projects/"+projectId;
    }

    /**
     * Удалить строку поиска
     * @param projectId
     * @param stId
     * @return
     */
    @DeleteMapping("{sid}/project/{id}")
    public String delete(@PathVariable("id") long projectId, @PathVariable("sid") long stId){
        searchTextService.delete(stId, projectId);
        return "redirect:/projects/"+projectId;
    }

    // открывается страница openvacanciesstext (которая теперь с пагинацией)
    @GetMapping("openvacs/{sid}/project/{id}")
    public ModelAndView openVacs(ModelMap model,
                                 @PathVariable("sid") long stextId,
                                 @PathVariable("id") long projectId,
                                 @RequestParam("page") int pageNumber){

        model.addAttribute("navbarItems", navbar.getItems());

        logger.info("page number from @RequestParam = {}", pageNumber);

        model.addAttribute("today", Calendar.getInstance().getTime());

        Project project = projectService.findById(projectId);
        SearchText searchText = searchTextService.findById(projectId, stextId);
        model.addAttribute("pageTitle", "Open vacancies by search text: "+ searchText.getText());
        model.addAttribute("pageNumber", pageNumber);
        model.addAttribute("pageNumberText", "current page is "+pageNumber);

        //List<OpenVacancyDto> vacs = vacancyService.findAllOpenByProjectId(project_id);
        // если pageNumber выходит за пределы существующих страниц, то в этой мэпе
        // не будет вакансий, но все равно будет статистика, которую мы используем
        // для вычисления показателя "total pages"
        Map<String, Object> mapAllVacancies = vacancyService.findAllOpenByProjectIdPaginationBySearchString(projectId, stextId, pageNumber);
        List<OpenVacancy> allVacancies = (List<OpenVacancy>) mapAllVacancies.get("vacs");

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

        model.addAttribute("itemsList", allVacancies);
        model.addAttribute("project_id", projectId);
        model.addAttribute("stext_id", stextId);
        model.addAttribute("stextDto", searchText);
        model.addAttribute("projectDto", project);

        return new ModelAndView("openvacanciesstext", model);
    }

}
