package com.uber.uberapi.controller;

import com.uber.uberapi.Exceptions.InavlidBookingException;
import com.uber.uberapi.Services.BookingService;
import com.uber.uberapi.Services.DriverMatchingService;
import com.uber.uberapi.models.*;
import com.uber.uberapi.repositories.BookingRepository;
import com.uber.uberapi.repositories.PassengerRepository;
import com.uber.uberapi.repositories.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PassengerController {
    @Autowired
    PassengerRepository passengerRepository;

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    DriverMatchingService driverMatchingService;

    @Autowired
    ReviewRepository reviewRepository;

    @Autowired
    BookingService bookingService;

    public Passenger getPassengerFromId(Long passengerId)
    {
        Optional<Passenger> passenger = passengerRepository.findById(passengerId);
        return passenger.get();
    }

    public Booking getPassengerBookingFromId(Long bookingId, Passenger passenger)
    {
        Optional<Booking> optionalBooking = bookingRepository.findById(bookingId);
        Booking booking = optionalBooking.get();
        if(!booking.getPassenger().equals(passenger))
        {
            throw new InavlidBookingException("Passenger " +passenger.getId()+ " has no such booking");
        }
        return booking;
    }

    @GetMapping("/{passengerId}")
    public Passenger getPassenger(@RequestParam(name = "passengerId") Long passengerId) {
        return getPassengerFromId(passengerId);

    }


    @GetMapping("{passengerId}/bookings")
    public List<Booking> getAllBookings(@RequestParam(name="passengerId") Long passengerId)
    {
        Passenger passenger = getPassengerFromId(passengerId);
        return (List<Booking>) passenger.getBookings();
    }

    @PostMapping("{passengerId}/bookings/{bookingId}")
    public Booking getBooking(@RequestParam(name="passengerId") Long passengerId,
                              @RequestParam(name="bookingId") Long bookingId)
    {
        Passenger passenger = getPassengerFromId(passengerId);
        return  getPassengerBookingFromId(bookingId,passenger);


    }
    @PostMapping("{passengerId}/bookings/")
    public void requestBooking(@RequestParam(name="passengerId") Long passengerId,
                              @RequestBody Booking data)
    {
        List<Location> route = new ArrayList<>();
        Passenger passenger = getPassengerFromId(passengerId);
        data.getRoute().forEach(location -> {
            route.add(Location.builder().latitude(location.getLatitude()).longitude(location.getLongitude()).build());
        });

        Booking booking = Booking.builder().rideStartOTP(OTP.make(passenger.getPhonenumber())).route(route).
                passenger(passenger).bookingType(data.getBookingType()).scheduledTime(data.getScheduledTime()).build();
        bookingService.createBooking(booking);


        //todo

    }

    @PatchMapping("{passengerId}/bookings/{bookingId")
    public void updateRoute(@RequestParam(name="passengerId") Long passengerId,
                            @RequestParam(name="bookingId") Long bookingId,
                            @RequestBody Booking data)
    {
        Passenger passenger=getPassengerFromId(passengerId);
        Booking booking = getPassengerBookingFromId(bookingId,passenger);
        List<Location> route = new ArrayList<>(booking.getCompleted_route());
        data.getRoute().forEach(location -> {
            route.add(Location.builder().latitude(location.getLatitude()).longitude(location.getLongitude()).build());
        });
        bookingService.updateRoute(booking,route);


    }

    @DeleteMapping("{passengerId}/bookings/{bookingId}")
    public void cancelBooking(@RequestParam(name="passengerId") Long passengerId,
                              @RequestParam(name="bookingId") Long bookingId)
    {
        Passenger passenger=getPassengerFromId(passengerId);
        Booking booking = getPassengerBookingFromId(bookingId,passenger);
        bookingService.cancelByPassenger(passenger,booking);
    }


    @PatchMapping("{passengerId}/bookings/{bookingId}/rate")
    public void rateRide(@RequestParam(name="passengerId") Long passengerId,
                        @RequestParam(name="bookingId") Long bookingId,
                        @RequestBody Review data)
    {
        Passenger passenger=getPassengerFromId(passengerId);
        Booking booking = getPassengerBookingFromId(bookingId,passenger);
        Review review=Review.builder().note(data.getNote()).ratingOutOfFive(data.getRatingOutOfFive()).build();
        booking.setReviewByPassenger(review);
        reviewRepository.save(review);
        bookingRepository.save(booking);

    }

    @PostMapping("{passengerId}/bookings/{bookingId}")
    public void retryBooking(@RequestParam(name="passengerId") Long passengerId,
                             @RequestParam(name="bookingId") Long bookingId
                              )
    {
        Passenger passenger = getPassengerFromId(passengerId);
        Booking booking= getPassengerBookingFromId(bookingId,passenger);
        bookingService.retryBooking(booking);


        //todo

    }
}
