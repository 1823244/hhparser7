package com.ens.hhparser5.service;

import com.ens.hhparser5.model.OpenVacancy;
import com.ens.hhparser5.model.Vacancy;
import com.ens.hhparser5.repository.BlackListRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BlackListService {

    @Autowired
    private BlackListRepo blackListRepo;

    public Vacancy findById(long vacancyId){
        return blackListRepo.findById(vacancyId);
    }

    public void addToBlackList(long vacancyId) {
        blackListRepo.addToBlackList(vacancyId);
    }

    public List<OpenVacancy> findAll() {
        return blackListRepo.findAll();
    }

    public void removeFromBlackList(long vacancyId) {
        blackListRepo.removeFromBlackList(vacancyId);
    }
}
