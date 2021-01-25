package com.uber.uberapi.Services;

import com.uber.uberapi.Exceptions.InvalidActionForBookingStateException;
import com.uber.uberapi.Services.messagequeue.MessageQueue;
import com.uber.uberapi.models.*;
import com.uber.uberapi.Services.notification.NotificationService;
import com.uber.uberapi.repositories.BookingRepository;
import com.uber.uberapi.repositories.DriverRepository;
import com.uber.uberapi.repositories.PassengerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class BookingService {
    @Autowired
    DriverMatchingService driverMatchingService;
    @Autowired
    OTPService otpservice;
    @Autowired
    BookingRepository bookingRepository;
    @Autowired
    PassengerRepository passengerRepository;

    @Autowired
    SchedulingService schedulingService;
    @Autowired
    MessageQueue messageQueue;
    @Autowired
    Constants constants;
    @Autowired
    NotificationService notificationService;

    @Autowired
    DriverRepository driverRepository;


    public void createBooking(Booking booking) {
        if(booking.getStartTime().after(new Date()))
        {
            booking.setBookingStatus(BookingStatus.SCHEDULED);
            messageQueue.sendMessage(constants.getSchedulingTopicName(), new SchedulingService.Message(booking));
            schedulingService.schedule(booking);
        }
        else
        {
            booking.setBookingStatus(BookingStatus.ASSIGNING_DRIVER);
            otpservice.sendRideStartOTP(booking.getRideStartOTP());
            messageQueue.sendMessage(constants.getDriverMatchingTopicName(), new DriverMatchingService.Message(booking));
        }
        passengerRepository.save(booking.getPassenger());
        bookingRepository.save(booking);


    }


    public void acceptBooking(Driver driver, Booking booking) {
        if(!booking.needsDriver())
        {
            return;
        }
        if(!driver.canAcceptBooking(constants.getMaxWaitTimeForPreviousRide()))
        {
            notificationService.notify(driver.getPhoneNumber(),"cannot accept boooking");
            return ;
        }
        booking.setDriver(driver);
        driver.setActiveBooking(booking);
        booking.getNotifiedDrivers().clear();
        driver.getAcceptableBookings().clear();;

        notificationService.notify(booking.getPassenger().getPhonenumber(),"driver is arriving");
        notificationService.notify(driver.getPhoneNumber(),"booking accepted");
        bookingRepository.save(booking);
        driverRepository.save(driver);

    }


    public void cancelByDriver(Booking booking, Driver driver) {
        booking.setDriver(null);
        driver.setActiveBooking(null);
        driver.getAcceptableBookings().remove(booking);
        notificationService.notify(booking.getPassenger().getPhonenumber(),"reassiging driver");
        notificationService.notify(driver.getPhoneNumber(),"Booking has been cancelled");
        retryBooking(booking);
        bookingRepository.save(booking);

    }


    public void cancelByPassenger(Passenger passenger, Booking booking) {
        try {
            booking.cancel();
        } catch (InvalidActionForBookingStateException inner) {
            notificationService.notify(booking.getPassenger().getPhonenumber(), "cannot cancel the booking now" +
                    "if the ride is in progress, ask your driver to end the ride");
            throw inner;
        }

    }


    public void updateRoute(Booking booking, List<Location> route) {
        if(!booking.canChangeRoute())
        {
            throw new InvalidActionForBookingStateException("Ride has already ben completed/cancelled") ;
        }
        booking.setRoute(route);
        bookingRepository.save(booking);
        notificationService.notify(booking.getDriver().getPhoneNumber(),"route has been updated!");

    }

    public void retryBooking(Booking booking) {
        createBooking(booking);
    }
}
