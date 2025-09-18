package com.ens.hhparser5.service;

import com.ens.hhparser5.model.Region;
import com.ens.hhparser5.repository.RegionRepo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.management.relation.RelationSupport;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Service
public class RegionService {
    @Autowired
    private HttpRequestService httpRequestService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private RegionRepo regionRepo;

    public void doImport() throws JsonProcessingException, SQLException {
        String json = httpRequestService.executeRequestAndGetResultAsString("https://api.hh.ru/areas");
        JsonNode root = objectMapper.readTree(json);

        loadRegionsFromArray(root);
    }

    public void loadRegionsFromArray(JsonNode arrayNode) throws SQLException {
        for (JsonNode element : arrayNode) {
            int id = element.get("id").asInt();
            int parent = element.get("parent_id").asInt();
            String name = element.get("name").textValue();
            JsonNode areas = element.get("areas");

            regionRepo.save(new Region(id,parent,name));

            if (areas.size() != 0) {
                //recursion
                loadRegionsFromArray(areas);
            }
        }
    }

    public Optional<Region> findById(int id) throws SQLException {
        return regionRepo.findById(id);
    }
}
