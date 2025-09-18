package com.ens.hhparser5.service;

import com.ens.hhparser5.model.Project;
import com.ens.hhparser5.model.SearchText;
import com.ens.hhparser5.repository.SearchTextRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchTextService {

    @Autowired
    private SearchTextRepo searchTextRepo;

    public SearchText findById(long projectId, long stextId){
        return searchTextRepo.findById(projectId, stextId);
    }

    public void save(SearchText searchText, Project project){
        searchTextRepo.save(searchText, project);
    }

    public void delete(long sid, long projectId){
        searchTextRepo.delete(sid, projectId);
    }

    public List<SearchText> findAllByProject(Project project){
        return searchTextRepo.findAllByProject(project);
    }
}
