package com.ens.hhparser5.service;

import com.ens.hhparser5.model.VacancySource;
import com.ens.hhparser5.repository.VacancySourceRepo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class VacancySourceService {

    @Autowired
    private VacancySourceRepo vacancySourceRepo;
    @Autowired
    private ObjectMapper jacksonMapper;
    @Autowired
    private HhruService hhruService;

    public VacancySource findById(String hhid){
        return vacancySourceRepo.findByHhid(hhid);
    }

    /**
     * получает вакансию из базы
     * если там нет деталей - обращается к АПИ
     * и сохраняет полную версию вакансии в базе
     * @param hhid
     * @return
     * @throws JsonProcessingException
     */
    public Map<String, String> getVacancyDetails(String hhid) throws JsonProcessingException {
        VacancySource vacancySource = vacancySourceRepo.findByHhid(hhid);
        String json = vacancySource.getJson();
        var jsonNode = jacksonMapper.readTree(json);
        Map<String,String> result = new HashMap<>();
        if (jsonNode.get("description") == null){
            //result.put("description",jsonNode.get("snippet").get("requirement").textValue()+"\n"+jsonNode.get("snippet").get("responsibility").textValue());
            String newJson = hhruService.getVacancyByAPI(hhid);
            vacancySource.setJson(newJson);
            vacancySourceRepo.save(vacancySource);
            var newJsonNode = jacksonMapper.readTree(newJson);
            result.put("description",newJsonNode.get("description").textValue());
        } else {
            result.put("description",jsonNode.get("description").textValue());
        }
        result.put("alternate_url",jsonNode.get("alternate_url").textValue());
        return result;
    }

    public String getVacancySourceJson(String hhid){
        VacancySource vacancySource = vacancySourceRepo.findByHhid(hhid);
        return vacancySource.getJson();
    }
}
