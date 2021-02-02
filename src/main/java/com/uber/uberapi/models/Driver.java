package com.uber.uberapi.models;

import com.uber.uberapi.Exceptions.UnapprovedDriverException;
import com.uber.uberapi.Services.utils.DateUtils;
import lombok.*;

import javax.persistence.*;
import java.util.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="driver")
public class Driver extends Auditable {
    @OneToOne
    private Review avgRating;
    @OneToOne(cascade = CascadeType.ALL)
    private Account account;
    private Gender gender;

    @OneToOne(mappedBy = "driver", cascade = CascadeType.ALL)
    private Car car;

    private String licenseDetails;
    @Temporal(value = TemporalType.DATE)
    private Date dob;

    @Enumerated(value = EnumType.STRING)
    private DriverApprovalStatus approvalStatus;
    @OneToMany(mappedBy = "driver")
    private List<Booking> bookings;
    private String phoneNumber;

    @ManyToMany(mappedBy = "notifiedDrivers", cascade = CascadeType.PERSIST)
    private Set<Booking> acceptableBookings = new HashSet<>();

    private Boolean isAvailable;
    private String activeCity;
    @OneToOne
    private Location lastKnownLocation;
    @OneToOne
    private Location home;
    private String name;

    @OneToOne
    private Booking activeBooking = null;

    public void setAvailable(Boolean available) {
        if (available && !getApprovalStatus().equals(DriverApprovalStatus.APPROVED)) {
            throw new UnapprovedDriverException("Driver Approval Pending or Denied" + getId());
        }
        isAvailable = available;
    }

    public boolean canAcceptBooking(int maxWaitTimeForPreviousRide) {
        if (isAvailable && activeBooking == null) {
            return true;
        }
        //check if current ride can be completed in 10  mins
        return activeBooking.getExpectedCompletionTime().before(DateUtils.addMinutes(new Date(), maxWaitTimeForPreviousRide));
    }
}


