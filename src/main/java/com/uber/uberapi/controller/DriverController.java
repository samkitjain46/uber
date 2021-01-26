package com.uber.uberapi.controller;

import com.uber.uberapi.Exceptions.InavlidBookingException;

import com.uber.uberapi.Services.BookingService;
import com.uber.uberapi.Services.Constants;
import com.uber.uberapi.Services.driverMatching.DriverMatchingService;
import com.uber.uberapi.models.*;
import com.uber.uberapi.repositories.BookingRepository;
import com.uber.uberapi.repositories.DriverRepository;
import com.uber.uberapi.repositories.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RequestMapping("/driver")
@RestController

public class DriverController {
    @Autowired
    DriverRepository driverRepository;

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    DriverMatchingService driverMatchingService;

    @Autowired
    ReviewRepository reviewRepository;

    @Autowired
    BookingService bookingService;

    @Autowired
    Constants constants;



    public Driver getDriverFromId(Long driverId)
    {
        Optional<Driver> driver = driverRepository.findById(driverId);

        return driver.get();
    }

    public Booking getDriverBookingFromId(Long bookingId, Driver driver)
    {

        Booking booking = getBookingFromId(bookingId);
        if(!booking.getDriver().equals(driver))
        {
            throw new InavlidBookingException("Driver " +driver.getId()+ " has no such booking");
        }
        return booking;
    }

    @GetMapping("/{driverId}")
    public Driver getDriver(@PathVariable(name = "driverId") Long driverId) {
        return getDriverFromId(driverId);

    }
    @PutMapping("/{driverId}")
    public void changeAvailability(@PathVariable(name="driverId") Long driverId, @RequestBody Boolean available)
    {
        Driver driver = getDriverFromId(driverId);
        driver.setIsAvailable(available);
        driverRepository.save(driver);
    }

    @GetMapping("{driverId}/bookings")
    public List<Booking> getAllBookings(@PathVariable(name="driverId") Long driverId)
    {
        Driver driver = getDriverFromId(driverId);
        return (List<Booking>) driver.getBookings();
    }

    @PostMapping("{driverId}/bookings/{bookingId}")
    public Booking getBooking(@PathVariable(name="driverId") Long driverId,
                              @PathVariable(name="bookingId") Long bookingId)
    {
        Driver driver = getDriverFromId(driverId);
        return  getDriverBookingFromId(bookingId,driver);


    }
    @PostMapping("{driverId}/bookings/{bookingId}")
    public void acceptBooking(@PathVariable(name="driverId") Long driverId,
                              @PathVariable(name="bookingId") Long bookingId)
    {
        Driver driver = getDriverFromId(driverId);
        Booking booking = getBookingFromId(bookingId);
        bookingService.acceptBooking(driver,booking);

    }

    private Booking getBookingFromId(Long bookingId) {
        Optional<Booking> booking = bookingRepository.findById(bookingId);

        return booking.get();
    }

    @DeleteMapping("{driverId}/bookings/{bookingId}")
    public void cancelBooking(@PathVariable(name="driverId") Long driverId,
                              @PathVariable(name="bookingId") Long bookingId)
    {
        Driver driver=getDriverFromId(driverId);
        Booking booking = getDriverBookingFromId(bookingId,driver);
        bookingService.cancelByDriver(booking,driver);
        driverMatchingService.cancelByDriver(driver,booking);
    }

    @PatchMapping("{driverId}/bookings/{bookingId}/start")
    public void startRide(@PathVariable(name="driverId") Long driverId,
                              @PathVariable(name="bookingId") Long bookingId,
                          @RequestBody OTP otp)
    {
        Driver driver=getDriverFromId(driverId);
        Booking booking = getDriverBookingFromId(bookingId,driver);
        booking.startRide(otp, constants.getRideStartOTPExpiryMinutes());
        bookingRepository.save(booking);

    }

    @PatchMapping("{driverId}/bookings/{bookingId}/end")
    public void endRide(@PathVariable(name="driverId") Long driverId,
                          @PathVariable(name="bookingId") Long bookingId)
    {
        Driver driver=getDriverFromId(driverId);
        Booking booking = getDriverBookingFromId(bookingId,driver);
        booking.endRide();
        bookingRepository.save(booking);

    }

    @PatchMapping("{driverId}/bookings/{bookingId}/rate")
    public void rateRide(@PathVariable(name="driverId") Long driverId,
                        @PathVariable(name="bookingId") Long bookingId,
                        @RequestBody Review data)
    {
        Driver driver=getDriverFromId(driverId);
        Booking booking = getDriverBookingFromId(bookingId,driver);
        Review review=Review.builder().note(data.getNote()).ratingOutOfFive(data.getRatingOutOfFive()).build();
        booking.setReviewByDriver(review);
        reviewRepository.save(review);
        bookingRepository.save(booking);

    }


}














