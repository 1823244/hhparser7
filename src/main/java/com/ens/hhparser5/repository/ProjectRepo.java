package com.ens.hhparser5.repository;

import com.ens.hhparser5.model.Project;
import com.ens.hhparser5.model.User;

import java.util.List;

public interface ProjectRepo {
    Project saveOrUpdate(Project project);
    Project save(Project project);
    Project findById(long id);
    Project findByName(String name);
    List<Project> findAll(User user);
    void update(Project project);
    void delete(Project project);
}
