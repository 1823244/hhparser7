package com.ens.hhparser5.repository;

import com.ens.hhparser5.configuration.SQLConfig;
import com.ens.hhparser5.model.VacancySource;
import lombok.Synchronized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;

@Component
public class VacancySourceRepoImpl implements VacancySourceRepo{

    private final Logger logger = LoggerFactory.getLogger(VacancySourceRepoImpl.class);

    @Autowired
    private DataSource ds;
    @Autowired
    private SQLConfig sqlConfig;
    @Autowired
    private Connection connection;

    /**
     * Ищет исходник вакансии в таблице vacancy_source - класс VacancySourceDto
     * Если находит - обновляет
     * Если не находит - создает новую, устанавливает поле id значением из базы данных
     */
    @Override
    public void save(VacancySource vacancySource) {
        // поиск ведем по hhid, т.к. в переданном объекте не заполнен наш id.
        VacancySource foundVac = findByHhid(vacancySource.getHhid());

        try {
            PreparedStatement stmt;
            if (foundVac.getId() == -1L) {
                stmt = getInsertStatementDriverManager();
            } else {
                stmt = getUpdateStatementDriverManager();
                stmt.setLong(3, foundVac.getId());
            }
            stmt.setString(1, vacancySource.getHhid());
            stmt.setString(2, vacancySource.getJson());

            stmt.executeUpdate();
            stmt.close();
            logger.info("saved vacancy source: {}", vacancySource.getHhid());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public VacancySource findByHhid(String hhid){

        try {
            //PreparedStatement stmt = DataSourceUtils.getConnection(ds).prepareStatement(
            //PreparedStatement stmt = sqlConfig.getConnection().prepareStatement(
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT * FROM vacancy_source WHERE hhid = ?");
            stmt.setString(1, hhid);
            ResultSet rs = stmt.executeQuery();
            VacancySource vac = new VacancySource(-1L);

            if (rs.next()){
                vac.setId(rs.getLong("id"));
                vac.setHhid(hhid);
                vac.setJson(rs.getString("json"));
            }
            rs.close();
            stmt.close();
            return vac;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //--------------------  INSERT

    /**
     * ADO-команда для создания новой вакансии
     * @return
     * @throws SQLException
     */
    @Synchronized
    public PreparedStatement getInsertStatement() throws Exception {

            return //DataSourceUtils.getConnection(ds).prepareStatement(
                    connection.prepareStatement(
                    "INSERT INTO vacancy_source (hhid, json) VALUES (?,?)", Statement.RETURN_GENERATED_KEYS);

    }

    //--------------------  UPDATE
    /**
     * ADO-команда для обновления вакансии
     * @return
     * @throws SQLException
     */
    @Synchronized
    public PreparedStatement getUpdateStatement() throws Exception {

        return //DataSourceUtils.getConnection(ds).prepareStatement(
                connection.prepareStatement(
                    "UPDATE vacancy_source SET hhid=?, json=? WHERE id=?");

    }

//--------------------  INSERT DriverManager

    /**
     * ADO-команда для создания новой вакансии
     * @return
     * @throws SQLException
     */
    @Synchronized
    public PreparedStatement getInsertStatementDriverManager() throws Exception {

        return //sqlConfig.getConnection().prepareStatement(
                connection.prepareStatement(
                "INSERT INTO vacancy_source (hhid, json) VALUES (?,?)", Statement.RETURN_GENERATED_KEYS);

    }

    //--------------------  UPDATE DriverManager
    /**
     * ADO-команда для обновления вакансии
     * @return
     * @throws SQLException
     */
    @Synchronized
    public PreparedStatement getUpdateStatementDriverManager() throws Exception {

        return //sqlConfig.getConnection().prepareStatement(
                connection.prepareStatement(
                "UPDATE vacancy_source SET hhid=?, json=? WHERE id=?");

    }
}

