package com.ens.hhparser5.repository;

import com.ens.hhparser5.service.PublicationHistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

@Component
public class PublicationHistoryRepoImpl implements PublicationHistoryRepo{

    private Logger logger = LoggerFactory.getLogger(PublicationHistoryRepoImpl.class);

    @Autowired
    private Connection connection;

    @Override
    public Map<String, Long> findPublication(long projectId, String hhid) {
        Map<String, Long> result = new HashMap<>();
        result.put("id", -1L);
        try {
            PreparedStatement stmt = connection.prepareStatement("""                             
                SELECT
                   v.archived
                   ,v.id
                   ,case when h.date_closed is null then 0 else 1 end as closed
                FROM
                   publication_history AS h
                   LEFT JOIN vacancy AS v
                   ON v.id = h.vacancy_id
                WHERE
                   h.project_id = ?
                   AND h.hhid = ?
                ORDER BY
                   h.date_published DESC
                LIMIT 1""");
            stmt.setLong(1, projectId);
            stmt.setString(2, hhid);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                result.put("id", rs.getLong("id"));
                result.put("archived", (long)rs.getInt("archived"));
                result.put("closed", rs.getLong("closed"));
                logger.info("findPublication(): successfully found hhid: {}", hhid);
            } else {
                logger.info("findPublication(): NOT FOUND hhid: {}", hhid);
            }
            rs.close();
            stmt.close();
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
