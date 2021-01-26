package com.uber.uberapi.Services;

import com.uber.uberapi.Services.locationtracking.LocationTrackingService;
import com.uber.uberapi.Services.messagequeue.MQMessage;
import com.uber.uberapi.Services.messagequeue.MessageQueue;
import com.uber.uberapi.Services.notification.NotificationService;
import com.uber.uberapi.models.Booking;
import com.uber.uberapi.Services.utils.DateUtils;
import com.uber.uberapi.repositories.BookingRepository;
import com.uber.uberapi.repositories.DriverRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Service
public class SchedulingService  {


    @Autowired
    LocationTrackingService locationTrackingService;
    @Autowired
    MessageQueue messageQueue;
    @Autowired
    Constants constants;
    @Autowired
    NotificationService notificationService;
    @Autowired
    BookingRepository bookingRepository;
    @Autowired
    DriverRepository driverRepository;
    @Autowired
    BookingService bookingService;
    Set<Booking> scheduledBookings = new HashSet<>();
    @Scheduled(fixedRate = 1000)
    public void consumer(){
        MQMessage m = messageQueue.consumeMessage(constants.getSchedulingTopicName());
        if(m==null)
        {
            return;
        }Message message = (Message)m;
        schedule(message.getBooking());
    }

    public void schedule(Booking booking) {
        scheduledBookings.add(booking);
    }
    @Scheduled(fixedRate = 60000)
    public void process(){
        Set<Booking> newScheduledBookings= new HashSet<>();
        for(Booking booking:scheduledBookings)
        {
            if(DateUtils.addMinutes(new Date(), constants.getBookingProcessBeforeTime()).after(booking.getScheduledTime())){
            bookingService.acceptBooking(booking.getDriver(), booking);
        }
            else{
                newScheduledBookings.add(booking);
            }
        }
        scheduledBookings= newScheduledBookings;


    }


    @Getter
    @Setter
    @AllArgsConstructor
    public static class Message implements MQMessage {
        private Booking booking;

    }
}
