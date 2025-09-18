package com.ens.hhparser5;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@SpringBootTest
class Hhparser5ApplicationTests {

	@Autowired
	private Connection connection;

	@Test
	void contextLoads() {
	}

	@Test
	void postgresTest() throws SQLException {
		PreparedStatement ps = connection.prepareStatement(
				"select * from project where id=?");


	}

}
