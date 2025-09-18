package com.ens.hhparser5.repository;

import com.ens.hhparser5.model.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.Optional;

@Component
public class RegionRepoImpl implements RegionRepo{

    private Logger logger = LoggerFactory.getLogger(RegionRepoImpl.class);

    @Autowired
    private Connection connection;
    @Override
    public Optional<Region> findById(int id) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("select * from regions where id=?");
        ps.setInt(1,id);
        ResultSet rs = ps.executeQuery();
        Region region = null;
        if (rs.next()){
            region = new Region();
            region.setId(rs.getInt("id"));
            region.setParentId(rs.getInt("parent_id"));
            region.setName(rs.getString("name"));
        }
        rs.close();
        ps.close();
        return Optional.ofNullable(region);
    }

    @Override
    public Region save(Region region) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("insert into regions (id,parent_id,name) values (?,?,?)");
        ps.setInt(1,region.getId());
        ps.setInt(2, region.getParentId());
        ps.setString(3, region.getName());
        ps.executeUpdate();
        ps.close();
        return region;
    }
}
