package com.ens.hhparser5.repository;

import com.ens.hhparser5.model.Region;

import java.sql.SQLException;
import java.util.Optional;

public interface RegionRepo {

    Optional<Region> findById(int id) throws SQLException;
    Region save(Region region) throws SQLException;

}
