package com.ens.hhparser5.repository;

import com.ens.hhparser5.model.Project;
import com.ens.hhparser5.model.SearchText;

import java.util.List;

public interface SearchTextRepo {
    SearchText save(SearchText searchText, Project project);
    List<SearchText> findAllByProject(Project project);
    void delete(long id, long projectId);
    SearchText findByName(long projectId, String name);
    SearchText findById(long projectId, long stextId);


}
