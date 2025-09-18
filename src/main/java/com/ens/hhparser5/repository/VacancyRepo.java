package com.ens.hhparser5.repository;

import com.ens.hhparser5.model.Vacancy;

public interface VacancyRepo {
    Vacancy saveOrUpdate(Vacancy vacancy);
    Vacancy findByHhid(String hhid);
    Vacancy findById(long vacancyId);
}
