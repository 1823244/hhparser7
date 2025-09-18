// https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/jdbc/core/JdbcTemplate.html
// https://www.javatpoint.com/spring-JdbcTemplate-tutorial
package com.ens.hhparser5.repository;

import com.ens.hhparser5.model.Inwork;
import com.ens.hhparser5.mappers.RowMapperInwork;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;

@Component
public class InworkRepoImpl implements InworkRepo{

    private JdbcTemplate jdbcTemplate;
    private DataSource ds;

    private String SQL_INSERT = "INSERT INTO inwork (vacancy_id,hhid) VALUES (?,?)";
    private String SQL_UPDATE = "UPDATE inwork SET vacancy_id=?,hhid=?,endtime=? WHERE id=?";

    @Autowired
    public InworkRepoImpl(DataSource ds) {
        this.ds = ds;
        this.jdbcTemplate = new JdbcTemplate(ds);
    }

    @Override
    public Inwork save(Inwork inwork){
        if (inwork.getId() == 0L) {
            // create

            try (
                    Connection conn = DataSourceUtils.getConnection(ds);
                    PreparedStatement stmt = conn.prepareStatement( SQL_INSERT, Statement.RETURN_GENERATED_KEYS )
            ) {
                stmt.setLong(1, inwork.getVacancyId());
                stmt.setString(2, inwork.getVacancyHhid());
                stmt.execute();
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    inwork.setId(keys.getLong(1));
                }
                keys.close();
                return inwork;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

        } else {
            // update

            jdbcTemplate.update(SQL_UPDATE,
                    inwork.getVacancyId(),
                    inwork.getVacancyHhid(),
                    java.sql.Date.from(inwork.getEndDate().toInstant()),
                    inwork.getId());
        }

        return inwork;
    }

    @Override
    public Inwork findById(long id){
        Inwork inwork = jdbcTemplate.queryForObject(
                "SELECT * FROM inwork WHERE id = ?", new RowMapperInwork(), id);
        return inwork;
    }

    @Override
    public List<Inwork> findAll(){
        List<Inwork> list = jdbcTemplate.query(
                "SELECT id,vacancy_id,hhid as vacancyhhid FROM inwork", new BeanPropertyRowMapper<Inwork>(Inwork.class));
        return list;

    }
}

