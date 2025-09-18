package com.ens.hhparser5.service;

import com.ens.hhparser5.configuration.SQLConfig;
import com.ens.hhparser5.model.Vacancy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class SearchHistoryService {

    @Autowired
    private SQLConfig sqlConfig;
    @Autowired
    private Connection connection;


    /**
     * Inserts new record into table Search_History
     *
     * @param project_id - long
     * @param vac -
     * @param search_date - java.sql.Date - usually, current date. The date defines in the beginning of
     *                    the import process, to avoid it amendment if we pass through midnight
     */
    public void addNewSearchHistoryRecord(long project_id, Vacancy vac, Date search_date){

        try {
            PreparedStatement stmt = connection.prepareStatement(//sqlConfig.getConnection().prepareStatement(
                    "INSERT INTO search_history (search_date, project_id, vacancy_id, hhid) VALUES (?,?,?,?)"
                    , Statement.RETURN_GENERATED_KEYS);
            stmt.setDate(1, search_date);
            stmt.setLong(2, project_id);
            stmt.setLong(3, vac.getId());
            stmt.setString(4, vac.getHhid());

            stmt.executeUpdate();
            stmt.close();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public void clearHistory(){

        try(PreparedStatement stmt = connection//sqlConfig.getConnection()
                .prepareStatement( "DELETE FROM search_history" )) {
            stmt.execute();
            stmt.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Returns search history for specific date, usually, Today (day of starting the import process)
     *
     * @param project_id - long
     * @param reportDate - java.sql.Date
     * @return - the list where the filtered by one project vacancies are collected
     */
    public List<Vacancy> getSearchHistory(long project_id, Date reportDate) {

        List<Vacancy> vacsList = new ArrayList<>();

        try {
            PreparedStatement stmt = connection//sqlConfig.getConnection()
                    .prepareStatement(
                    "SELECT DISTINCT " +
                            "   vacancy_id, " +
                            "   hhid " +
                            "FROM " +
                            "   search_history " +
                            "WHERE " +
                            "   search_date = ? " +
                            "   AND project_id = ?"
            );
            stmt.setDate(1, reportDate);
            stmt.setLong(2, project_id);
            stmt.execute();
            ResultSet rs = stmt.getResultSet();
            while (rs.next()) {
                Vacancy vac = new Vacancy();
                vac.setId(rs.getLong("vacancy_id"));
                vac.setHhid(rs.getString("hhid"));

                vacsList.add(vac);
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return vacsList;
    }

    public void clearHistoryByProject(long id) {

        try {
            PreparedStatement stmt = connection//sqlConfig.getConnection()
                    .prepareStatement( "DELETE FROM search_history WHERE project_id = ?" );

            stmt.setLong(1, id);
            stmt.execute();
            stmt.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void addNewSearchHistoryRecordBySearchText(long projectId, long stextId, Vacancy vac, Date search_date) {
        try {
             PreparedStatement stmt = connection//sqlConfig.getConnection()
                .prepareStatement(
                "INSERT INTO search_history_stext (search_date, project_id, searchtext_id, vacancy_id, hhid) VALUES (?,?,?,?,?)"
                , Statement.RETURN_GENERATED_KEYS);

            stmt.setDate(1, search_date);
            stmt.setLong(2, projectId);
            stmt.setLong(3, stextId);
            stmt.setLong(4, vac.getId());
            stmt.setString(5, vac.getHhid());

            stmt.executeUpdate();
            stmt.close();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
