package com.walt;

import com.walt.dao.CustomerRepository;
import com.walt.dao.DeliveryRepository;
import com.walt.dao.DriverRepository;
import com.walt.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;
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
        if (customer == null || restaurant == null || deliveryTime == null) {
            throw new RuntimeException("Error: One or more of the arguments are null");
        }
        if (!customer.getCity().getId().equals(restaurant.getCity().getId())) {
            throw new RuntimeException("Error: Restaurant and Costumer Should be located at the same city");
        }
        List<Driver> availableDrivers = driverRepository.findAllDriversByCity(customer.getCity()).stream()
                .filter(driver -> containsOverlappingDeliveries(driver, new Timestamp(deliveryTime.getTime())))
                .collect(Collectors.toList());

        Driver minKmDriver = getMinKmDriver(availableDrivers);
        if (minKmDriver == null) {
            System.err.println("No Driver is currently available"); // if we assume the costumer city is the city he order from
            return null; //TODO maybe exception mechanism
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
        Collections.shuffle(drivers); // We want to keep fairness if we have multiple drivers with same min
        return drivers.stream()
                .min(Comparator.comparingDouble(this::getDriverDeliveryKm))
                .orElse(null);
    }

    double getDriverDeliveryKm(Driver driver) {
        return deliveryRepository.findAllDeliveriesByDriver(driver).stream()
                .mapToDouble(Delivery::getDistance).sum();

    }
}
