package com.ens.hhparser5.repository;

import com.ens.hhparser5.configuration.SQLConfig;
import com.ens.hhparser5.model.Employer;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.provider.HibernateUtils;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.SessionFactoryUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class EmployerRepoImpl implements EmployerRepo {

    @Autowired
    private DataSource ds;
    @Autowired
    private SQLConfig sqlConfig;
    @Autowired
    private Connection connection;

    @Override
    public Employer save(Employer employer) {

        // поиск ведем по hhid, т.к. в переданном объекте не заполнен наш id.
        // Он заполняется в этом методе.
        Employer foundEmp = this.findByHhid(employer.getHhid());

        if (foundEmp.getId() == -1L) {
            //          CREATE
            try {
                //PreparedStatement stmt = sqlConfig.getConnection().prepareStatement(
                PreparedStatement stmt = connection.prepareStatement(
                        "INSERT INTO employer (hhid, name, url) VALUES (?,?,?)"
                            , Statement.RETURN_GENERATED_KEYS);

                stmt.setString(1, employer.getHhid());
                stmt.setString(2, employer.getName());
                stmt.setString(3, employer.getUrl());
                stmt.executeUpdate();
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    employer.setId(keys.getLong(1));
                }
                keys.close();
                stmt.close();
                return employer;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            //          UPDATE
            try {
                 //PreparedStatement stmt = sqlConfig.getConnection().prepareStatement(
                PreparedStatement stmt = connection.prepareStatement(
                         "UPDATE employer SET hhid=?,name=?,url=? WHERE id=?"
                         , Statement.RETURN_GENERATED_KEYS);

                stmt.setString(1, employer.getHhid());
                stmt.setString(2, employer.getName());
                stmt.setString(3, employer.getUrl());
                stmt.setLong(4, foundEmp.getId());
                stmt.executeUpdate();
                stmt.close();
                employer.setId(foundEmp.getId());//на всякий случай
                return employer;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public Employer findByHhid(String hhid) {
        try {
            //PreparedStatement stmt =  sqlConfig.getConnection().prepareStatement(
            PreparedStatement stmt =  connection.prepareStatement(
                "SELECT * FROM employer WHERE hhid = ?");
            stmt.setString(1, hhid);
            ResultSet rs = stmt.executeQuery();
            Employer vac = new Employer(-1L);
            if (rs.next()){
                vac.setId(rs.getLong("id"));
                vac.setHhid(hhid);
                vac.setName(rs.getString("name"));
            }
            rs.close();
            stmt.close();
            return vac;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Employer> findAll() {

        List<Employer> result = new ArrayList<>();
        try {
            //PreparedStatement stmt = sqlConfig.getConnection().prepareStatement( "SELECT * FROM employer" );
            Statement stmt = connection.createStatement();
            stmt.execute("SELECT * FROM employer");
            ResultSet rs = stmt.getResultSet();
            while (rs.next()){
                result.add(new Employer(
                        rs.getLong("id"),
                        rs.getString("hhid"),
                        rs.getString("name"),
                        rs.getString("url")
                        )
                );
            }
            rs.close();
            stmt.close();
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Employer findById(long id) {
        try {
            //PreparedStatement stmt =  sqlConfig.getConnection().prepareStatement(
            PreparedStatement stmt =  connection.prepareStatement(
                        "SELECT * FROM employer WHERE id = ?");
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            Employer vac = new Employer(-1L);
            if (rs.next()){
                vac.setId(rs.getLong("id"));
                vac.setHhid(rs.getString("hhid"));
                vac.setName(rs.getString("name"));
            }
            rs.close();
            stmt.close();
            return vac;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(Employer employer) {
        try(PreparedStatement stmt =  connection
                .prepareStatement("DELETE FROM employer WHERE id = ?")) {

            stmt.setLong(1, employer.getId());
            stmt.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
