package com.uber.uberapi.Services;

import com.uber.uberapi.Services.messagequeue.MQMessage;
import com.uber.uberapi.Services.messagequeue.MessageQueue;
import com.uber.uberapi.Services.notification.NotificationService;
import com.uber.uberapi.models.Booking;
import com.uber.uberapi.models.Driver;
import com.uber.uberapi.models.Location;
import com.uber.uberapi.models.Passenger;
import com.uber.uberapi.repositories.BookingRepository;
import com.uber.uberapi.repositories.DriverRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.awt.print.Book;

@Service
public class DriverMatchingService  {
    @Autowired
    LocationTrackingService locationTrackingService;
    @Autowired
    MessageQueue messageQueue;
    @Autowired
    Constants constants;
    @Autowired
    NotificationService notificationService;
    @Autowired
    BookingRepository bookingRepository;
    @Autowired
    DriverRepository driverRepository;
    @Scheduled(fixedRate = 1000)
    public void consumer(){
        MQMessage m = messageQueue.consumeMessage(constants.getDriverMatchingTopicName());
        if(m==null)
        {
            return;
        }
        Message message = (Message)m;
        findNearbyDrivers(message.getBooking());
    }

    private void findNearbyDrivers(Booking booking) {
        //get start location of booking
        Location pickup = booking.getPickupLocation();
        List<Driver> drivers= locationTrackingService.getDriversNearLocation(pickup);
        if(drivers.size()==0)
        {
            notificationService.notify(booking.getPassenger().getPhonenumber(),"no cabs near you");
            return;
        }
        notificationService.notify(booking.getPassenger().getPhonenumber(),"contacting cabs around you");
        //filter the drivers somehow
        if(drivers.size()==0)
        {
            notificationService.notify(booking.getPassenger().getPhonenumber(),"no cabs near you");
        }
        drivers.forEach(driver -> {
            notificationService.notify(driver.getPhoneNumber(),"Booking near you :" + booking.toString());
            driver.getAcceptableBookings().add(booking);
        });
        bookingRepository.save(booking);


    }

    public void acceptBooking(Driver driver, Booking booking) {

    }


    public void cancelByDriver(Driver driver, Booking booking) {

    }


    public void cancelByPassenger(Passenger passenger, Booking booking) {

    }


    public void assignDriver(Booking booking) {

    }

    public static void main(String[] args) {

    }
    @Getter
    @Setter
    @AllArgsConstructor
    public static  class Message implements MQMessage{
        private Booking booking;

        @Override
        public String toString() {
            return String.format("Need to find drivers for %s", booking.toString());
        }
    }
}
