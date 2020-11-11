package com.walt;

import com.walt.dao.CustomerRepository;
import com.walt.dao.DeliveryRepository;
import com.walt.dao.DriverRepository;
import com.walt.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class WaltServiceImpl implements WaltService {
    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    DriverRepository driverRepository;

    @Autowired
    DeliveryRepository deliveryRepository;

    @Override
    public Delivery createOrderAndAssignDriver(Customer customer, Restaurant restaurant, Date deliveryTime) {
        //TODO check restaurant and costumer city are the same
        List<Driver> availableDrivers = driverRepository.findAllDriversByCity(customer.getCity()).stream()
                .filter(driver -> containsOverlappingDeliveries(driver, new Timestamp(deliveryTime.getTime())))
                .collect(Collectors.toList());

        Driver minKmDriver = getMinKmDriver(availableDrivers);
        if (minKmDriver == null) {
            return null; //TODO handle errors
        }

        Delivery delivery = new Delivery(minKmDriver, restaurant, customer, deliveryTime);
        deliveryRepository.save(delivery);
        return delivery;
    }

    @Override
    public List<DriverDistance> getDriverRankReport() {
        return deliveryRepository.findDistancesByDriver();
    }

    @Override
    public List<DriverDistance> getDriverRankReportByCity(City city) {
        return deliveryRepository.findCityDistancesByDriver(city);
    }

    private boolean containsOverlappingDeliveries(Driver driver, Timestamp deliveryTime) {
        List<Delivery> deliveries = deliveryRepository.findAllDeliveriesByDriver(driver);
        for (Delivery delivery : deliveries) {
            long millis = Math.abs(delivery.getDeliveryTime().getTime() - deliveryTime.getTime());

            //1000ms * 60s * 2m
            if (millis <= (1000 * 60 * 60)) {
                return false;
            }
        }
        return true;
    }

    private Driver getMinKmDriver(List<Driver> drivers) {
        return drivers.stream()
                .min(Comparator.comparingDouble(this::getDriverDeliveryKm))
                .orElse(null);
    }

    double getDriverDeliveryKm(Driver driver) {
        return deliveryRepository.findAllDeliveriesByDriver(driver).stream()
                .mapToDouble(Delivery::getDistance).sum();

    }
}
