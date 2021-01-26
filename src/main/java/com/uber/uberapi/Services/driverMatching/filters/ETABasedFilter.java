package com.uber.uberapi.Services.driverMatching.filters;

import com.uber.uberapi.Services.Constants;
import com.uber.uberapi.Services.ETAService;
import com.uber.uberapi.models.Booking;
import com.uber.uberapi.models.Driver;
import com.uber.uberapi.models.Location;

import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ETABasedFilter extends DriverFilter {
    private final ETAService etaService;


    public ETABasedFilter(ETAService etaService, Constants constants) {
        super(constants);
        this.etaService = etaService;

    }

    @Override
    public List<Driver> apply(List<Driver> drivers, Booking booking) {
        if(!getConstants().getIsETABasedFilterEnabled()) return drivers;
        Location pickup = booking.getPickupLocation();
        return drivers.stream().filter(driver->{
            return etaService.getETAMinutes(driver.getLastKnownLocation(),pickup)<= getConstants().getMAXDriverETAMinutes();
        }).collect(Collectors.toList());
    }
}
