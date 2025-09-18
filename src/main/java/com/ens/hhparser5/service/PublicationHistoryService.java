package com.ens.hhparser5.service;

import com.ens.hhparser5.configuration.AppConfig;
import com.ens.hhparser5.model.Vacancy;
import com.ens.hhparser5.repository.PublicationHistoryRepo;
import com.ens.hhparser5.utility.ClockHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class PublicationHistoryService {

    private Logger logger = LoggerFactory.getLogger(PublicationHistoryService.class);

    @Autowired
    private AppConfig appConfig;
    @Autowired
    private Connection connection;
    @Autowired
    private PublicationHistoryRepo publicationHistoryRepo;

    public Map<String, Long> findPublication(long projectId, String hhid) {
        return publicationHistoryRepo.findPublication(projectId, hhid);
    }

    /**
     * Creates or updates info about vacancy publication:
     * - creates new if not exists
     * - updates:
     *      - set open if reopened vacancy has come
     *      - set closed if archived vacancy has come
     *
     * @param projectId
     * @param vacancy
     * @param datePublished
     * @return long vacancy_id
     */
    public void savePublication(long projectId, Vacancy vacancy, Date datePublished) {
        String query = """
            INSERT INTO
            publication_history (
               project_id,
               vacancy_id,
               hhid,
               date_published,
               date_closed,
               logmoment
               )
            VALUES (
               ?,?,?,?,?,?
               )""";
        Map<String, Long> existingPublication = findPublication(projectId, vacancy.getHhid());
        if (existingPublication.get("id") == -1L) {
            try {
                PreparedStatement stmt = connection.prepareStatement(query);
                stmt.setLong(1, projectId);
                stmt.setLong(2, vacancy.getId());
                stmt.setString(3, vacancy.getHhid());
                stmt.setDate(4, datePublished);
                stmt.setNull(5, Types.DATE);//closed
                //old
                //stmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
                //new
                stmt.setTimestamp(6, Timestamp.valueOf(
                        LocalDateTime.now(ClockHolder.getClock())));
                stmt.executeUpdate();
                stmt.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            if ((existingPublication.get("archived")==1L || existingPublication.get("closed")==1L) && vacancy.getArchived() == 0) {
                // если найдена и это ( вакансия в архиве ИЛИ вакансия закрыта )
                // - делаем открытой (т.к. нашли ее поиском в API)
                setVacancyOpen(vacancy.getId());
            } else if (existingPublication.get("archived")==0L && vacancy.getArchived() == 1){
                // если найдена и это ( вакансия НЕ в архиве по данным нашего приложения И вакансия в архиве по данным API)
                // - делаем закрытой
                setPublicationClosed(vacancy.getId());
            }
        }
    }

    private void setPublicationClosed(long id) {
        String queryText = "UPDATE publication_history SET date_closed = ?, logmoment = ? WHERE vacancy_id = ?";
        java.sql.Date dateClosed = java.sql.Date.valueOf(LocalDate.now());
        try {
            PreparedStatement stmt = connection.prepareStatement(queryText);
            stmt.setDate(1, dateClosed);
            stmt.setTimestamp(2, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            stmt.setLong(3, id);
            stmt.execute();
            stmt.close();
            logger.info("vacancy {} has been closed", id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void setVacancyOpen(long id) {

        String queryText = "UPDATE publication_history SET date_closed = NULL, logmoment = ? WHERE vacancy_id = ?";

        try {
            //PreparedStatement stmt = sqlConfig.getConnection().prepareStatement(queryText);
            PreparedStatement stmt = connection.prepareStatement(queryText);
            stmt.setTimestamp(1, java.sql.Timestamp.valueOf(LocalDateTime.now(ClockHolder.getClock())));
            stmt.setLong(2, id);

            stmt.executeUpdate();
            stmt.close();

            logger.info("vacancy {} has been reopened", id);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    private String queryTextSimple(){
        return  "" +
                "SELECT" +
                "   h.vacancy_id as vacancy_id" +
                "   ,v.hhid as hhid" +
                "   ,v.name as vacancy_name " +
                "   ,v.salary_from as salary_from " +
                "   ,v.salary_to as salary_to " +
                "   ,v.gross as gross " +
                "   ,e.name as employer_name " +
                "   ,v.url as url " +
                "   ,v.alternate_url as alternate_url " +
                "FROM " +
                "   publication_history as h " +
                "       LEFT JOIN vacancy as v " +
                "       ON v.id = h.vacancy_id " +
                "       LEFT JOIN employer as e " +
                "       ON e.id = v.employer_id " +
                "WHERE " +
                "   date_published = ? " +
                "   AND date_closed is NULL " +
                "   AND project_id = ?";
    }

    public void vacancyClosingProcessWithSQLQuery(long project_id, java.sql.Date currentDate){

        if (appConfig.isUsePostgres()) {
            vacancyClosingProcessWithSQLQueryPostgres(project_id, currentDate);
        } else {
            vacancyClosingProcessWithSQLQueryMSSQL(project_id, currentDate);
        }
    }

    public void vacancyClosingProcessWithSQLQueryMSSQL(long project_id, java.sql.Date currentDate){

        logger.info("start closing process by SQL-query (MSSQL) {}", LocalDateTime.now(ClockHolder.getClock()).toString());

        String query = """
                    Declare @DateToday date;
                    Set @DateToday = ?;
                    Declare @logmoment datetime;
                    Set @logmoment = ?;
                    Declare @project_id bigint;
                    Set @project_id = ?;
            
                    UPDATE publication_history
                    SET date_closed = @DateToday,
                    logmoment = @logmoment
                            OUTPUT deleted.*
                            WHERE vacancy_id IN (
            
                            SELECT _yesterday.vacancy_id
                            FROM
                    (
                            SELECT subq.vacancy_id as vacancy_id
                            FROM
                    (
                            SELECT MAX(h.date_published) as date_published,
                    h.vacancy_id,
                            h.project_id
                    FROM publication_history AS h
                    WHERE h.date_published <= @DateToday
                            AND h.project_id = @project_id
                            AND h.date_closed is NULL
                    GROUP BY h.vacancy_id,
                            h.project_id
                                                        ) AS subq
                    INNER JOIN publication_history as h
                    ON h.date_published = subq.date_published
                    AND h.vacancy_id = subq.vacancy_id
                    AND h.project_id = subq.project_id
                                                    ) AS _yesterday
                    left join
                        (
                                SELECT DISTINCT
                                    vacancy_id
                                FROM search_history
                                WHERE search_date = @DateToday
                                AND project_id = @project_id
                        ) as _today
                        ON _today.vacancy_id = _yesterday.vacancy_id
                    WHERE _today.vacancy_id IS NULL
                    )""";

        try {
             //PreparedStatement stmt = sqlConfig.getConnection().prepareStatement(query);
            PreparedStatement stmt = connection.prepareStatement(query);

            //java.sql.Date dateToday = java.sql.Date.valueOf(currentDate.toLocalDate().minusDays(1L));
            stmt.setDate(1, currentDate);
            stmt.setTimestamp(2, java.sql.Timestamp.valueOf(LocalDateTime.now(ClockHolder.getClock())));
            stmt.setLong(3, project_id);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()){
                logger.info("vacancy {} has been closed", rs.getString("hhid"));
            }
            rs.close();
            stmt.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        logger.info("finish closing process {}", LocalDateTime.now(ClockHolder.getClock()).toString());

    }

    public void vacancyClosingProcessWithSQLQueryPostgres(long project_id, java.sql.Date currentDate){

        logger.info("start closing process by SQL-query (postgres) {}", LocalDateTime.now(ClockHolder.getClock()).toString());

        String query = """
                    UPDATE publication_history
                    SET date_closed = ? ,                --#1 @DateToday,
                    logmoment = ?                       --#2 @logmoment
                            
                        WHERE vacancy_id IN
                        (
            
                            SELECT _yesterday.vacancy_id
                            FROM
                                (
                                    SELECT subq.vacancy_id as vacancy_id
                                    FROM
                                        (
                                            SELECT MAX(h.date_published) as date_published,
                                                h.vacancy_id,
                                                h.project_id
                                            FROM publication_history AS h
                                            WHERE h.date_published <= ?         --#3 @DateToday
                                                AND h.project_id = ?        --#4 @project_id
                                                   AND h.date_closed is NULL
                                            GROUP BY h.vacancy_id,
                                                h.project_id
                                        ) AS subq
                                        INNER JOIN publication_history as h
                                            ON h.date_published = subq.date_published
                                                AND h.vacancy_id = subq.vacancy_id
                                                AND h.project_id = subq.project_id
                                ) AS _yesterday
                                left join
                                    (
                                        SELECT DISTINCT
                                            vacancy_id
                                        FROM search_history
                                        WHERE search_date = ?    --#5 @DateToday
                                        AND project_id =  ?      --#6 @project_id
                                    ) as _today
                                   ON _today.vacancy_id = _yesterday.vacancy_id
                            WHERE _today.vacancy_id IS NULL
                        )
                    RETURNING hhid""";


        try {
            //PreparedStatement stmt = sqlConfig.getConnection().prepareStatement(query);
            PreparedStatement stmt = connection.prepareStatement(query);

            //java.sql.Date dateToday = java.sql.Date.valueOf(currentDate.toLocalDate().minusDays(1L));
            stmt.setDate(1, currentDate);
            stmt.setTimestamp(2, java.sql.Timestamp.valueOf(LocalDateTime.now(ClockHolder.getClock())));
            stmt.setDate(3, currentDate);
            stmt.setLong(4, project_id);
            stmt.setDate(5, currentDate);
            stmt.setLong(6, project_id);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()){
                logger.info("vacancy {} has been closed", rs.getString("hhid"));
            }
            rs.close();
            stmt.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        logger.info("finish closing process {}", LocalDateTime.now(ClockHolder.getClock()).toString());

    }


    public void savePublicationBySearchText(long projectId, long stextId, Vacancy vac, Date date_published) {

        Map<String, Long> foundVac = findPublicationBySearchText(projectId, stextId, vac.getHhid());
        if (foundVac.get("id") == -1L) {
            try {
                 //PreparedStatement stmt = sqlConfig.getConnection().prepareStatement("""
                PreparedStatement stmt = connection.prepareStatement("""
                    INSERT INTO
                    publication_history_stext (
                       project_id,
                       searchtext_id,
                       vacancy_id,
                       hhid,
                       date_published,
                       date_closed,
                       logmoment
                       )
                    VALUES (
                       ?,?,?,?,?,?,?
                       )""");

                stmt.setLong(1, projectId);
                stmt.setLong(2, stextId);
                stmt.setLong(3, vac.getId());
                stmt.setString(4, vac.getHhid());
                stmt.setDate(5, date_published);
                stmt.setNull(6, Types.DATE);//closed
                //old
                //stmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
                //new
                stmt.setTimestamp(7, Timestamp.valueOf(
                        LocalDateTime.now(ClockHolder.getClock())
                ));
                stmt.executeUpdate();
                stmt.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            if ((foundVac.get("archived")==1L || foundVac.get("closed")==1L) && vac.getArchived() == 0) {
                // если найдена и это ( вакансия в архиве ИЛИ вакансия закрыта )
                // - делаем открытой (т.к. нашли ее поиском в API)
                setVacancyOpen(vac.getId());
            } else if (foundVac.get("archived")==0L && vac.getArchived() == 1){
                // если найдена и это ( вакансия НЕ в архиве по данным нашего приложения И вакансия в архиве по данным API)
                // - делаем закрытой
                setPublicationClosed(vac.getId());
            }
        }
    }

    // ищет публикацию вакансии в таблице publication_history
    // Возвращает Мэп с полями:
    //      id
    //      archived
    //      closed
    // В этом Мэпе лежат данные только одной вакансии!!
    //
    public Map<String, Long> findPublicationBySearchText(
                long project_id, long stextId, String hhid) {
        Map<String, Long> result = new HashMap<>();
        result.put("id", -1L);

        try {

            //PreparedStatement stmt = sqlConfig.getConnection().prepareStatement("""
            PreparedStatement stmt = connection.prepareStatement("""
                SELECT
                   v.archived
                   ,v.id
                   ,case when h.date_closed is null then 0 else 1 end as closed
                FROM
                   publication_history_stext AS h
                   LEFT JOIN vacancy AS v
                   ON v.id = h.vacancy_id
                WHERE
                   h.project_id = ?
                   AND h.searchtext_id = ?
                   AND h.hhid = ?
                ORDER BY
                   h.date_published DESC
                LIMIT 1""");

            stmt.setLong(1, project_id);
            stmt.setLong(2, stextId);
            stmt.setString(3, hhid);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                result.put("id", rs.getLong("id"));
                result.put("archived", (long)rs.getInt("archived")); // cast int to long
                result.put("closed", rs.getLong("closed"));

                logger.info("findPublicationBySearchText(): successfully found hhid: {}", hhid);

            } else {
                logger.info("findPublicationBySearchText(): NOT FOUND hhid: {}", hhid);
            }
            rs.close();
            stmt.close();
            return result;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public void vacancyClosingProcessWithSQLQueryBySearchText(long projectId, long stextId, Date currentDate) {

        logger.info("start closing process by SQL-query (postgres) {}", LocalDateTime.now(ClockHolder.getClock()).toString());

        String query = """
                    UPDATE publication_history_stext
                    SET date_closed = ? ,                --#1 @DateToday,
                    logmoment = ?                       --#2 @logmoment
                            
                        WHERE vacancy_id IN
                        (
            
                            SELECT _yesterday.vacancy_id
                            FROM
                                (
                                    SELECT subq.vacancy_id as vacancy_id
                                    FROM
                                        (
                                            SELECT MAX(h.date_published) as date_published,
                                                h.vacancy_id,
                                                h.project_id,
                                                h.searchtext_id
                                            FROM publication_history_stext AS h
                                            WHERE h.date_published <= ?         --#3 @DateToday
                                                AND h.project_id = ?        --#4 @project_id
                                                AND h.searchtext_id = ?        --#5 @stextId
                                                   AND h.date_closed is NULL
                                            GROUP BY h.vacancy_id,
                                                h.project_id,
                                                h.searchtext_id
                                        ) AS subq
                                        INNER JOIN publication_history_stext as h
                                            ON h.date_published = subq.date_published
                                                AND h.vacancy_id = subq.vacancy_id
                                                AND h.project_id = subq.project_id
                                                AND h.searchtext_id = subq.searchtext_id
                                ) AS _yesterday
                                left join
                                    (
                                        SELECT DISTINCT
                                            vacancy_id
                                        FROM search_history_stext
                                        WHERE search_date = ?    --#6 @DateToday
                                        AND project_id =  ?      --#7 @project_id
                                        AND searchtext_id =  ?      --#8 @stextId
                                    ) as _today
                                   ON _today.vacancy_id = _yesterday.vacancy_id
                            WHERE _today.vacancy_id IS NULL
                        )
                    RETURNING hhid""";


        try {
             //PreparedStatement stmt = sqlConfig.getConnection().prepareStatement(query);
            PreparedStatement stmt = connection.prepareStatement(query);

            //java.sql.Date dateToday = java.sql.Date.valueOf(currentDate.toLocalDate().minusDays(1L));
            stmt.setDate(1, currentDate);
            stmt.setTimestamp(2, java.sql.Timestamp.valueOf(LocalDateTime.now(ClockHolder.getClock())));
            stmt.setDate(3, currentDate);
            stmt.setLong(4, projectId);
            stmt.setLong(5, stextId);
            stmt.setDate(6, currentDate);
            stmt.setLong(7, projectId);
            stmt.setLong(8, stextId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()){
                logger.info("vacancy {} has been closed", rs.getString("hhid"));
            }
            rs.close();
            stmt.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        logger.info("finish closing process {}", LocalDateTime.now(ClockHolder.getClock()).toString());

    }

}