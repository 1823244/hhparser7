package com.ens.hhparser5.mappers;

import com.ens.hhparser5.model.Inwork;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class RowMapperInwork implements RowMapper<Inwork> {

    @Override
    public Inwork mapRow(ResultSet rs, int rowNum) throws SQLException {
        Inwork inwork = new Inwork();
        inwork.setId(rs.getLong("id"));
        inwork.setStatus("");
        inwork.setVacancyId(rs.getLong("vacancy_id"));
        inwork.setVacancyHhid(rs.getString("hhid"));
        return inwork;
    }
}
