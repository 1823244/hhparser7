package com.ens.hhparser5.service;

import com.ens.hhparser5.model.Employer;
import com.ens.hhparser5.model.VacancySource;
import com.ens.hhparser5.repository.EmployerRepo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmployerService {

    @Autowired
    private EmployerRepo employerRepo;
    @Autowired
    private ObjectMapper jacksonMapper;

    public Employer findById(long id){
        return employerRepo.findById(id);
    }

    public Employer findByHhid(String hhid){
        return employerRepo.findByHhid(hhid);
    }

    public List<Employer> findAll() {
        return employerRepo.findAll();
    }

    /**
     * Этот метод должен работать в один поток. Так проще сохранить уникальных работодателей
     * @param allVacanciesByProject
     * @return
     * @throws JsonProcessingException
     */
    public void parseAndSaveUniqueEmployers(Map<String, VacancySource> allVacanciesByProject) throws JsonProcessingException {

        Map<String,Employer> emps = new HashMap<>();

        for (VacancySource vacancySource : allVacanciesByProject.values()) {
            JsonNode jsonNode = jacksonMapper.readTree(vacancySource.getJson());
            String employer_hhid = "";
            String employer_name = "";
            String url = "";
            JsonNode emp = jsonNode.get("employer");
            if (!(emp == null) && (!emp.isNull())) {
                if (!(emp.get("id") == null) && !(emp.get("id").isNull())) {
                    employer_hhid = emp.get("id").asText();
                }
                if (!(emp.get("name") == null) && !emp.get("name").isNull()) {
                    employer_name = emp.get("name").asText();
                }
                if (!(emp.get("alternate_url") == null) && !emp.get("alternate_url").isNull()) {
                    url = emp.get("alternate_url").asText();
                }
            }

            if (emps.get(employer_hhid) == null){
                Employer employer = new Employer();
                employer.setHhid(employer_hhid);
                employer.setName(employer_name);
                employer.setUrl(url);
                emps.put(employer_hhid, employer);
            }

        }
        for (Employer employer : emps.values()) {
            // сохраняем работодателя. при этом, если он новый, то в объект
            // возвращается ID из нашей базы и можно далее использовать getId()
            employerRepo.save(employer);
        }

    }

    public void delete(Employer employer) {
        employerRepo.delete(employer);
    }
}
