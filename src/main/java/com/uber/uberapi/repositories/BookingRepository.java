package com.uber.uberapi.repositories;

import com.uber.uberapi.models.Account;
import com.uber.uberapi.models.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.awt.print.Book;

@Repository
public interface BookingRepository extends JpaRepository<Booking,Long> {
}
