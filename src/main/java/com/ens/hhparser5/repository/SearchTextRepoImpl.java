package com.ens.hhparser5.repository;

import com.ens.hhparser5.model.Project;
import com.ens.hhparser5.model.SearchText;
import com.ens.hhparser5.configuration.SQLConfig;
import com.ens.hhparser5.utility.RowMapperSearchText;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class SearchTextRepoImpl implements SearchTextRepo{
    @Autowired
    private SQLConfig sqlConfig;
    @Autowired
    private DataSource ds;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public SearchText save(SearchText searchText, Project project) {
        try (
                Connection conn = DataSourceUtils.getConnection(ds);
                PreparedStatement stmt = conn.prepareStatement( "INSERT INTO project_search_text (name, project_id) VALUES (?,?)", Statement.RETURN_GENERATED_KEYS)
        ) {
                stmt.setString(1, searchText.getText());
                stmt.setLong(2, project.getId());
                stmt.executeUpdate();
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    searchText.setId(keys.getLong(1));
                }
                keys.close();
                return searchText;

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

    }

    /**
     * #learningjava_jdbctemplate_query1
     * Получение списка объектов через Spring JdbcTemplate
     * Запрос с одним параметром
     * @param project
     * @return
     */
    @Override
    public List<SearchText> findAllByProject(Project project) {
        String queryText = """ 
                    SELECT
                       t.id as id,
                       t.name as text,
                       project.id as project_id
                    FROM
                       project_search_text as t
                       left join project 
                       on project.id = t.project_id
                    WHERE
                        t.project_id = ?
                    """;

        List<SearchText> result = jdbcTemplate.query(queryText,
                new RowMapperSearchText(),
                new Object[]{project.getId()});
        return result;

    }

    @Override
    public void delete(long id, long projectId) {
        try (
                Connection conn = DataSourceUtils.getConnection(ds);
                PreparedStatement stmt = conn.prepareStatement( "DELETE FROM project_search_text WHERE id=? AND project_id=?")
        ) {
            stmt.setLong(1, id);
            stmt.setLong(2, projectId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public SearchText findByName(long projectId, String searchText){
        try  {
            Connection conn = DataSourceUtils.getConnection(ds);
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT * FROM project_search_text WHERE project_id = ? and name = ?");
            stmt.setLong(1, projectId);
            stmt.setString(2, searchText);
            ResultSet rs = stmt.executeQuery();
            SearchText result = new SearchText(-1L,-1L,null);//if nothing was found
            while (rs.next()){
                SearchText searchTextDto = new SearchText(
                        rs.getLong("id"),
                        rs.getLong("project_id"),
                        rs.getString("name")
                );
                result = searchTextDto;
                break;
            }
            rs.close();
            stmt.close();
            DataSourceUtils.releaseConnection(conn, ds);
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public SearchText findById(long projectId, long stextId) {
        try (
                Connection conn = DataSourceUtils.getConnection(ds);
                PreparedStatement stmt = conn.prepareStatement( "SELECT * FROM project_search_text WHERE project_id = ? and id = ?")
        ) {
            stmt.setLong(1, projectId);
            stmt.setLong(2, stextId);
            ResultSet rs = stmt.executeQuery();
            SearchText result = new SearchText(-1L,-1L,null);//if nothing was found
            while (rs.next()){
                SearchText searchText = new SearchText(
                        rs.getLong("id"),
                        rs.getLong("project_id"),
                        rs.getString("name")
                );
                result = searchText;
                break;
            }
            rs.close();
            return result;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
