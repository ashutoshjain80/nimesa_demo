package com.nimesa.demo.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.nimesa.demo.entity.S3ObjectEntity;



public interface S3ObjectRepository extends JpaRepository<S3ObjectEntity, UUID> {
        long countByBucketName(String bucketName);
        List<S3ObjectEntity> findByBucketNameContaining(String searchPattern);
        Page<S3ObjectEntity> findByBucketName(String bucketNam,Pageable pageable);
        List<S3ObjectEntity> findByBucketNameAndObjectKeyContaining(String bucketName, String objectKeyPattern,Pageable pageable);
        @Query("SELECT o.objectKey FROM S3ObjectEntity o where bucketName=:bucketName")
        List<String> findAllObjects(@Param("bucketName") String bucketName);
        @Transactional
        void deleteByObjectKeyIn( Collection<String> keys);
        @Query("""
    SELECT o FROM S3ObjectEntity o
    WHERE LOWER(o.bucketName) = LOWER(:bucketName)
      AND LOWER(o.objectKey) LIKE LOWER(:pattern)
""")
Page<S3ObjectEntity> findByBucketAndObjectKeyPatternIgnoreCase(@Param("bucketName") String bucketName,
                                                               @Param("pattern") String pattern,Pageable pageable);

}

