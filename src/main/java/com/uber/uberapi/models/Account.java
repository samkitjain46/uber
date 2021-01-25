package com.uber.uberapi.models;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "account")
public class Account extends Auditable{
    @Column(unique = true,nullable = false)
    private String username;

    private String password;

    @ManyToMany
    private List<Role> roles = new ArrayList<>();
    //private List<Location> route = new ArrayList<>();
}
