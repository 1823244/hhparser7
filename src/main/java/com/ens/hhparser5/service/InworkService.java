package com.ens.hhparser5.service;

import com.ens.hhparser5.model.Inwork;
import com.ens.hhparser5.repository.InworkRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InworkService {

    @Autowired
    private InworkRepo inworkRepo;

    public Inwork save(Inwork inwork){
        return inworkRepo.save(inwork);
    }

    public Inwork findById(long id){
        return inworkRepo.findById(id);
    }

    public List<Inwork> findAll(){
        return inworkRepo.findAll();

    }
}
