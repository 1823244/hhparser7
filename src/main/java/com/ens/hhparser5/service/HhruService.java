package com.ens.hhparser5.service;

import com.ens.hhparser5.configuration.AppConfig;
import com.ens.hhparser5.model.VacancySource;
import com.ens.hhparser5.service.MethodCallMetricsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HhruService {
    private final Logger logger = LoggerFactory.getLogger(HhruService.class);

    @Autowired
    AppConfig appConfig;
    @Autowired
    private ObjectMapper jacksonMapper;
    @Autowired
    private HttpRequestService httpRequestService;


    /**
     * Возвращает МЭП с вакансиями по проекту в целом
     * http://localhost:9090/display/SPAC/HhruService.hhparser5
     * @param searchTextList
     * @param currentDate
     * @return
     */
    public Map<String, VacancySource> processSearchTextsList(final List<String> searchTextList,
                                                             final java.sql.Date currentDate) throws JsonProcessingException {

        final Map<String, VacancySource> allVacanciesByProject = new HashMap<>();

        logger.info("processing search text list: {}", searchTextList.toString());

        // таким подходом мы соберем в мэпе allVacanciesByProject только уникальные вакансии
        // (в общем случае вакансии могут повторяться в результатах поиска по
        // разным поисковым строкам)
        for (String searchText : searchTextList) {
            List<VacancySource> vacancies = getVacanciesByOneSearchString(searchText);
            Map<String, VacancySource> vacsByOneSearchString = new HashMap<>();
            for (VacancySource element : vacancies) {
                vacsByOneSearchString.put(element.getHhid(), element);
            }
            for (Map.Entry entry : vacsByOneSearchString.entrySet()) {
                VacancySource vacDto = (VacancySource) entry.getValue();
                allVacanciesByProject.put((String) entry.getKey(), vacDto);
            }
        }


        
        return allVacanciesByProject;
    }

    /**
     * Возвращает Map с вложенными Map с вакансиями по проекту в целом и по строкам поиска
     * http://localhost:9090/display/SPAC/HhruService.hhparser5
     * @param searchTextList
     * @param currentDate
     * @return
     */
    public Map<String, Map> processSearchTextsListVersionSubproject(final List<String> searchTextList,
                                                                final java.sql.Date currentDate) throws JsonProcessingException {

        Map<String,Map> result = new HashMap<>();

        // get list of search texts within one project

        final Map<String, VacancySource> allVacanciesByProject = new HashMap<>();

        logger.info("processing search text list: {}", searchTextList.toString());

        // таким подходом мы соберем в мэпе allVacanciesByProject только уникальные вакансии
        // (в общем случае вакансии могут повторяться в результатах поиска по
        // разным поисковым строкам)
        for (String searchText : searchTextList) {
            List<VacancySource> vacancies = getVacanciesByOneSearchString(searchText);
            Map<String, VacancySource> vacsByOneSearchString = new HashMap<>();
            for (VacancySource element : vacancies) {
                vacsByOneSearchString.put(element.getHhid(), element);
            }

            result.put(searchText, new HashMap<String, VacancySource>());
            result.get(searchText).putAll(vacsByOneSearchString);

            for (Map.Entry entry : vacsByOneSearchString.entrySet()) {
                VacancySource vacDto = (VacancySource) entry.getValue();
                allVacanciesByProject.put((String) entry.getKey(), vacDto);
            }
        }

        result.put("all", allVacanciesByProject);
        return result;
    }

    /**
     * 'Extract' tool (in terms of ETL)
     * Executes query against hh.ru and collects all vacancies within one search string, i.e. 'java developer'
     */
    private List<VacancySource> getVacanciesByOneSearchString(String searchString) throws JsonProcessingException {

        logger.info("processing search text: {}", searchString);

        List<VacancySource> vacancies = new ArrayList<>();

        //get pages count
        String url = String.format("https://api.hh.ru/vacancies?area=113&search_field=name&text=%s&per_page=%d&page=%d"
                ,searchString,appConfig.getVacanciesPerPage(),0);

        //HttpResponse<String> response = httpRequestService.executeRequestAndGetResponse(url);
        String json = httpRequestService.executeRequestAndGetResultAsString(url);

        /*if (response == null){
            throw new RuntimeException("HTTP request ERROR! response is NULL!");
        }*/
        if (json.isEmpty()){
            throw new RuntimeException("HTTP request ERROR! response is EMPTY!");
        }

        //String json = response.body();
        var jsonNode = jacksonMapper.readTree(json);

        if (jsonNode.get("pages") == null) {
            logger.info("tag <pages> is NULL! no vacancies found by search string: {}", searchString);
            return vacancies;
        }
        int pagesCount = jsonNode.get("pages").asInt(0);
        logger.info("found pages: {}", pagesCount);

        int vacanciesCount = jsonNode.get("found").asInt(0);
        logger.info("found vacancies: {}", vacanciesCount);

        if (vacanciesCount == 0) {
            return vacancies;
        }

        logger.info("alternate url: {}", jsonNode.get("alternate_url").asText(""));

        for (int page_number=0; page_number<pagesCount; page_number++){
            //String url2 = "https://api.hh.ru/vacancies?area=113&search_field=name"+"&text="+searchString+"&per_page="+per_page+"&page="+page_number;
            String url2 = String.format("https://api.hh.ru/vacancies?area=113&search_field=name&text=%s&per_page=%d&page=%d"
                    ,searchString,appConfig.getVacanciesPerPage(),page_number);

            if (page_number > 0) {
                // обычная пауза между запросами
                try {
                    Thread.sleep(appConfig.getDelay());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            logger.info("processing url of page # {}: {}",page_number,url2);

            // при ошибке выполнения запроса будем пытаться повторить его.
            // при этом пауза между попытками будет расти на 1 секунду.
            int repeatCount = 10;
            int currentIteration = 1;
            int repeatIncrease = 1000;//ms
            //int sleep = 10000;//ms
            int sleep = appConfig.getDelay();
            while (currentIteration <= repeatCount) {

                try {
                    vacancies.addAll( getOnePageForSearchString( url2 ) );
                    break;
                } catch (Exception e) {
                    currentIteration = currentIteration+1;
                    logger.error("There was an error in getOnePageForSearchString(). Process goes to repeat # {}", repeatCount);
                    // экстренная пауза между запросами
                    try {
                        Thread.sleep(sleep);
                    } catch (InterruptedException s) {
                        break;
                    }
                    sleep += repeatIncrease;
                }
            }
        }

        return vacancies;

    }

    /**
     * collects all vacancies within one page of result
     */
    private List<VacancySource> getOnePageForSearchString(String url) throws JsonProcessingException {
        List<VacancySource> vacancies = new ArrayList<>();
        HttpResponse<String> response = httpRequestService.executeRequestAndGetResponse(url);

        if (response == null){
            throw new RuntimeException("HTTP request ERROR! response is NULL!");
        }

        String json = response.body();
        JsonNode rootNode = jacksonMapper.readTree(json);
        var items = rootNode.get("items");
        if (items == null) return vacancies;
        //String itemsJson = items.asText();

        for (int i=0; i<items.size(); i++){
            JsonNode oneItem = items.get(i);

            VacancySource vacDto = new VacancySource();
            vacDto.setJson(oneItem.toPrettyString());
            //vacDto.setJsonNode(oneItem);//не надо здесь. поместим объект непосредственно перед обработкой вакансии
            vacDto.setHhid(oneItem.get("id").textValue());
            vacancies.add(vacDto);
        }
        return vacancies;
    }

    /**
     * Возвращает html с описанием вакансии из тэга description
     * @param hhid
     * @return
     */
    public Map<String,String> getVacancyDetailsByAPI(String hhid) throws JsonProcessingException {

        String json = getVacancyByAPI(hhid);

        var jsonNode = jacksonMapper.readTree(json);

        Map<String,String> result = new HashMap<>();
        result.put("description",jsonNode.get("description").textValue());
        //result.put("salary",jsonNode.get("description").textValue());
        //result.put("salary_gross",jsonNode.get("salary").get("to").asText());
        //result.put("schedule",jsonNode.get("schedule").get("name").asText());
        //result.put("employment",jsonNode.get("employment").get("name").textValue());
        //result.put("address",jsonNode.get("address").get("city").textValue());
        //result.put("professional_roles",jsonNode.get("professional_roles").get("name").textValue());
        result.put("alternate_url",jsonNode.get("alternate_url").textValue());
        /*result.put("______",jsonNode.get("description").textValue());
        result.put("______",jsonNode.get("description").textValue());
        result.put("______",jsonNode.get("description").textValue());
        result.put("______",jsonNode.get("description").textValue());
        */

        return result;
    }

    /**
     * Возвращает json с данными вакансии по URL https://api.hh.ru/vacancies/__id__
     * @param hhid
     * @return
     */
    public String getVacancyByAPI(String hhid) {
        String url = String.format("https://api.hh.ru/vacancies/%s",hhid);
        String json = httpRequestService.executeRequestAndGetResultAsString(url);
        return json;
    }
}
