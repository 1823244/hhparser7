package com.ens.hhparser5.service;

import com.ens.hhparser5.configuration.AppConfig;
import com.ens.hhparser5.model.*;
import com.ens.hhparser5.repository.EmployerRepo;
import com.ens.hhparser5.repository.VacancyRepo;
import com.ens.hhparser5.repository.VacancySourceRepo;
import com.ens.hhparser5.utility.ClockHolder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class VacancyService {
    private final Logger logger = LoggerFactory.getLogger(VacancyService.class);

    @Autowired
    private AppConfig appConfig;
    @Autowired
    private VacancyRepo vacancyRepo;
    @Autowired
    private DataSource ds;
    @Autowired
    private Connection connection;
    @Autowired
    private PublicationHistoryService publicationHistoryService;
    @Autowired
    private SearchHistoryService searchHistoryService;
    @Autowired
    private VacancySourceRepo vacancySourceRepo;
    @Autowired
    private ObjectMapper jacksonMapper;
    @Autowired
    private EmployerRepo employerRepo;
    @Autowired
    private RegionService regionService;

    public void processOneVacancy(final VacancySource vacancySource,
                                   final long project_id,
                                   final Date currentDate){
        try {
            final Vacancy vacancy = getVacancyFromJson(vacancySource);

            vacancyRepo.saveOrUpdate(vacancy);
            vacancySourceRepo.save(vacancySource);

            publicationHistoryService
                    .savePublication(project_id
                            , vacancy
                            , currentDate);

            searchHistoryService
                    .addNewSearchHistoryRecord(project_id
                            , vacancy
                            , currentDate);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    private Vacancy getVacancyFromJson(VacancySource vacancySource) throws JsonProcessingException {
        JsonNode rootNode = jacksonMapper.readTree(vacancySource.getJson());

        final String hhid = rootNode.get("id").asText();
        logger.info("id: {}, name: {}, employer: {}, compensation to: {} {}"
                , hhid
                , rootNode.get("name").asText()
                , rootNode.get("employer").get("name")
                , rootNode.get("salary").get("to")
                , "."//oneItem.get("salary").get("gross").asBoolean() ? "gross" : "net"
        );

        int salary_from = 0;
        int salary_to = 0;
        boolean gross = false;
        if (!(rootNode.get("salary") == null) && !rootNode.get("salary").isNull()){
            JsonNode s_from = rootNode.get("salary").get("from");
            if (!(s_from == null) && !s_from.isNull()) {
                salary_from = s_from.asInt();
            }
            JsonNode s_to = rootNode.get("salary").get("to");
            if (!(s_to ==null) && !s_to.isNull()) {
                salary_to = s_to.asInt();
            }
            JsonNode s_g = rootNode.get("salary").get("gross");
            if (!(s_g == null) && !s_g.isNull()) {
                gross = s_g.asBoolean();
            }
        }
        String employer_hhid = "";
        JsonNode emp = rootNode.get("employer");
        if (!(emp == null) && (!emp.isNull())) {
            if (!(emp.get("id") == null) && !(emp.get("id").isNull())){
                employer_hhid = emp.get("id").asText();
            }
        }

        JsonNode area = rootNode.get("area");
        int areaId = area.get("id").asInt();

        Vacancy vac = new Vacancy();

        vac.setHhid(rootNode.get("id").asText());
        vac.setArchived(rootNode.get("archived").asBoolean() ? 1 : 0);
        vac.setEmployer(employerRepo.findByHhid(employer_hhid).getId());
        vac.setName(rootNode.get("name").asText());
        vac.setGross(gross ? 1 : 0);
        vac.setAlternate_url(rootNode.get("alternate_url").asText());
        vac.setUrl(rootNode.get("url").asText());
        vac.setSalary_from(salary_from);
        vac.setSalary_to(salary_to);
        vac.setRegion(areaId);

        return vac;
    }

    // Запрос, выбирающий открытые вакансии на определенную дату
    // (без пагинации)
    private String vacsQueryTextPostgres(){
        String res = """    
                   SELECT
                    h.vacancy_id,
                    CASE WHEN v.salary_to > 0 THEN
                        CASE WHEN v.gross = 1 THEN v.salary_to * 0.87 ELSE salary_to END
                    ELSE
                        CASE WHEN v.gross = 1 THEN v.salary_from * 0.87 ELSE salary_from END
                    END AS salary_netto,
                    v.hhid as hhid,
                    v.NAME AS vacancy_name,
                    e.NAME AS employer_name,
                    e.hhid AS employer_hhid,
                    v.alternate_url as alternate_url,
                    r.name as region,
                    subq.date_published as date_published
                 FROM
                    (SELECT
                        Max(date_published) AS date_published,
                        vacancy_id,
                        project_id
                    FROM
                        publication_history AS h
                    WHERE
                        (date_published <= ?) --1 @date_published
                        AND (project_id = ?) --2 @project_id
                        --AND (date_closed IS NULL)
                    GROUP BY
                        vacancy_id,
                        project_id
                    ) AS subq
                    INNER JOIN
                        publication_history AS h
                            ON h.date_published = subq.date_published
                            AND h.vacancy_id = subq.vacancy_id
                            AND h.project_id = subq.project_id
                    LEFT OUTER JOIN
                        vacancy AS v
                            ON v.id = h.vacancy_id
                    LEFT OUTER JOIN
                        employer AS e
                            ON e.id = v.employer_id
                    LEFT OUTER JOIN
                        regions AS r
                            ON r.id = v.region
                 WHERE
                    (h.date_closed IS NULL)
                    OR (h.date_closed > ? ) --3 @date_published
                 ORDER BY
                    salary_netto DESC,
                    employer_name               
                """;
        return res;
    }

    /**
     * Ищет все открытые вакансии для html страницы projects БЕЗ ПАГИНАЦИИ
     * @param id - ИД проекта
     * @return
     */
    public List<OpenVacancy> findAllOpenByProjectId(long id, java.sql.Date reportDate){
        List<OpenVacancy> vacs = new ArrayList<>();
        try (
                //Connection conn = DataSourceUtils.getConnection(ds);
                PreparedStatement stmt = connection.prepareStatement( vacsQueryTextPostgres() )
        ){
            java.sql.Date reportDateValue;
            if (reportDate == null) {
                reportDateValue = java.sql.Date.valueOf(LocalDate.now(ClockHolder.getClock()));
            } else {
                reportDateValue = reportDate;
            }
            stmt.setDate(1, reportDateValue);
            stmt.setLong(2, id);
            stmt.setDate(3, reportDateValue);
            ResultSet rs = stmt.executeQuery();
            int count = 1;
            while (rs.next()){

                var v = new OpenVacancy();

                v.setId(rs.getLong("vacancy_id"));
                v.setName(rs.getString("vacancy_name"));
                v.setUrl(rs.getString("alternate_url"));
                v.setHhid(rs.getString("hhid"));
                v.setEmployer(rs.getString("employer_name"));
                v.setSalary_netto(rs.getInt("salary_netto"));
                v.setEmployer_hhid(rs.getString("employer_hhid"));
                v.setEmployer_link("https://hh.ru/employer/"+rs.getString("employer_hhid"));
                v.setStartDate(rs.getDate("date_published"));
                v.setRegionName(rs.getString("region"));
                v.setCount(count++);

                vacs.add(v);

            }
            rs.close();
            return vacs;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Запрос, выбирающий открытые вакансии на определенную дату
    // по проекту и строке поиска
    // (без пагинации)
    private String vacsQueryTextPostgresBySearchText(){
        String res = """
                                
                   SELECT
                     h.vacancy_id,
                     CASE WHEN v.salary_to > 0 
                        THEN 
                            CASE WHEN v.gross = 1 
                                THEN v.salary_to * 0.87 
                                ELSE salary_to 
                            END 
                        ELSE 
                            CASE WHEN v.gross = 1 
                                THEN v.salary_from * 0.87 
                                ELSE salary_from 
                            END 
                     END AS salary_netto,
                  v.hhid as hhid,
                  v.NAME AS vacancy_name,
                  e.NAME AS employer_name,
                  e.hhid AS employer_hhid,
                  v.alternate_url as alternate_url
                   FROM
                     (
                       SELECT
                         Max(date_published) AS date_published,
                         vacancy_id,
                         project_id,
                         searchtext_id
                       FROM
                         publication_history_stext AS h
                       WHERE
                         (date_published <= ?) --1 @date_published
                         AND (project_id = ?) --2 @project_id
                         AND (searchtext_id = ?) --3 int
                         --AND (date_closed IS NULL)
                       GROUP BY
                         vacancy_id,
                         project_id,
                         searchtext_id
                     ) AS subq
                     INNER JOIN
                         publication_history_stext AS h
                             ON h.date_published = subq.date_published
                             AND h.vacancy_id = subq.vacancy_id
                             AND h.project_id = subq.project_id
                             AND h.searchtext_id = subq.searchtext_id
                     LEFT OUTER JOIN
                         vacancy AS v
                             ON v.id = h.vacancy_id
                     LEFT OUTER JOIN
                         employer AS e
                             ON e.id = v.employer_id
                 WHERE
                	(h.date_closed IS NULL)
                	OR (h.date_closed > ? ) --4 @date_published
                 ORDER BY
                    salary_netto DESC,
                    employer_name                
                """;
        return res;
    }

    /**
     * Ищет все открытые вакансии для html страницы projects БЕЗ ПАГИНАЦИИ
     * // по проекту и строке поиска
     * @param projectId - ИД проекта
     * @return
     */
    public List<OpenVacancy> findAllOpenByProjectIdAndSearchTextId(long projectId, long searchTextId){
        List<OpenVacancy> vacs = new ArrayList<>();
        try (
                //Connection conn = DataSourceUtils.getConnection(ds);
                PreparedStatement stmt = connection.prepareStatement( vacsQueryTextPostgresBySearchText() )
        ){
            stmt.setDate(1, java.sql.Date.valueOf(LocalDate.now(ClockHolder.getClock())));
            stmt.setLong(2, projectId);
            stmt.setLong(3, searchTextId);
            stmt.setDate(4, java.sql.Date.valueOf(LocalDate.now(ClockHolder.getClock())));
            ResultSet rs = stmt.executeQuery();
            int count = 1;
            while (rs.next()){

                var v = new OpenVacancy();

                v.setId(rs.getLong("vacancy_id"));
                v.setName(rs.getString("vacancy_name"));
                v.setUrl(rs.getString("alternate_url"));
                v.setHhid(rs.getString("hhid"));
                v.setEmployer(rs.getString("employer_name"));
                v.setSalary_netto(rs.getInt("salary_netto"));
                v.setEmployer_hhid(rs.getString("employer_hhid"));
                v.setEmployer_link("https://hh.ru/employer/"+rs.getString("employer_hhid"));
                v.setCount(count++);

                vacs.add(v);

            }
            rs.close();
            return vacs;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Запрос, выбирающий открытые вакансии на определенную дату + pagination
    // по проекту
    private String vacsQueryTextPostgresPagination(){
        String res = """
                SELECT
                	CTEORDERED.RowNum AS RowNum,
                	CTEORDERED.vacancy_id AS vacancy_id,
                	CTEORDERED.salary_netto AS salary_netto,
                	CTEORDERED.hhid as hhid,
                	CTEORDERED.vacancy_name AS vacancy_name,
                	CTEORDERED.employer_name AS employer_name,
                	CTEORDERED.employer_hhid AS employer_hhid,
                	CTEORDERED.alternate_url as alternate_url,
                	CTEORDERED.region as region,
                	CTEORDERED.date_published as date_published
                FROM
                	(SELECT
                			ROW_NUMBER() OVER (
                				ORDER BY
                				salary_netto DESC,
                				employer_name) AS RowNum,
                			CTE.vacancy_id AS vacancy_id,
                			CTE.salary_netto AS salary_netto,
                			CTE.hhid as hhid,
                			CTE.vacancy_name AS vacancy_name,
                			CTE.employer_name AS employer_name,
                			CTE.employer_hhid AS employer_hhid,
                			CTE.alternate_url as alternate_url,
                			CTE.region as region,
                			CTE.date_published as date_published
                		FROM
                			(SELECT
                				h.vacancy_id,
                				CASE WHEN v.salary_to > 0 THEN
                					CASE WHEN v.gross = 1 THEN v.salary_to * 0.87 ELSE salary_to END
                				ELSE
                					CASE WHEN v.gross = 1 THEN v.salary_from * 0.87 ELSE salary_from END
                				END AS salary_netto,
                				v.hhid as hhid,
                				v.NAME AS vacancy_name,
                				e.NAME AS employer_name,
                				e.hhid AS employer_hhid,
                				v.alternate_url as alternate_url,
                				r.name as region,
                				subq.date_published as date_published
                			FROM
                				(SELECT
                					Max(date_published) AS date_published,
                					vacancy_id,
                					project_id
                				FROM
                					publication_history AS h
                				WHERE
                					(date_published <= ?) --1 @date_published
                					AND (project_id = ?) --2 @project_id
                					--AND (date_closed IS NULL)
                				GROUP BY
                					vacancy_id,
                					project_id
                				) AS subq
                				INNER JOIN
                					publication_history AS h
                						ON h.date_published = subq.date_published
                						AND h.vacancy_id = subq.vacancy_id
                						AND h.project_id = subq.project_id
                				LEFT OUTER JOIN
                					vacancy AS v ON v.id = h.vacancy_id
                				LEFT OUTER JOIN
                					employer AS e ON e.id = v.employer_id
                				LEFT OUTER JOIN
                					regions AS r ON r.id = v.region
                				LEFT JOIN
                					blacklist as bl
                						ON bl.vacancy_id = h.vacancy_id
                			WHERE
                				((h.date_closed IS NULL)
                					OR (h.date_closed > ? ) --3 @date_published
                				)
                				AND
                				bl.id IS NULL --не показываем вакансии, которые есть в блэклисте
                			) as CTE
                	) AS CTEORDERED
                WHERE
                	CTEORDERED.RowNum > ?
                	AND CTEORDERED.RowNum <= ?                  
                """;
        return res;
    }

    /**
     * Реализация пагинации при просмотре открытых вакансий
     * Возвращает Map с вакансиями на указанной "странице" для view "openvacancies.html"
     *
     * @param projectId
     * @param pageNumber
     * @return
     */
    public Map<String,Object> findAllOpenByProjectIdPagination(long projectId, int pageNumber){
        Map<String,Object> resMap = new HashMap<>();
        List<OpenVacancy> vacanciesList = new ArrayList<>();
        String queryText = vacsQueryTextPostgresPagination();
        try (PreparedStatement stmt = connection.prepareStatement( queryText )){
            stmt.setDate(1, java.sql.Date.valueOf(LocalDate.now(ClockHolder.getClock())));
            stmt.setLong(2, projectId);
            stmt.setDate(3, java.sql.Date.valueOf(LocalDate.now(ClockHolder.getClock())));
            stmt.setLong(4, (long) pageNumber * appConfig.getPagination() - appConfig.getPagination());
            stmt.setLong(5, (long) pageNumber * appConfig.getPagination());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()){

                var v = new OpenVacancy();

                v.setId(rs.getLong("vacancy_id"));
                v.setName(rs.getString("vacancy_name"));
                v.setUrl(rs.getString("alternate_url"));
                v.setHhid(rs.getString("hhid"));
                v.setEmployer(rs.getString("employer_name"));
                v.setSalary_netto(rs.getInt("salary_netto"));
                v.setEmployer_hhid(rs.getString("employer_hhid"));
                v.setEmployer_link("https://hh.ru/employer/"+rs.getString("employer_hhid"));
                v.setCount(rs.getInt("RowNum"));
                v.setStartDate(rs.getDate("date_published"));
                v.setRegionName(rs.getString("region"));
                vacanciesList.add(v);

            }
            rs.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        resMap.put("vacs", vacanciesList);

        //todo Нужен рефакторинг. Возможно стоит вызывать это из контроллера
        List<OpenVacancy> allVacs = findAllOpenByProjectId(projectId, null);

        // дальше соберем статистику по количеству вакансий в нескольких разрезах оплат
        resMap.put("total", allVacs.size());

        resMap.put("over500", allVacs.stream().filter((vac)-> vac.getSalary_netto() >= 500000)
                .collect(Collectors.toList()).size());

        resMap.put("over400", allVacs.stream().filter((vac)-> vac.getSalary_netto() >= 400000)
                .collect(Collectors.toList()).size());

        resMap.put("over350", allVacs.stream().filter((vac)-> vac.getSalary_netto() >= 350000)
                .collect(Collectors.toList()).size());

        resMap.put("over300", allVacs.stream().filter((vac)-> vac.getSalary_netto() >= 300000)
                .collect(Collectors.toList()).size());

        resMap.put("over250", allVacs.stream().filter((vac)-> vac.getSalary_netto() >= 250000)
                .collect(Collectors.toList()).size());

        resMap.put("over200", allVacs.stream().filter((vac)-> vac.getSalary_netto() >= 200000)
                .collect(Collectors.toList()).size());

        resMap.put("hiddensalary", allVacs.stream().filter((vac)-> vac.getSalary_netto() == 0)
                .collect(Collectors.toList()).size());

        return resMap;

    }

    // Запрос, выбирающий открытые вакансии на определенную дату + pagination
    private String vacsQueryTextPostgresPaginationBySearchText(){
        String res = """
                SELECT 
                    CTEORDERED.RowNum AS RowNum,
                    CTEORDERED.vacancy_id AS vacancy_id,
                    CTEORDERED.salary_netto AS salary_netto,
                    CTEORDERED.hhid as hhid,
                    CTEORDERED.vacancy_name AS vacancy_name,
                    CTEORDERED.employer_name AS employer_name,
                    CTEORDERED.employer_hhid AS employer_hhid,
                    CTEORDERED.alternate_url as alternate_url
                FROM (
                   SELECT 
                    ROW_NUMBER() OVER (
                        ORDER BY 
                            salary_netto DESC,
                            employer_name) AS RowNum,
                    CTE.vacancy_id AS vacancy_id,
                    CTE.salary_netto AS salary_netto,
                    CTE.hhid as hhid,
                    CTE.vacancy_name AS vacancy_name,
                    CTE.employer_name AS employer_name,
                    CTE.employer_hhid AS employer_hhid,
                    CTE.alternate_url as alternate_url
                  FROM (  SELECT                  
                     h.vacancy_id,
                     CASE WHEN v.salary_to > 0 
                        THEN 
                            CASE WHEN v.gross = 1 
                                THEN v.salary_to * 0.87 
                                ELSE salary_to 
                            END 
                        ELSE 
                            CASE WHEN v.gross = 1 
                                THEN v.salary_from * 0.87 
                                ELSE salary_from 
                            END 
                     END AS salary_netto,
                  v.hhid as hhid,
                  v.NAME AS vacancy_name,
                  e.NAME AS employer_name,
                  e.hhid AS employer_hhid,
                  v.alternate_url as alternate_url
                   FROM
                     (
                       SELECT
                         Max(date_published) AS date_published,
                         vacancy_id,
                         project_id,
                         searchtext_id
                       FROM
                         publication_history_stext AS h
                       WHERE
                         (date_published <= ?) --1 @date_published
                         AND (project_id = ?) --2 @project_id
                         AND (searchtext_id = ?) --3 @stext_id
                         --AND (date_closed IS NULL)
                       GROUP BY
                         vacancy_id,
                         project_id,
                         searchtext_id                         
                     ) AS subq
                     INNER JOIN publication_history_stext AS h 
                     ON h.date_published = subq.date_published
                     AND h.vacancy_id = subq.vacancy_id
                     AND h.project_id = subq.project_id
                     AND h.searchtext_id = subq.searchtext_id
                     LEFT OUTER JOIN vacancy AS v ON v.id = h.vacancy_id
                  LEFT OUTER JOIN employer AS e ON e.id = v.employer_id
                WHERE
                	(h.date_closed IS NULL)
                	OR (h.date_closed > ? ) --4 @date_published 
                	) as CTE
                ) AS CTEORDERED  
                WHERE
                    CTEORDERED.RowNum > ? --5 int
                     AND CTEORDERED.RowNum <= ? --6 int                         
                """;
        return res;
    }

    /**
     * Возвращает Map со всеми вакансиями по проекту и строке поиска для отчета openvacanciestext.html
     * Пока (2023-01-18) пагинация работает только на Postgres
     * Для MSSQL нужно менять использование хранимой функции на запрос
     * @param projectId
     * @param pageNumber
     * @return
     */
    public Map<String, Object> findAllOpenByProjectIdPaginationBySearchString(long projectId, long stextId, int pageNumber) {

        Map<String,Object> resMap = new HashMap<>();
        List<OpenVacancy> vacs = new ArrayList<>();
        String queryText = vacsQueryTextPostgresPaginationBySearchText();
        try (//Connection conn = DataSourceUtils.getConnection(ds);
             PreparedStatement stmt = connection.prepareStatement( queryText )
        ){
            stmt.setDate(1, java.sql.Date.valueOf(LocalDate.now(ClockHolder.getClock())));
            stmt.setLong(2, projectId);
            stmt.setLong(3, stextId);
            stmt.setDate(4, java.sql.Date.valueOf(LocalDate.now(ClockHolder.getClock())));
            stmt.setLong(5, (long) pageNumber * appConfig.getPagination() - appConfig.getPagination());
            stmt.setLong(6, (long) pageNumber * appConfig.getPagination());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()){

                var v = new OpenVacancy();

                v.setId(rs.getLong("vacancy_id"));
                v.setName(rs.getString("vacancy_name"));
                v.setUrl(rs.getString("alternate_url"));
                v.setHhid(rs.getString("hhid"));
                v.setEmployer(rs.getString("employer_name"));
                v.setSalary_netto(rs.getInt("salary_netto"));
                v.setEmployer_hhid(rs.getString("employer_hhid"));
                v.setEmployer_link("https://hh.ru/employer/"+rs.getString("employer_hhid"));
                v.setCount(rs.getInt("RowNum"));

                vacs.add(v);

            }
            rs.close();
            resMap.put("vacs", vacs);

            List<OpenVacancy> allVacs = findAllOpenByProjectIdAndSearchTextId(projectId, stextId);

            // дальше соберем статистику по количеству вакансий в нескольких разрезах оплат
            resMap.put("total", allVacs.size());

            resMap.put("over500", allVacs.stream().filter((vac)-> vac.getSalary_netto() >= 500000)
                    .collect(Collectors.toList()).size());

            resMap.put("over400", allVacs.stream().filter((vac)-> vac.getSalary_netto() >= 400000)
                    .collect(Collectors.toList()).size());

            resMap.put("over350", allVacs.stream().filter((vac)-> vac.getSalary_netto() >= 350000)
                    .collect(Collectors.toList()).size());

            resMap.put("over300", allVacs.stream().filter((vac)-> vac.getSalary_netto() >= 300000)
                    .collect(Collectors.toList()).size());

            resMap.put("over250", allVacs.stream().filter((vac)-> vac.getSalary_netto() >= 250000)
                    .collect(Collectors.toList()).size());

            resMap.put("over200", allVacs.stream().filter((vac)-> vac.getSalary_netto() >= 200000)
                    .collect(Collectors.toList()).size());

            resMap.put("hiddensalary", allVacs.stream().filter((vac)-> vac.getSalary_netto() == 0)
                    .collect(Collectors.toList()).size());

            return resMap;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void processOneVacancyBySearchText(VacancySource vacancySource, long projectId, long stextId, Date currentDate) {
        try {
            final Vacancy vacancy = getVacancyFromJson(vacancySource);

            vacancyRepo.saveOrUpdate(vacancy);
            vacancySourceRepo.save(vacancySource);

            publicationHistoryService
                    .savePublicationBySearchText(projectId
                            , stextId
                            , vacancy
                            , currentDate);

            searchHistoryService
                    .addNewSearchHistoryRecordBySearchText(projectId
                            , stextId
                            , vacancy
                            , currentDate);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public List<OpenVacancy> findNewVacanciesForToday(Project project, java.sql.Date reportDate) {
        List<OpenVacancy> vacs = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement( vacsQueryTextPostgresNewVacsForToday() )        ){
            java.sql.Date reportDateValue;
            if (reportDate == null) {
                reportDateValue = java.sql.Date.valueOf(LocalDate.now(ClockHolder.getClock()));
            } else {
                reportDateValue = reportDate;
            }
            stmt.setDate(1, reportDateValue);
            stmt.setLong(2, project.getId());
            stmt.setDate(3, reportDateValue);
            ResultSet rs = stmt.executeQuery();
            int count = 1;
            while (rs.next()){

                var v = new OpenVacancy();

                v.setId(rs.getLong("vacancy_id"));
                v.setName(rs.getString("vacancy_name"));
                v.setUrl(rs.getString("alternate_url"));
                v.setHhid(rs.getString("hhid"));
                v.setEmployer(rs.getString("employer_name"));
                v.setSalary_netto(rs.getInt("salary_netto"));
                v.setEmployer_hhid(rs.getString("employer_hhid"));
                v.setEmployer_link("https://hh.ru/employer/"+rs.getString("employer_hhid"));
                v.setRegionName(rs.getString("region"));
                v.setCount(count++);

                vacs.add(v);

            }
            rs.close();
            return vacs;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Запрос, выбирающий открытые вакансии открытые "сегодня"
    // (без пагинации)
    private String vacsQueryTextPostgresNewVacsForToday(){
        String res = """
                                
                   SELECT
                     h.vacancy_id,
                     CASE WHEN v.salary_to > 0 
                        THEN 
                            CASE WHEN v.gross = 1 THEN v.salary_to * 0.87 ELSE salary_to END 
                        ELSE 
                            CASE WHEN v.gross = 1 THEN v.salary_from * 0.87 ELSE salary_from END 
                     END AS salary_netto,
                  v.hhid as hhid,
                  v.NAME AS vacancy_name,
                  e.NAME AS employer_name,
                  e.hhid AS employer_hhid,
                  r.name AS region,
                  v.alternate_url as alternate_url
                   FROM
                     (
                       SELECT
                         Max(date_published) AS date_published,
                         project_id
                       FROM
                         publication_history AS h
                       WHERE
                         (date_published <= ?) --1 @date_published
                         AND (project_id = ?) --2 @project_id
                         --AND (date_closed IS NULL)
                       GROUP BY
                         project_id
                     ) AS subq
                     INNER JOIN publication_history AS h 
                        ON h.date_published = subq.date_published
                        AND h.project_id = subq.project_id
                     LEFT OUTER JOIN vacancy AS v 
                        ON v.id = h.vacancy_id
                     LEFT OUTER JOIN employer AS e 
                        ON e.id = v.employer_id
                     LEFT OUTER JOIN regions AS r 
                        ON r.id = v.region
                WHERE
                	(h.date_closed IS NULL)
                	OR (h.date_closed > ? ) --3 @date_published
                ORDER BY
                    salary_netto DESC,
                    employer_name                
                """;
        return res;
    }

    public List<OpenVacancy> findClosedVacanciesForToday(Project project, java.sql.Date reportDate) {
        List<OpenVacancy> vacs = new ArrayList<>();
        try
        {
            Statement dropTable = connection.createStatement();
            dropTable.executeUpdate("DROP TABLE IF EXISTS max_date;");
            // выберем максимальную дату публикации вакансий по указанному проекту
            // и будем считать, что это - "сегодня"
            PreparedStatement createTable = connection.prepareStatement("""
                    CREATE TEMP TABLE max_date
                AS
                    SELECT
                         Max(h.date_published) AS date_published,
                         h.project_id AS project_id
                       FROM
                         publication_history AS h
                       WHERE
                         (h.date_published <= ?) --1 @date_published
                         AND (h.project_id = ?) --2 @project_id
                       GROUP BY
                         h.project_id
                         
                """);
            java.sql.Date reportDateValue;
            if (reportDate == null) {
                reportDateValue = java.sql.Date.valueOf(LocalDate.now(ClockHolder.getClock()));
            } else {
                reportDateValue = reportDate;
            }
            createTable.setDate(1, reportDateValue);
            createTable.setLong(2, project.getId());
            createTable.executeUpdate();

            PreparedStatement stmt = connection.prepareStatement( vacsQueryTextPostgresClosedVacsForToday() );

            ResultSet rs = stmt.executeQuery();
            int count = 1;
            while (rs.next()){

                var v = new OpenVacancy();

                v.setId(rs.getLong("vacancy_id"));
                v.setName(rs.getString("vacancy_name"));
                v.setUrl(rs.getString("alternate_url"));
                v.setHhid(rs.getString("hhid"));
                v.setEmployer(rs.getString("employer_name"));
                v.setSalary_netto(rs.getInt("salary_netto"));
                v.setEmployer_hhid(rs.getString("employer_hhid"));
                v.setEmployer_link("https://hh.ru/employer/"+rs.getString("employer_hhid"));
                v.setStartDate(rs.getDate("date_published"));
                v.setRegionName(rs.getString("region"));
                v.setCount(count++);

                vacs.add(v);

            }
            rs.close();
            stmt.close();
            createTable.close();
            dropTable.executeUpdate("DROP TABLE IF EXISTS max_date;");
            dropTable.close();

            return vacs;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Запрос, выбирающий вакансии, закрытые "сегодня"
    // (без пагинации)
    // т.е. "вчера" они есть, а "сегодня" - их уже нет
    // "сегодня" - максимальная дата в истории публикаций
    // "вчера" - максимальная на минус 1 день от "сегодня"
    private String vacsQueryTextPostgresClosedVacsForToday(){
        String res = """
                   SELECT
                     h.vacancy_id,
                     CASE WHEN v.salary_to > 0
                        THEN
                            CASE WHEN v.gross = 1 THEN v.salary_to * 0.87 ELSE salary_to END
                        ELSE
                            CASE WHEN v.gross = 1 THEN v.salary_from * 0.87 ELSE salary_from END
                     END AS salary_netto,
                  v.hhid as hhid,
                  v.NAME AS vacancy_name,
                  e.NAME AS employer_name,
                  e.hhid AS employer_hhid,
                  v.alternate_url as alternate_url,
                  r.name as region,
                  h.date_published as date_published
                   FROM
                     publication_history AS h
                        INNER JOIN max_date as m
                        ON h.date_closed = m.date_published
                        AND h.project_id = m.project_id
                     LEFT OUTER JOIN vacancy AS v
                        ON v.id = h.vacancy_id
                     LEFT OUTER JOIN employer AS e
                        ON e.id = v.employer_id
                     LEFT OUTER JOIN regions AS r
                        ON r.id = v.region
                ORDER BY
                    salary_netto DESC,
                    employer_name
                """;
        return res;
    }

    // возвращает дату, на которую мы нашли новые вакансии на сегодня
    // это максимальная дата в таблице истории публикации
    public Date getDateOfNewVacancies(Project project, Date reportDate) {
        try (
                //Connection conn = DataSourceUtils.getConnection(ds);
                PreparedStatement stmt = connection.prepareStatement("""
                       SELECT
                         Max(date_published) AS date_published
                       FROM
                         publication_history AS h
                       WHERE
                         (date_published <= ?) --1 @date_published
                         AND (project_id = ?) --2 @project_id
                        """ )
        ){
            java.sql.Date reportDateValue;
            if (reportDate == null) {
                reportDateValue = java.sql.Date.valueOf(LocalDate.now(ClockHolder.getClock()));
            } else {
                reportDateValue = reportDate;
            }
            stmt.setDate(1, reportDateValue);
            stmt.setLong(2, project.getId());

            ResultSet rs = stmt.executeQuery();
            Date resultDate = null;
            int count = 1;
            while (rs.next()){
                resultDate = rs.getDate("date_published");
            }
            rs.close();
            return resultDate;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Vacancy findByHhid(String hhid){
        return vacancyRepo.findByHhid(hhid);
    }

    public Vacancy findById(long id) {
        return vacancyRepo.findById(id);
    }
}
