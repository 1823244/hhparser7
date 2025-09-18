package com.ens.hhparser5.restcontroller;

import com.ens.hhparser5.dto.OpenVacancyDto;
import com.ens.hhparser5.service.VacancyService;
import com.ens.hhparser5.service.VacancySourceService;
import com.ens.hhparser5.model.OpenVacancy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Публичный REST API для получения открытых вакансий без авторизации
 */
@RestController
@RequestMapping("/api/public/vacancies")
@CrossOrigin(origins = "*")
public class PublicVacancyController {

    @Autowired
    private VacancyService vacancyService;
    
    @Autowired
    private VacancySourceService vacancySourceService;

    /**
     * Получить все открытые вакансии по проекту
     * @param projectId ID проекта
     * @param reportDate дата отчета (опционально, по умолчанию текущая дата)
     * @return список открытых вакансий
     */
    @GetMapping("/open/{projectId}")
    public ResponseEntity<List<OpenVacancyDto>> getOpenVacancies(
            @PathVariable Long projectId,
            @RequestParam(required = false) String reportDate) {
        
        Date sqlDate = null;
        if (reportDate != null && !reportDate.isEmpty()) {
            try {
                sqlDate = Date.valueOf(reportDate);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        }
        
        List<OpenVacancy> vacancies = vacancyService.findAllOpenByProjectId(projectId, sqlDate);
        List<OpenVacancyDto> vacancyDtos = vacancies.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(vacancyDtos);
    }

    /**
     * Получить новые вакансии за сегодня по проекту
     * @param projectId ID проекта
     * @param reportDate дата отчета (опционально, по умолчанию текущая дата)
     * @return список новых вакансий
     */
    @GetMapping("/new/{projectId}")
    public ResponseEntity<List<OpenVacancyDto>> getNewVacancies(
            @PathVariable Long projectId,
            @RequestParam(required = false) String reportDate) {
        
        Date sqlDate = null;
        if (reportDate != null && !reportDate.isEmpty()) {
            try {
                sqlDate = Date.valueOf(reportDate);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        }
        
        // Создаем объект Project для передачи в сервис
        com.ens.hhparser5.model.Project project = new com.ens.hhparser5.model.Project();
        project.setId(projectId);
        
        List<OpenVacancy> vacancies = vacancyService.findNewVacanciesForToday(project, sqlDate);
        List<OpenVacancyDto> vacancyDtos = vacancies.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(vacancyDtos);
    }

    /**
     * Получить открытые вакансии по проекту и поисковому тексту
     * @param projectId ID проекта
     * @param searchTextId ID поискового текста
     * @return список открытых вакансий
     */
    @GetMapping("/open/{projectId}/searchtext/{searchTextId}")
    public ResponseEntity<List<OpenVacancyDto>> getOpenVacanciesBySearchText(
            @PathVariable Long projectId,
            @PathVariable Long searchTextId) {
        
        List<OpenVacancy> vacancies = vacancyService.findAllOpenByProjectIdAndSearchTextId(projectId, searchTextId);
        List<OpenVacancyDto> vacancyDtos = vacancies.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(vacancyDtos);
    }

    /**
     * Получить детальное описание вакансии по её hhid
     * @param hhid ID вакансии на HeadHunter
     * @return детали вакансии (описание, ссылка)
     */
    @GetMapping("/details/{hhid}")
    public ResponseEntity<Map<String, String>> getVacancyDetails(@PathVariable String hhid) {
        try {
            Map<String, String> details = vacancySourceService.getVacancyDetails(hhid);
            return ResponseEntity.ok(details);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Преобразование OpenVacancy в OpenVacancyDto
     */
    private OpenVacancyDto convertToDto(OpenVacancy vacancy) {
        return new OpenVacancyDto(
                vacancy.getId(),
                vacancy.getName(),
                vacancy.getHhid(),
                vacancy.getSalary_netto(),
                vacancy.getEmployer(),
                vacancy.getUrl(),
                vacancy.getEmployer_hhid(),
                vacancy.getEmployer_link(),
                vacancy.getCount(),
                vacancy.getStartDate(),
                vacancy.getRegionName()
        );
    }
}
