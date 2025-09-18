package com.ens.hhparser5.service;

import com.ens.hhparser5.repository.ProjectRepo;
import com.ens.hhparser5.service.VacancyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VacanciesListJson {


    @Autowired
    private ProjectRepo projectRepo;
    @Autowired
    private VacancyService vacancyService;
    @Autowired
    private ObjectMapper jacksonMapper;

    /**
     * Отчет. Формирует json - список вакансий с отбором по проекту и дате
     * @param project_name String
     * @param reportDate java.sql.Date
     * @return json
     */
    public String vacListByDateAndProject(String project_name, java.sql.Date reportDate) {

        try {
            return jacksonMapper.writer().writeValueAsString(
                    vacancyService.findAllOpenByProjectId(
                            projectRepo.findByName(project_name).getId(), reportDate));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }




}
