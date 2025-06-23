package com.nimesa.demo.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.nimesa.demo.entity.S3BucketDetails;

public interface S3BucketRepository extends JpaRepository<S3BucketDetails, UUID>{
       @Query("SELECT o.bucketName FROM S3BucketDetails o")
     List<String> findAllBuckets();
    
    
}
