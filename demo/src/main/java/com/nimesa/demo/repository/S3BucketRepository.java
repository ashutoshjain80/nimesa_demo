package com.nimesa.demo.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.nimesa.demo.entity.S3BucketDetails;

public interface S3BucketRepository extends JpaRepository<S3BucketDetails, UUID>{
     Page<S3BucketDetails> findAllByJobId(UUID jobId, Pageable pageable);
     
       @Query("SELECT o.bucketName FROM S3BucketDetails o")
     List<String> findAllBuckets();
    
    
}
