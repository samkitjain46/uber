package com.uber.uberapi.models;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="passenger")
public class Passenger extends Auditable {
    @OneToOne(cascade = CascadeType.ALL)
    private Account account;

    private String name;
    @OneToOne
    private Review avgRating;
    @Enumerated(value = EnumType.STRING)
    private Gender gender;

    @OneToMany(mappedBy = "passenger")
    private List<Booking> bookings = new ArrayList<>();

    @Temporal(value=TemporalType.DATE)
    private Date dob;

    private String phonenumber;
    @OneToOne
    private Location home;
    @OneToOne
    private Location work;
    @OneToOne
    private Location lastknownLocation;





}
