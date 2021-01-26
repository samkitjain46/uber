package com.uber.uberapi.Services.driverMatching.filters;

import com.uber.uberapi.Services.Constants;
import com.uber.uberapi.models.Booking;
import com.uber.uberapi.models.Driver;
import com.uber.uberapi.models.Gender;

import java.util.List;
import java.util.stream.Collectors;

public class GenderFilter extends DriverFilter {
    public GenderFilter(Constants constants) {
        super(constants);
    }

    @Override
    public List<Driver> apply(List<Driver> drivers, Booking booking) {
        //male driver can only drive male passengers
        if(!getConstants().getIsGenderBasedFilterEnabled()) return drivers;
        Gender passengerGender = booking.getPassenger().getGender();
        return  drivers.stream().filter(driver->{
            Gender driverGender= driver.getGender();
            //x implies y => ~x | y
            return !driverGender.equals(Gender.MALE) || passengerGender.equals(Gender.MALE);

        }).collect(Collectors.toList());

    }
}
