package com.ens.hhparser5.restcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@SuppressWarnings(value = "unused")
@RestController
@RequestMapping("main")
public class MainRestController {

    @Autowired
    private DataSource ds;

    // удаляет всё, кроме Projects, SearchTexts
    @GetMapping("cleandb")
    public String cleanDatabase() {

        List<String> queries = new ArrayList<>();
        queries.add("truncate table employer;");
        queries.add("truncate table search_history;");
        queries.add("truncate table vacancy;");
        queries.add("truncate table vacancy_source;");
        queries.add("truncate table publication_history;");
        queries.add("truncate table publication_history_stext;");
        queries.add("truncate table publication_history_stext;");

        for (String q:queries
             ) {
            try (   Connection conn = DataSourceUtils.getConnection(ds);
                    PreparedStatement stmt = conn.prepareStatement(q)) {
                stmt.executeUpdate();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return "truncate has been done at: "+ Calendar.getInstance().getTime();
    }

}
