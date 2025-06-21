package com.nimesa.demo.service;

import java.util.List;

import com.nimesa.demo.response.BackupResponse;

public interface EC2BackupRestoreService{
    List<BackupResponse> backupAllInstances();
    String restore(String snapshotId,String instanceId);

}