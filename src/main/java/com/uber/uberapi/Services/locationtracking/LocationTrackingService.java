package com.uber.uberapi.Services.locationtracking;

import com.uber.uberapi.Services.Constants;
import com.uber.uberapi.Services.messagequeue.MQMessage;
import com.uber.uberapi.Services.messagequeue.MessageQueue;
import com.uber.uberapi.Services.utils.quadtree.QuadTree;
import com.uber.uberapi.models.Driver;
import com.uber.uberapi.models.Location;
import com.uber.uberapi.repositories.DriverRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LocationTrackingService {
    @Autowired
    MessageQueue messageQueue;
    @Autowired
    Constants constants;
    @Autowired
    DriverRepository driverRepository;
    QuadTree world = new QuadTree();
    public List<Driver> getDriversNearLocation(Location pickup) {
        return world.findNeighboursIds(pickup.getLatitude(),pickup.getLongitude(),constants.getMaxDistanceInKmsForDriverMatching()).stream().
                map(driverId->driverRepository.findById(driverId).orElseThrow())
                .collect(Collectors.toList());


    }

    public void updateDriverLocation(Driver driver, Location location) {
        world.removeNeighbour(driver.getId());
        world.addNeighbour(driver.getId(),location.getLatitude(),location.getLongitude());

        driver.setLastKnownLocation(location);
        driverRepository.save(driver);
    }
    @Scheduled(fixedRate = 1000)
    public void consumer(){
        MQMessage m = messageQueue.consumeMessage(constants.getDriverMatchingTopicName());
        if(m==null)
        {
            return;
        }
        Message message = (Message)m;
        updateDriverLocation(message.getDriver(),message.getLocation());

    }
    @Getter
    @Setter
    @AllArgsConstructor
    public static class Message implements MQMessage{
        private Driver driver;
        private Location location;
    }
}
