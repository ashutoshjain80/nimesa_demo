package com.nimesa.demo.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nimesa.demo.entity.S3ObjectEntity;



public interface S3ObjectRepository extends JpaRepository<S3ObjectEntity, UUID> {
        long countByBucketName(String bucketName);
        List<S3ObjectEntity> findByBucketNameContaining(String searchPattern);
        List<S3ObjectEntity> findByBucketName(String bucketName);
        @Query("SELECT o.bucketName FROM S3ObjectEntity o where bucketName=:bucketName")
        List<String> findAllObjects(@Param("bucketName") String bucketName);
        void deleteByObjectKeyIn( Collection<String> keys);
}

