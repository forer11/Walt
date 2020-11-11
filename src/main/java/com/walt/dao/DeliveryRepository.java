package com.walt.dao;

import com.walt.model.City;
import com.walt.model.Driver;
import com.walt.model.Delivery;
import com.walt.model.DriverDistance;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface DeliveryRepository extends CrudRepository<Delivery, Long> {
    List<Delivery> findAllDeliveriesByDriver(@Param("driver") Driver driver);

    @Query("SELECT delivery.driver as driver, sum(delivery.distance) as totalDistance " +
            "FROM Delivery delivery " +
            "GROUP BY delivery.driver " +
            "ORDER BY totalDistance DESC")
    List<DriverDistance> findDistancesByDriver();

    @Query("SELECT delivery.driver as driver, sum(delivery.distance) as totalDistance " +
            "FROM Delivery delivery WHERE delivery.driver.city = :driverCity " +
            "GROUP BY delivery.driver " +
            "ORDER BY totalDistance DESC")
    List<DriverDistance> findCityDistancesByDriver(@Param("driverCity") City city);
}


