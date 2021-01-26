package com.uber.uberapi.controller;

import com.uber.uberapi.Services.Constants;
import com.uber.uberapi.Services.locationtracking.LocationTrackingService;
import com.uber.uberapi.Services.messagequeue.MessageQueue;
import com.uber.uberapi.models.Driver;
import com.uber.uberapi.models.Location;
import com.uber.uberapi.models.Passenger;
import com.uber.uberapi.repositories.DriverRepository;
import com.uber.uberapi.repositories.PassengerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
@RequestMapping("/location")
public class LocationTrackingController {
    @Autowired
    DriverRepository driverRepository;

    @Autowired
    PassengerRepository passengerRepository;

    @Autowired
    LocationTrackingService locationTrackingService;
    @Autowired
    MessageQueue messageQueue;
    @Autowired
    Constants constants;
    public Driver getDriverFromId(Long driverId)
    {
        Optional<Driver> driver = driverRepository.findById(driverId);

        return driver.get();
    }


    @PutMapping("driver/{driverId}")
    public void updateDriverLocation(@PathVariable Long driverId,
                                     @RequestBody Location data){

        Driver driver=getDriverFromId(driverId);
        Location location=Location.builder().latitude(data.getLatitude()).longitude(data.getLongitude()).build();
        messageQueue.sendMessage(constants.getLocationTrackingTopicName(),new LocationTrackingService.Message(driver,location));
        locationTrackingService.updateDriverLocation(driver,location);

    }
    public Passenger getPassengerFromId(Long passengerId)
    {
        Optional<Passenger> passenger = passengerRepository.findById(passengerId);
        return passenger.get();
    }
    @PutMapping("passenger/{passengerId}")
    public void updatePassengerLocation(@PathVariable Long passsengerId,
                                        @RequestBody Location location){
        Passenger passenger= getPassengerFromId(passsengerId);
        passenger.setLastknownLocation(Location.builder().latitude(location.getLatitude()).longitude(location.getLongitude()).build());
        passengerRepository.save(passenger);
    }
}
