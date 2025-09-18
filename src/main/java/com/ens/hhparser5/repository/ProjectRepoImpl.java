package com.ens.hhparser5.repository;

import com.ens.hhparser5.model.Project;
import com.ens.hhparser5.model.User;
import com.ens.hhparser5.utility.AuthUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class ProjectRepoImpl implements ProjectRepo{
    @Autowired
    private DataSource ds;
    @Autowired
    private Connection connection;

    public Project saveOrUpdate(Project project){
        if (project.getId() != 0L) {
            update(project);
        } else {
            save(project);
        }
        return project;
    }

    // создает новый
    @Override
    public Project save(Project project) {
        try (PreparedStatement stmt = connection.prepareStatement(
                     "INSERT INTO project (name,userid) VALUES (?,?)",
                     Statement.RETURN_GENERATED_KEYS )) {
            stmt.setString(1, project.getName());
            stmt.setLong(2, project.getUserId());
            stmt.execute();
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                project.setId(keys.getLong(1));
            }
            keys.close();
            return project;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Project findByName(String name) {
        try (PreparedStatement stmt = connection.prepareStatement( "SELECT id, name, userid FROM project WHERE name = ?")){
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            Project project = null;
            if (rs.next()){
                project = new Project(rs.getLong("id"), rs.getString("name"));
                project.setUserId(rs.getLong("userid"));
            } else {
                project = null;//if nothing was found
            }
            rs.close();
            return project;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Project findById(long id) {
        Project project = null;
        try (PreparedStatement stmt = connection.prepareStatement(
                        "SELECT id, name, userid FROM project WHERE id = ?" )) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()){
                project = new Project(rs.getLong("id"), rs.getString("name"));
                project.setUserId(rs.getLong("userid"));
            }
            rs.close();
            return project;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Project> findAll(User user) {
        List<Project> projects = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement( "SELECT id, name FROM project WHERE userid = ?" )) {
            stmt.setLong(1, user.getId());
            stmt.execute();
            ResultSet rs = stmt.getResultSet();
            while (rs.next()){
                Project project = new Project(rs.getLong("id"), rs.getString("name"));
                project.setUserId(user.getId());
                projects.add(project);
            }
            rs.close();
            return projects;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(Project project) {
        try (PreparedStatement stmt = connection.prepareStatement( "UPDATE project SET name=? WHERE id=?" )) {
            stmt.setString(1, project.getName());
            stmt.setLong(2, project.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(Project project) {
        try (PreparedStatement stmt = connection.prepareStatement( "DELETE FROM project WHERE id=?" )) {
            stmt.setLong(1, project.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
