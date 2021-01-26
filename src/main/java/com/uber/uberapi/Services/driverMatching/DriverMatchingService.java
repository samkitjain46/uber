package com.uber.uberapi.Services.driverMatching;

import com.uber.uberapi.Services.Constants;
import com.uber.uberapi.Services.ETAService;
import com.uber.uberapi.Services.driverMatching.filters.DriverFilter;
import com.uber.uberapi.Services.driverMatching.filters.ETABasedFilter;
import com.uber.uberapi.Services.driverMatching.filters.GenderFilter;
import com.uber.uberapi.Services.locationtracking.LocationTrackingService;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DriverMatchingService  {
    final MessageQueue messageQueue;
    final Constants constants;
    final NotificationService notificationService;
    final BookingRepository bookingRepository;
    final DriverRepository driverRepository;
    final LocationTrackingService locationTrackingService;
    final ETAService etaService;

    final List<DriverFilter> driverFilters = new ArrayList<>();


    public DriverMatchingService(LocationTrackingService locationTrackingService, MessageQueue messageQueue, Constants constants, NotificationService notificationService, BookingRepository bookingRepository, DriverRepository driverRepository, ETAService etaService) {
        this.locationTrackingService = locationTrackingService;
        this.messageQueue = messageQueue;
        this.constants = constants;
        this.notificationService = notificationService;
        this.bookingRepository = bookingRepository;
        this.driverRepository = driverRepository;
        this.etaService = etaService;
        driverFilters.add(new ETABasedFilter(this.etaService,constants));
        driverFilters.add(new GenderFilter(constants));


    }

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
        drivers=filterDrivers(drivers,booking);
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

    private List<Driver> filterDrivers(List<Driver> drivers, Booking booking) {
        for(DriverFilter filter:driverFilters)
        {
           drivers= filter.apply(drivers,booking);
        }
        return drivers;

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
