package com.nimesa.demo.service;

import java.util.List;

import com.amazonaws.services.s3.model.ListBucketsPaginatedRequest;
import com.nimesa.demo.response.BackupResponse;

import com.nimesa.demo.response.ImageStateResponse;
import com.nimesa.demo.response.JobStatusResponse;

public interface EC2BackupRestoreService{
    List<BackupResponse> backupAllInstances();
    String restore(List<String> snapshotId,String imageId);
    List<ImageStateResponse> checkImageBackupStatus(List<String> imageId);
    List<ImageStateResponse> discover();
    JobStatusResponse discover_new();
    JobStatusResponse listAllS3Buckets_new(ListBucketsPaginatedRequest paginatedRequest);
    List<String> listAllS3Buckets(ListBucketsPaginatedRequest paginatedRequest);
    List<JobStatusResponse> getJobStatus(List<String> jobIds);

}