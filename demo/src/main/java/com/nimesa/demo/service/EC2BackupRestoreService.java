package com.nimesa.demo.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;


import com.amazonaws.services.s3.model.ListBucketsPaginatedRequest;
import com.nimesa.demo.entity.EC2InstanceDetails;
import com.nimesa.demo.entity.S3BucketDetails;
import com.nimesa.demo.entity.S3ObjectEntity;
import com.nimesa.demo.response.BackupResponse;

import com.nimesa.demo.response.ImageStateResponse;
import com.nimesa.demo.response.JobStatusResponse;

public interface EC2BackupRestoreService{
    List<BackupResponse> backupAllInstances();
    String restore(List<String> snapshotId,String imageId);
    List<ImageStateResponse> checkImageBackupStatus(List<String> imageId);
    JobStatusResponse discover_new(String servieName);
    JobStatusResponse listAllS3Buckets_new(ListBucketsPaginatedRequest paginatedRequest);
    List<JobStatusResponse> getJobStatus(List<String> jobIds);
    Page<EC2InstanceDetails> getEC2InstanceDetails(int page,int size);
    Page<S3BucketDetails> getS3BucketDetails(int page ,int size);
    JobStatusResponse createJobAndStoreS3Objects(String bucketName);
    long getS3ObjectCount(String bucketName);
    Page<S3ObjectEntity> getS3Objects(String bucketName,String searchPattern,int page,int size);



}