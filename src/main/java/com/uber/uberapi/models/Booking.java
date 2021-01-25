package com.uber.uberapi.models;

import com.uber.uberapi.Exceptions.InvalidActionForBookingStateException;
import com.uber.uberapi.Exceptions.InvalidOTPException;
import com.uber.uberapi.repositories.DriverRepository;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.*;
import java.util.*;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "booking", indexes= {@Index(columnList = "passenger_id"),@Index(columnList = "driver_id")})

public class Booking extends  Auditable{
    @ManyToOne
    private Passenger passenger;

    @ManyToOne
    private Driver driver;

    @ManyToMany(cascade =CascadeType.PERSIST)
    private Set<Driver> notifiedDrivers = new HashSet<>();
    @Enumerated(value= EnumType.STRING)
    private BookingType bookingType;

    @Enumerated(value=EnumType.STRING)
    private BookingStatus bookingStatus;

    @OneToOne
    private Review reviewByPassenger;

    @OneToOne
    private Review reviewByDriver;

    @OneToOne
    private PaymentReceipt paymentReceipt;

    @Autowired
    DriverRepository driverRepository;



    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name="booking_route",
            joinColumns = @JoinColumn(name="booking_id"),
            inverseJoinColumns = @JoinColumn(name="exact_location_id"),
            indexes={@Index(columnList = "booking_id")}
    )
    @OrderColumn(name="location_index")
    private List<Location> route = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name="booking_completed_route",
            joinColumns = @JoinColumn(name="booking_id"),
            inverseJoinColumns = @JoinColumn(name="exact_location_id"),
            indexes={@Index(columnList = "booking_id")}
    )
    @OrderColumn(name="location_index")
    private List<Location> completed_route = new ArrayList<>();

    @Temporal(value=TemporalType.TIMESTAMP)
    private Date startTime;
    @Temporal(value=TemporalType.TIMESTAMP)
    private Date endTIme;

    @Temporal(value=TemporalType.TIMESTAMP)
    private  Date expectedCompletionTime;

    @Column(nullable = true)
    @Temporal(value=TemporalType.TIMESTAMP)
    private Date scheduledTime;

    private long totalDistanceMeters;
    @OneToOne
    private OTP rideStartOTP;


    public void startRide(OTP otp, int rideStartOTPExpiryMinutes) {
        if(!bookingStatus.equals(BookingStatus.CAB_ARRIVED))
        {
            throw new InvalidActionForBookingStateException("cannot start the ride before the driver has reached the pickup point");
        }
        if(!rideStartOTP.validateEnteredOTP(otp, rideStartOTPExpiryMinutes))
        {
            throw new InvalidOTPException();
        }
        bookingStatus = BookingStatus.IN_RIDE;
    }

    public void endRide() {
        if(!bookingStatus.equals(BookingStatus.IN_RIDE))
        {
            throw new InvalidActionForBookingStateException("The ride has not started yet");
        }
        driver.setActiveBooking(null);
        driverRepository.save(driver);
        bookingStatus=BookingStatus.COMPLETED;
    }

    public boolean canChangeRoute() {
        return bookingStatus.equals(BookingStatus.ASSIGNING_DRIVER) || bookingStatus.equals(BookingStatus.CAB_ARRIVED) ||
                bookingStatus.equals(BookingStatus.IN_RIDE) || bookingStatus.equals(BookingStatus.SCHEDULED) || bookingStatus.equals(BookingStatus.REACHING_PICKUP_LOCATION)
                ||bookingStatus.equals(BookingStatus.ASSIGNING_DRIVER);
    }

    public boolean needsDriver() {
        return bookingStatus.equals(BookingStatus.ASSIGNING_DRIVER);
    }

    public Location getPickupLocation() {
        return route.get(0);
    }



    public void cancel() {
        if(!(bookingStatus.equals(BookingStatus.REACHING_PICKUP_LOCATION)||
        bookingStatus.equals(BookingStatus.ASSIGNING_DRIVER)||
        bookingStatus.equals(BookingStatus.SCHEDULED)||
        bookingStatus.equals(BookingStatus.CAB_ARRIVED))){
           throw  new InvalidActionForBookingStateException("Cannot cancel the booking now. If the ride is in progress, ask driver to end ride");

        }
        bookingStatus=BookingStatus.CANCELLED;
        driver=null;
        notifiedDrivers.clear();

    }
}
