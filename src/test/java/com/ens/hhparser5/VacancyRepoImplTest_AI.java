package com.ens.hhparser5;

import com.ens.hhparser5.model.Vacancy;
import com.ens.hhparser5.repository.VacancyRepoImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class VacancyRepoImplTest_AI {

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @InjectMocks
    private VacancyRepoImpl vacancyRepo;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(connection.prepareStatement(anyString(), anyInt())).thenReturn(preparedStatement);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
    }

    @Test
    void testSaveOrUpdate_InsertNewVacancy() throws Exception {
        // Arrange
        Vacancy vacancy = new Vacancy();
        vacancy.setHhid("test_hhid");
        vacancy.setName("Test Vacancy");
        vacancy.setId(123L);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false); // Simulate vacancy not existing
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(resultSet.getLong(1)).thenReturn(123L);

        // Act
        Vacancy savedVacancy = vacancyRepo.saveOrUpdate(vacancy);

        // Assert
        verify(preparedStatement, times(1)).executeUpdate();
        assertNotNull(savedVacancy);
        assertEquals(123L, savedVacancy.getId());
    }

    @Test
    void testSaveOrUpdate_UpdateExistingVacancy() throws Exception {
        // Arrange
        Vacancy vacancy = new Vacancy();
        vacancy.setHhid("test_hhid");
        vacancy.setName("Test Vacancy");
        Vacancy existingVacancy = new Vacancy();
        existingVacancy.setId(123L);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true); // Simulate vacancy exists
        when(resultSet.getLong("id")).thenReturn(123L);
        vacancyRepo = spy(vacancyRepo); // To mock private methods if required in the future

        // Act
        Vacancy savedVacancy = vacancyRepo.saveOrUpdate(vacancy);

        // Assert
        verify(preparedStatement, times(1)).executeUpdate(); // Ensure update query is executed
        assertNotNull(savedVacancy);
        assertEquals(123L, savedVacancy.getId());
    }

    @Test
    void testFindByHhid_Hit() throws Exception {
        // Arrange
        String hhid = "test_hhid";
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong("id")).thenReturn(123L);
        when(resultSet.getString("hhid")).thenReturn(hhid);

        // Act
        Vacancy vacancy = vacancyRepo.findByHhid(hhid);

        // Assert
        assertNotNull(vacancy);
        assertEquals(hhid, vacancy.getHhid());
        assertEquals(123L, vacancy.getId());
    }

    @Test
    void testFindByHhid_Miss() throws Exception {
        // Arrange
        String hhid = "non_existing_hhid";
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false); // Simulate no record found

        // Act
        Vacancy vacancy = vacancyRepo.findByHhid(hhid);

        // Assert
        assertNotNull(vacancy);
        assertEquals(-1L, vacancy.getId());
    }

    @Test
    void testFindById_Hit() throws Exception {
        // Arrange
        long vacancyId = 123L;
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong("id")).thenReturn(vacancyId);
        when(resultSet.getString("hhid")).thenReturn("test_hhid");

        // Act
        Vacancy vacancy = vacancyRepo.findById(vacancyId);

        // Assert
        assertNotNull(vacancy);
        assertEquals(vacancyId, vacancy.getId());
        assertEquals("test_hhid", vacancy.getHhid());
    }

    @Test
    void testFindById_Miss() throws Exception {
        // Arrange
        long vacancyId = 999L;
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false); // Simulate no record found

        // Act
        Vacancy vacancy = vacancyRepo.findById(vacancyId);

        // Assert
        assertNotNull(vacancy);
        assertEquals(-1L, vacancy.getId());
    }

    @Test
    void testFillVacancyFromRS() throws Exception {
        // Arrange
        Vacancy vacancy = new Vacancy();
        when(resultSet.getLong("id")).thenReturn(123L);
        when(resultSet.getString("hhid")).thenReturn("test_hhid");
        when(resultSet.getString("name")).thenReturn("Test Vacancy");
        when(resultSet.getInt("archived")).thenReturn(1);
        when(resultSet.getString("alternate_url")).thenReturn("http://alternate.url");
        when(resultSet.getInt("gross")).thenReturn(1);
        when(resultSet.getString("url")).thenReturn("http://url");
        when(resultSet.getInt("salary_from")).thenReturn(1000);
        when(resultSet.getInt("salary_to")).thenReturn(2000);
        when(resultSet.getLong("employer_id")).thenReturn(456L);
        when(resultSet.getInt("region")).thenReturn(789);

        // Act
        vacancyRepo.fillVacancyFromRS(resultSet, vacancy);

        // Assert
        assertEquals(123L, vacancy.getId());
        assertEquals("test_hhid", vacancy.getHhid());
        assertEquals("Test Vacancy", vacancy.getName());
        assertEquals(1, vacancy.getArchived());
        assertEquals("http://alternate.url", vacancy.getAlternate_url());
        assertEquals(1, vacancy.getGross());
        assertEquals("http://url", vacancy.getUrl());
        assertEquals(1000, vacancy.getSalary_from());
        assertEquals(2000, vacancy.getSalary_to());
        assertEquals(456L, vacancy.getEmployer());
        assertEquals(789, vacancy.getRegion());
    }
}
