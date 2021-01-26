package com.uber.uberapi.Services;

import com.uber.uberapi.models.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ETAService {
    @Autowired
    Constants constants;
    public int getETAMinutes(Location lastKnownLocation, Location pickup) {
        return (int) (60.0 * lastKnownLocation.distaceKm(pickup)/constants.getDefaultETASpeed());

    }
}
