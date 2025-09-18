package com.ens.hhparser5.repository;

import com.ens.hhparser5.model.Employer;
import com.ens.hhparser5.model.OpenVacancy;
import com.ens.hhparser5.model.Vacancy;
import com.ens.hhparser5.utility.ClockHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class BlackListRepoImpl implements BlackListRepo{

    @Autowired
    private DataSource ds;
    @Autowired
    private VacancyRepo vacancyRepo;
    @Autowired
    private EmployerRepo employerRepo;

    @Override
    public void addToBlackList(long vacancyId) {
        try (Connection conn = DataSourceUtils.getConnection(ds)) {

            var vacancyDto = vacancyRepo.findById(vacancyId);

            PreparedStatement stmt = conn.prepareStatement("""
                    DELETE FROM blacklist WHERE vacancy_id = ?
                    """);

            stmt.setLong(1, vacancyDto.getId());
            stmt.executeUpdate();

            stmt = conn.prepareStatement("""
                    INSERT INTO blacklist (vacancy_id,hhid,logmoment) VALUES (?,?,?)
                    """);

            stmt.setLong(1, vacancyDto.getId());
            stmt.setString(2, vacancyDto.getHhid());
            stmt.setTimestamp(3,java.sql.Timestamp.from(Instant.now(ClockHolder.getClock())));

            stmt.executeUpdate();

            stmt.close();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void removeFromBlackList(long vacancyId) {
        try (Connection conn = DataSourceUtils.getConnection(ds)) {

            PreparedStatement stmt = conn.prepareStatement("""
                    DELETE FROM blacklist WHERE vacancy_id = ?
                    """);

            stmt.setLong(1, vacancyId);
            stmt.executeUpdate();

            stmt.close();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public List<OpenVacancy> findAll() {
        List<OpenVacancy> list = new ArrayList<>();

        try (Connection conn = DataSourceUtils.getConnection(ds)) {

            PreparedStatement stmt = conn.prepareStatement("""
                    SELECT * FROM blacklist ORDER BY logmoment DESC
                    """);

            ResultSet rs = stmt.executeQuery();
            int count = 1;

            while (rs.next()) {
                Vacancy vacDto = vacancyRepo.findById(rs.getLong("vacancy_id"));

                OpenVacancy v = new OpenVacancy();

                Employer emp = employerRepo.findById(vacDto.getEmployer());
                v.setId(vacDto.getId());
                v.setName(vacDto.getName());
                v.setUrl(vacDto.getUrl());
                v.setHhid(vacDto.getHhid());
                v.setEmployer(emp.getName());
                v.setSalary_netto(vacDto.getSalary_to());
                v.setEmployer_hhid(emp.getHhid());
                v.setEmployer_link("https://hh.ru/employer/"+emp.getHhid());
                v.setCount(count++);
                list.add(v);
            }
            rs.close();
            stmt.close();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    // вернет объект вакансии, если она есть в таблице blacklist
    @Override
    public Vacancy findById(long vacancyId) {

        Vacancy vacDto = null;

        try (Connection conn = DataSourceUtils.getConnection(ds)) {

            PreparedStatement stmt = conn.prepareStatement("""
                    SELECT * FROM blacklist WHERE vacancy_id = ?
                    """);

            stmt.setLong(1, vacancyId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                vacDto = vacancyRepo.findById(rs.getLong("vacancy_id"));
            }
            rs.close();
            stmt.close();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return vacDto;
    }

}
