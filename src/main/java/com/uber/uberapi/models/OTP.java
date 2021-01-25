package com.uber.uberapi.models;

import com.uber.uberapi.Exceptions.InvalidOTPException;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "otp")
public class OTP extends Auditable{
    private String code;
    private String sentToNumber;

    public static OTP make(String phonenumber) {
        return OTP.builder().code("0000").sentToNumber(phonenumber).build();
    }


    public boolean validateEnteredOTP(OTP otp,Integer expiryMinutes) {
        if(!code.equals(otp.getCode()))
        {
            return false;
        }
        return true;

    }
}
