package com.uber.uberapi.models;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="car")
public class Car extends Auditable {
    @ManyToOne
    private  Color color;

    @OneToOne
    private Driver driver;

    private String plateNumber;
    private String brandAndModel;
    @Enumerated(value=EnumType.STRING)
    private CarType carType;


}
