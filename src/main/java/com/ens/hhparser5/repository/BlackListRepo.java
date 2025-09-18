package com.ens.hhparser5.repository;

import com.ens.hhparser5.model.OpenVacancy;
import com.ens.hhparser5.model.Vacancy;

import java.util.List;

public interface BlackListRepo {
    void addToBlackList(long vacancyId);
    void removeFromBlackList(long vacancyId);
    List<OpenVacancy> findAll();
    Vacancy findById(long vacancyId);
}
