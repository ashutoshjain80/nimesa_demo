package com.nimesa.demo.controller;

import java.util.ArrayList;
import java.util.List;

import com.nimesa.demo.response.BackupResponse;
import com.nimesa.demo.service.EC2BackupRestoreService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EC2BackupRestoreController{

    private final EC2BackupRestoreService ec2BackupRestoreService;

    public EC2BackupRestoreController(EC2BackupRestoreService ec2BackupRestoreService){
        this.ec2BackupRestoreService=ec2BackupRestoreService;
    }

    @PostMapping("/backup")
    public ResponseEntity<?> backup(){
       return new ResponseEntity<>(ec2BackupRestoreService.backupAllInstances(),HttpStatus.OK);  
    }

    @GetMapping("/image/status")
    public ResponseEntity<String> checkImageStatus(@RequestParam String imageId){
       String state= ec2BackupRestoreService.checkImageBackupStatus(imageId);
       return new ResponseEntity<>(state,HttpStatus.OK);
    }

    @PostMapping ("/restore")
    public ResponseEntity<List<String>> restore(@RequestBody List<BackupResponse> restoreRequest){
        
        List<String> instanceIds=new ArrayList<>();
        for(BackupResponse req:restoreRequest){
            String instanceId=ec2BackupRestoreService.restore(req.getSnapshotId(),req.getImageId());
            instanceIds.add(instanceId);

        }
        return new ResponseEntity<>(instanceIds,HttpStatus.OK); 
    }

}