package com.ens.hhparser5;


import com.ens.hhparser5.model.Inwork;
import com.ens.hhparser5.repository.InworkRepoImpl;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;

@SpringBootTest
public class InworkRepoTest {

    @Autowired
    private InworkRepoImpl inworkRepo;
    @Test
    public void testUpdateCommand(){
        Inwork inwork = new Inwork();
        inwork.setVacancyId(654654L);
        inwork.setVacancyHhid("321654987");
        inworkRepo.save(inwork);

        Assert.assertNull(inwork.getEndDate());

        long id = inwork.getId();

        inwork = inworkRepo.findById(id);
        inwork.setEndDate(java.util.Date.from(Instant.parse("2023-12-31T00:00:00.00Z")));
        inworkRepo.save(inwork);

        Assert.assertEquals(java.util.Date.from(Instant.parse("2023-12-31T00:00:00.00Z")), inwork.getEndDate());
    }

}
