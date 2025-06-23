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
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Data
@Table(name = "S3_objets")
@NoArgsConstructor
public class S3ObjectEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String bucketName;
    private String objectKey;
    
    public S3ObjectEntity(String bucketName,String objectKey){
        this.bucketName=bucketName;
        this.objectKey=objectKey;
    }
}
