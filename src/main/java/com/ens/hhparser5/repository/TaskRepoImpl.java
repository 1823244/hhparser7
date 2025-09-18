package com.ens.hhparser5.repository;

import com.ens.hhparser5.configuration.SQLConfig;
import com.ens.hhparser5.model.Task;
import com.ens.hhparser5.model.User;
import com.ens.hhparser5.utility.AuthUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class TaskRepoImpl implements TaskRepo{

    @Autowired
    private AuthUser authUser;
    @Autowired
    private SQLConfig sqlConfig;
    @Autowired
    private ProjectRepo projectRepo;
    @Autowired
    private Connection connection;

    private void fillTheFields(Task task, ResultSet rs) throws SQLException {
        task.setId(rs.getLong("id"));
        task.setName(rs.getString("name"));
        task.setProjectId(rs.getLong("project_id"));
        if (rs.getTimestamp("startmoment") != null){
            task.setStartTime(rs.getTimestamp("startmoment").toLocalDateTime());
        }
        if (rs.getTimestamp("endmoment") != null){
            task.setEndTime(rs.getTimestamp("endmoment").toLocalDateTime());
        }
        task.setProject(projectRepo.findById(rs.getLong("project_id")));

    }

    @Override
    public Task save(Task task) {
        Task taskFound = this.findById(task.getId());
        if (taskFound == null) {
            return create(task);
        } else {
            return update(task, taskFound);
        }
    }

    @Override
    public Task create(Task task) {
        Task newTask = new Task();
        try {
             //PreparedStatement stmt = sqlConfig.getConnection().prepareStatement(
            PreparedStatement stmt = connection.prepareStatement(
                     """
                     INSERT INTO task (name,username,project_id,
                     starttime,startdate,startmoment,
                     endtime,enddate,endmoment,userid)
                     VALUES (?,?,?,?,?,?,?,?,?,?)
                     """
                     , Statement.RETURN_GENERATED_KEYS);

                stmt.setString(1, task.getName());
                stmt.setString(2, task.getUser());
                if (task.getProject() == null){
                    stmt.setLong(3, task.getProjectId());
                } else {
                    stmt.setLong(3, task.getProject().getId());
                }

                stmt.setTime(4, java.sql.Time.valueOf(task.getStartTime().toLocalTime()));
                stmt.setDate(5, java.sql.Date.valueOf(task.getStartTime().toLocalDate()));
                stmt.setTimestamp(6,java.sql.Timestamp.valueOf(task.getStartTime()));
                if (task.getEndTime() != null) {
                    stmt.setTime(7, java.sql.Time.valueOf(task.getEndTime().toLocalTime()));
                    stmt.setDate(8, java.sql.Date.valueOf(task.getEndTime().toLocalDate()));
                    stmt.setTimestamp(9, java.sql.Timestamp.valueOf(task.getEndTime()));
                } else {
                    stmt.setNull(7, Types.TIMESTAMP);
                    stmt.setNull(8, Types.TIMESTAMP);
                    stmt.setNull(9, Types.TIMESTAMP);
                }
            stmt.setLong(10, task.getUserId());
            stmt.executeUpdate();

            newTask.setName(task.getName());
            newTask.setUser(task.getUser());
            newTask.setProjectId(task.getProjectId());
            newTask.setProject(task.getProject());
            newTask.setStartTime(task.getStartTime());
            newTask.setEndTime(task.getEndTime());
            newTask.setUserId(task.getUserId());
            // если создавали новый объект, нужно получить созданный id из базы данных
            // и добавить его к dto
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                newTask.setId(keys.getLong(1));
            }
            keys.close();
            stmt.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return newTask;
    }

    @Override
    public Task update(Task task, Task taskFound) {
        Task newTask = new Task();
        try {
             //PreparedStatement stmt = sqlConfig.getConnection().prepareStatement(
            PreparedStatement stmt = connection.prepareStatement(
                """
                    UPDATE task SET 
                        name=?,
                        username=?,
                        project_id=?,
                        startmoment=?,
                        endmoment=?,
                        userid=?  
                    WHERE id = ?
                    """);

            stmt.setString(1, task.getName());
            stmt.setString(2, task.getUser());
            if (task.getProject() == null){
                stmt.setLong(3, task.getProjectId());
            } else {
                stmt.setLong(3, task.getProject().getId());
            }
            if (task.getStartTime() != null) {
                stmt.setTimestamp(4, java.sql.Timestamp.valueOf(task.getStartTime()));
            } else {
                stmt.setNull(4, Types.TIMESTAMP);
            }
            if (task.getEndTime() != null) {
                stmt.setTimestamp(5, java.sql.Timestamp.valueOf(task.getEndTime()));
            } else {
                stmt.setNull(5, Types.TIMESTAMP);
            }
            if (task.getUserId() == 0L){
                throw new RuntimeException("there is no UserId in Task!");
            } else {
                stmt.setLong(6, task.getUserId());
            }

            stmt.setLong(7, task.getId());

            stmt.executeUpdate();

            newTask.setName(task.getName());
            newTask.setUser(task.getUser());
            newTask.setProjectId(task.getProjectId());
            newTask.setProject(task.getProject());
            newTask.setStartTime(task.getStartTime());
            newTask.setEndTime(task.getEndTime());
            newTask.setUserId(task.getUserId());
            newTask.setId(taskFound.getId());
            stmt.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return newTask;
    }


    @Override
    public Task findById(long id) {
        Task task = null;
        try {
                //PreparedStatement stmt = sqlConfig.getConnection().prepareStatement(
            PreparedStatement stmt = connection.prepareStatement(
                        """
                    SELECT * FROM task WHERE id = ?
                        """ );

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()){
                task = new Task();
                fillTheFields(task, rs);
            }
            rs.close();
            stmt.close();
            return task;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Task findLast(User user) {

        Task task = null;
        try {
            //PreparedStatement stmt = sqlConfig.getConnection().prepareStatement(
            PreparedStatement stmt = connection.prepareStatement(
                        """
                    SELECT * FROM task WHERE userid = ? ORDER BY startmoment DESC LIMIT 1
                        """ );
            stmt.setLong(1, user.getId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()){
                task = findById(rs.getLong("id"));
            }
            rs.close();
            stmt.close();
            return task;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Task> findAll(User user) {
        List<Task> list = new ArrayList<>();
        try {
            //PreparedStatement stmt = sqlConfig.getConnection().prepareStatement(
            PreparedStatement stmt = connection.prepareStatement(
                """
                        SELECT * FROM task WHERE userid = ?
                    """ );
            stmt.setLong(1, user.getId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()){
                Task task = new Task();
                fillTheFields(task, rs);
                list.add(task);
            }
            rs.close();
            stmt.close();
            return list;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
