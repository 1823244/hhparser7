package com.ens.hhparser5;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@SpringBootTest
public class RegionsTest {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private Connection connection;

    @Test
    public void testRegionImport() throws JsonProcessingException, SQLException {


        String json = """
                [
                      {
                            "id": "113",
                            "parent_id": null,
                            "name": "Россия",
                            "areas": [
                                  {
                                        "id": "1620",
                                        "parent_id": "113",
                                        "name": "Республика Марий Эл",
                                        "areas": [
                                              {
                                                    "id": "4228",
                                                    "parent_id": "1620",
                                                    "name": "Виловатово",
                                                    "areas": [ ]
                                              },
                                              {
                                                    "id": "1621",
                                                    "parent_id": "1620",
                                                    "name": "Волжск",
                                                    "areas": [ ]
                                              },
                                              {
                                                    "id": "1622",
                                                    "parent_id": "1620",
                                                    "name": "Звенигово",
                                                    "areas": [ ]
                                              }
                                        ]
                                  }
                            ]
                      },
                      {
                            "id": "40",
                            "parent_id": null,
                            "name": "Казахстан",
                            "areas": [
                                  {
                                        "id": "6251",
                                        "parent_id": "40",
                                        "name": "Абай",
                                        "areas": [ ]
                                  },
                                  {
                                        "id": "6782",
                                        "parent_id": "40",
                                        "name": "Айет",
                                        "areas": [ ]
                                  }
                            ]
                      }
                ]                                           
                                            """;


        //PreparedStatement ps = connection.prepareStatement("delete from regions");
        //ps.executeUpdate();

        JsonNode root = objectMapper.readTree(json);
        //loadRegionsFromArray(root);

    }

    public void loadRegionsFromArray(JsonNode arrayNode) throws SQLException {
        for (JsonNode element : arrayNode) {
            int id = element.get("id").asInt();
            int parent = element.get("parent_id").asInt();
            String name = element.get("name").textValue();
            JsonNode areas = element.get("areas");

            PreparedStatement ps = connection.prepareStatement("insert into regions (id,parent_id,name) values (?,?,?)");
            ps.setInt(1,id);
            ps.setInt(2, parent);
            ps.setString(3, name);
            ps.executeUpdate();

            if (areas.size() != 0) {
                //recursion
                loadRegionsFromArray(areas);
            }
        }
    }
}
