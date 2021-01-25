package com.uber.uberapi.models;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@MappedSuperclass //dont create table for auditable
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class Auditable implements Serializable {
    //autogeerated, database to provide the id for us
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @CreatedDate
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @LastModifiedDate
    private Date updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true; //same object
        //check the class of noth
        if (o == null || getClass() != o.getClass()) return false;
        //cast to auditable
        Auditable auditable = (Auditable) o;
        //compare id
        if(id==null || auditable.id == null)
            return false;

        return id.equals(auditable.id);
    }

    @Override
    public int hashCode() {
        return id==null?0:id.hashCode();
    }



}