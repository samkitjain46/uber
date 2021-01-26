package com.uber.uberapi.Services;

import com.uber.uberapi.repositories.DBConstantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import java.util.HashMap;
import java.util.Map;

@Service
public class Constants {
    @Autowired
    final DBConstantRepository dbConstantRepository;
   private final Map<String,String> constants = new HashMap<String,String>();
    private static final Integer TEN_MNUTES=60*10*1000;
    public Constants(DBConstantRepository dbConstantRepository) {
        this.dbConstantRepository = dbConstantRepository;
        loadConstantsFromDB();
    }
    @Scheduled(fixedRate = TEN_MNUTES)
    private void loadConstantsFromDB()
    {
        dbConstantRepository.findAll().forEach(dbconstant ->{
            constants.put(dbconstant.getName(), dbconstant.getValue());
        });
    }
    public Integer getRideStartOTPExpiryMinutes()
    {
        return Integer.parseInt(constants.getOrDefault("rideStartOTPExpiryMinutes","3600000"));
    }


    public String getSchedulingTopicName() {
        return constants.getOrDefault("schedulingTopicName","schedulingServiceTopic");
    }

    public String getDriverMatchingTopicName() {
        return constants.getOrDefault("driverMatchingTopicName","driverMatchingTopic");
    }

    public int getMaxWaitTimeForPreviousRide() {
        return Integer.parseInt(constants.getOrDefault("maxWaitTimeForPreviousRide","900000"));
    }

    public Integer getBookingProcessBeforeTime() {
        return Integer.parseInt(constants.getOrDefault("bookingProcessBeforeTime","900000"));
    }

    public String getLocationTrackingTopicName() {
        return constants.getOrDefault("locationTrackingTopicName","locationTrackingTopic");
    }

    public double getMaxDistanceInKmsForDriverMatching() {
        return Double.parseDouble(constants.getOrDefault("maxDistanceInKmsForDriverMatching","2"));
    }

    public int getMAXDriverETAMinutes() {
        return  Integer.parseInt(constants.getOrDefault("maxDriverETAMinutes","15"));
    }

    public boolean getIsETABasedFilterEnabled() {
        return Boolean.parseBoolean(constants.getOrDefault("isETABasedFilterEnabled","true"));
    }
    public boolean getIsGenderBasedFilterEnabled() {
        return Boolean.parseBoolean(constants.getOrDefault("isGenderBasedFilterEnabled","true"));

    }

    public double getDefaultETASpeed() {
        return Double.parseDouble(constants.getOrDefault("defaultETASpeedKMPH","30.0"));

    }
}
