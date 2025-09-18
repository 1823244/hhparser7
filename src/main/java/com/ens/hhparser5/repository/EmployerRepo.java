package com.ens.hhparser5.repository;

import com.ens.hhparser5.model.Employer;

import java.util.List;

public interface EmployerRepo {
    Employer save(Employer employer);
    Employer findByHhid(String hhid);
    List<Employer> findAll();
    Employer findById(long id);

    void delete(Employer employer);
}
