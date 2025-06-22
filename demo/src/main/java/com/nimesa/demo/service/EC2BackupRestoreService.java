package com.nimesa.demo.service;

import java.util.List;

import com.nimesa.demo.response.BackupResponse;
import com.nimesa.demo.response.ImageStateResponse;

public interface EC2BackupRestoreService{
    List<BackupResponse> backupAllInstances();
    String restore(List<String> snapshotId,String imageId);
    List<ImageStateResponse> checkImageBackupStatus(List<String> imageId);
    List<String> discover();

}