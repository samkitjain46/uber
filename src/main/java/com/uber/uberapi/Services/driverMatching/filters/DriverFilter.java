package com.uber.uberapi.Services.driverMatching.filters;

import com.uber.uberapi.Services.Constants;
import com.uber.uberapi.models.Booking;
import com.uber.uberapi.models.Driver;
import lombok.Getter;

import java.util.List;

public abstract class DriverFilter {
    @Getter
    private final Constants constants;

    public DriverFilter(Constants constants) {
        this.constants = constants;
    }


    public List<Driver> apply(List<Driver> drivers, Booking booking){

    }
}
