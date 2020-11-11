package com.walt;

import com.walt.dao.*;
import com.walt.model.*;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@SpringBootTest()
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class WaltTest {

    @TestConfiguration
    static class WaltServiceImplTestContextConfiguration {

        @Bean
        public WaltService waltService() {
            return new WaltServiceImpl();
        }
    }

    @Autowired
    WaltService waltService;

    @Resource
    CityRepository cityRepository;

    @Resource
    CustomerRepository customerRepository;

    @Resource
    DriverRepository driverRepository;

    @Resource
    DeliveryRepository deliveryRepository;

    @Resource
    RestaurantRepository restaurantRepository;

    @BeforeEach()
    public void prepareData() {

        City jerusalem = new City("Jerusalem");
        City tlv = new City("Tel-Aviv");
        City bash = new City("Beer-Sheva");
        City haifa = new City("Haifa");
        City kfarSava = new City("KfarSava");


        cityRepository.save(jerusalem);
        cityRepository.save(tlv);
        cityRepository.save(bash);
        cityRepository.save(haifa);
        cityRepository.save(kfarSava);


        createDrivers(jerusalem, tlv, bash, haifa, kfarSava);

        createCustomers(jerusalem, tlv, haifa, kfarSava);

        createRestaurant(jerusalem, tlv, kfarSava);
    }

    private void createRestaurant(City jerusalem, City tlv, City kfar) {
        Restaurant meat = new Restaurant("meat", jerusalem, "All meat restaurant");
        Restaurant vegan = new Restaurant("vegan", tlv, "Only vegan");
        Restaurant cafe = new Restaurant("cafe", tlv, "Coffee shop");
        Restaurant chinese = new Restaurant("chinese", tlv, "chinese restaurant");
        Restaurant mexican = new Restaurant("restaurant", tlv, "mexican restaurant ");
        Restaurant dogCafe = new Restaurant("doggies", kfar, "doggie restaurant ");


        restaurantRepository.saveAll(Lists.newArrayList(meat, vegan, cafe, chinese, mexican, dogCafe));
    }

    private void createCustomers(City jerusalem, City tlv, City haifa, City kfar) {
        Customer beethoven = new Customer("Beethoven", tlv, "Ludwig van Beethoven");
        Customer mozart = new Customer("Mozart", jerusalem, "Wolfgang Amadeus Mozart");
        Customer chopin = new Customer("Chopin", haifa, "Frédéric François Chopin");
        Customer rachmaninoff = new Customer("Rachmaninoff", tlv, "Sergei Rachmaninoff");
        Customer bach = new Customer("Bach", tlv, "Sebastian Bach. Johann");
        Customer lior = new Customer("Lior", kfar, "Zeev Haklai");
        Customer chong = new Customer("Richongo", kfar, "Chong city");


        customerRepository.saveAll(Lists.newArrayList(beethoven, mozart, chopin, rachmaninoff, bach, lior, chong));
    }

    private void createDrivers(City jerusalem, City tlv, City bash, City haifa, City kfar) {
        Driver mary = new Driver("Mary", tlv);
        Driver patricia = new Driver("Patricia", tlv);
        Driver jennifer = new Driver("Jennifer", haifa);
        Driver james = new Driver("James", bash);
        Driver john = new Driver("John", bash);
        Driver robert = new Driver("Robert", jerusalem);
        Driver david = new Driver("David", jerusalem);
        Driver daniel = new Driver("Daniel", tlv);
        Driver noa = new Driver("Noa", haifa);
        Driver ofri = new Driver("Ofri", haifa);
        Driver nata = new Driver("Neta", jerusalem);
        Driver malol = new Driver("Malol", kfar);


        driverRepository.saveAll(Lists.newArrayList(mary, patricia, jennifer, james, john,
                robert, david, daniel, noa, ofri, nata, malol));
    }

    @Test
    public void testBasics() {

        assertEquals(((List<City>) cityRepository.findAll()).size(), 4);
        assertEquals((driverRepository.findAllDriversByCity(cityRepository.findByName("Beer-Sheva")).size()), 2);
    }

    @Test
    public void create2Deliveries() {
        Customer customer = customerRepository.findByName("Beethoven");
        Date date = new Date();
        Restaurant restaurant = restaurantRepository.findByName("vegan");
        Delivery delivery = waltService.createOrderAndAssignDriver(customer, restaurant, date);

        Customer customer2 = customerRepository.findByName("Beethoven");
        Date date2 = new Date();
        Restaurant restaurant2 = restaurantRepository.findByName("vegan");
        Delivery delivery2 = waltService.createOrderAndAssignDriver(customer2, restaurant2, date2);

        assertNotEquals(delivery.getDriver().getId(), delivery2.getDriver().getId());
    }

    @Test
    public void create2DeliveriesFor1Driver() {
        Customer customer = customerRepository.findByName("Lior");
        Date date = new Date();
        Restaurant restaurant = restaurantRepository.findByName("doggies");
        Delivery delivery = waltService.createOrderAndAssignDriver(customer, restaurant, date);

        Customer customer2 = customerRepository.findByName("Richongo");
        Date date2 = new Date();
        Restaurant restaurant2 = restaurantRepository.findByName("doggies");
        Delivery delivery2 = waltService.createOrderAndAssignDriver(customer2, restaurant2, date2);
        assertNull(delivery2);
    }

    @Test
    public void create2DeliveriesFor1DriverWithHourTimeDifference() {
        Customer customer = customerRepository.findByName("Lior");
        Date date = new Date();
        Restaurant restaurant = restaurantRepository.findByName("doggies");
        Delivery delivery = waltService.createOrderAndAssignDriver(customer, restaurant, date);

        Customer customer2 = customerRepository.findByName("Richongo");
        Date date2 = new Date();
        date2.setTime(date2.getTime() + TimeUnit.HOURS.toMillis(2));
        Restaurant restaurant2 = restaurantRepository.findByName("doggies");
        Delivery delivery2 = waltService.createOrderAndAssignDriver(customer2, restaurant2, date2);
        assertEquals(delivery.getDriver().getId(), delivery2.getDriver().getId());
    }

    @Test
    public void create2DeliveriesFor1DriverWith45MINTimeDifference() {
        Customer customer = customerRepository.findByName("Lior");
        Date date = new Date();
        Restaurant restaurant = restaurantRepository.findByName("doggies");
        Delivery delivery = waltService.createOrderAndAssignDriver(customer, restaurant, date);

        Customer customer2 = customerRepository.findByName("Richongo");
        Date date2 = new Date();
        date2.setTime(date2.getTime() + TimeUnit.MINUTES.toMillis(45));
        Restaurant restaurant2 = restaurantRepository.findByName("doggies");
        Delivery delivery2 = waltService.createOrderAndAssignDriver(customer2, restaurant2, date2);
        assertNull(delivery2);
    }

    @Test
    public void driverRankingTest1() {
        Customer customer = customerRepository.findByName("Beethoven");
        Date date = new Date();
        Restaurant restaurant = restaurantRepository.findByName("vegan");
        Delivery delivery = waltService.createOrderAndAssignDriver(customer, restaurant, date);

        Customer customer2 = customerRepository.findByName("Beethoven");
        Date date2 = new Date();
        Restaurant restaurant2 = restaurantRepository.findByName("vegan");
        Delivery delivery2 = waltService.createOrderAndAssignDriver(customer2, restaurant2, date2);

        assertNotEquals(delivery.getDriver().getId(), delivery2.getDriver().getId());

        Customer customer3 = customerRepository.findByName("Lior");
        Date date3 = new Date();
        Restaurant restaurant3 = restaurantRepository.findByName("doggies");
        Delivery delivery3 = waltService.createOrderAndAssignDriver(customer3, restaurant3, date3);

        Customer customer4 = customerRepository.findByName("Richongo");
        Date date4 = new Date();
        date4.setTime(date4.getTime() + TimeUnit.HOURS.toMillis(2));
        Restaurant restaurant4 = restaurantRepository.findByName("doggies");
        Delivery delivery4 = waltService.createOrderAndAssignDriver(customer4, restaurant4, date4);
        assertEquals(delivery3.getDriver().getId(), delivery4.getDriver().getId());

        waltService.getDriverRankReport().forEach(driverDistance -> {
            System.out.println(driverDistance.getDriver().getName() + ":" + driverDistance.getTotalDistance());
        });
        System.out.println("\n----------------------------------------------------");
        waltService.getDriverRankReportByCity(customer2.getCity()).forEach(driverDistance -> {
            System.out.println(driverDistance.getDriver().getName() + ":" + driverDistance.getTotalDistance());
        });
    }
}
