package com.ens.hhparser5.repository;

import com.ens.hhparser5.model.VacancySource;

public interface VacancySourceRepo {
    void save(VacancySource vacancySource);
    VacancySource findByHhid(String hhid);


}
