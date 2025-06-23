package com.nimesa.demo.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Data
@Table(name = "S3_bucket_name")
public class S3BucketDetails {
     @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private UUID jobId;
    private String bucketName; 
    
}
