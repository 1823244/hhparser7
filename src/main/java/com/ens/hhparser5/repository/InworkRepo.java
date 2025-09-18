package com.ens.hhparser5.repository;

import com.ens.hhparser5.model.Inwork;

import java.util.List;

public interface InworkRepo {

    public Inwork save(Inwork inwork);

    public Inwork findById(long id);

    public List<Inwork> findAll();


}
