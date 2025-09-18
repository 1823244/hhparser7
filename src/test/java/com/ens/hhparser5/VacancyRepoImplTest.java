package com.ens.hhparser5;

import com.ens.hhparser5.model.Vacancy;
import com.ens.hhparser5.repository.VacancyRepo;
import com.ens.hhparser5.service.VacancyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Connection;
import java.sql.SQLException;

@SpringBootTest
public class VacancyRepoImplTest {

    @Autowired
    private Connection connection;
    //@Autowired
    //private DataSource ds;
    @Autowired
    private VacancyRepo vacancyRepo;

    @Autowired
    VacancyService vacancyService;

    /*
    Проверяет, что вакансия создается правильно:
    1. что она в принципе создается в базе данных
    2. что не создаются дубли
     */
    @Test
    public void testSaveOrUpdate() throws SQLException {


        Vacancy vacancy = Vacancy.builder()
                .id(123456789L)
                .name("Test vacancy for VacancyRepoImplTest")
                .hhid("9876541")
                .build();

        //var vac = vacancyService.findByHhid("123456789");

        // в первый раз она создается
        this.vacancyRepo.saveOrUpdate(vacancy);
        //checkIfTheVacancyExists(vacancy);

        // во второй раз - обновляется
        // не должно быть дубля
        this.vacancyRepo.saveOrUpdate(vacancy);
        //checkIfTheVacancyExistsAndIsUnique(vacancy);



    }

}
