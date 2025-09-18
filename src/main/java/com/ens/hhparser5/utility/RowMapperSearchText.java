package com.ens.hhparser5.utility;

import com.ens.hhparser5.model.SearchText;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class RowMapperSearchText implements RowMapper<SearchText> {

    /**
     * Implementation of RowMapper for concrete case: ResultSet of SearchText
     *
     * @param rs the ResultSet to map (pre-initialized for the current row)
     * @param i the number of the current row
     * @return Object which is a representation of "hh.ru search text value"
     * @throws SQLException sql exception
     */
    @Override
    public SearchText mapRow(ResultSet rs, int i) throws SQLException {
        SearchText searchText = new SearchText();
        searchText.setText(rs.getString("text"));
        searchText.setProjectId(rs.getLong("project_id"));
        searchText.setId(rs.getLong("id"));

        return searchText;
    }
}
