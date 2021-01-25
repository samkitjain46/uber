package com.uber.uberapi.models;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="namedlocation")
public class NamedLocation extends Auditable {
    @OneToOne
    private Location location;
    private String name;
    private String zipCode;
    private String city;
    private String country;
    private String state;
}
