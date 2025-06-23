package com.nimesa.demo.controller;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.medialive.model.SrtSettings;
import com.amazonaws.services.s3.model.ListBucketsPaginatedRequest;
import com.nimesa.demo.response.BackupResponse;
import com.nimesa.demo.response.ImageStateResponse;
import com.nimesa.demo.response.JobStatusResponse;
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
    public ResponseEntity<List<ImageStateResponse>> checkImageStatus(@RequestParam List<String> imageId){
       List<ImageStateResponse> state= ec2BackupRestoreService.checkImageBackupStatus(imageId);
       return new ResponseEntity<>(state,HttpStatus.OK);
    }

    @GetMapping("/discover")
    public ResponseEntity<List<JobStatusResponse>> discoverServices(@RequestParam List<String> services){
         List<JobStatusResponse> result = new ArrayList<>();
    boolean hasS3 = services.stream().anyMatch(s -> s.equalsIgnoreCase("S3"));
    boolean hasEC2 = services.stream().anyMatch(s -> s.equalsIgnoreCase("EC2"));
    
    if (hasEC2) result.add(ec2BackupRestoreService.discover_new());
    if (hasS3) result.add(ec2BackupRestoreService.listAllS3Buckets_new(paginatdRequest()));
        return  new ResponseEntity<>(result,HttpStatus.OK);
    }

    @GetMapping("/job/status")
    public ResponseEntity<List<JobStatusResponse>> jobStaus(@RequestParam List<String> jobIds){
      List<JobStatusResponse> jobStatusResp=ec2BackupRestoreService.getJobStatus(jobIds);
      return new ResponseEntity<>(jobStatusResp,HttpStatus.OK);
    }

    @GetMapping("/instances/discover")
    public ResponseEntity<List<ImageStateResponse>> discover(){
        List<ImageStateResponse> discoveredInstances= ec2BackupRestoreService.discover();
        List<String> S3BucketNames= ec2BackupRestoreService.listAllS3Buckets(paginatdRequest());
        S3BucketNames.forEach(System.out::println);
       return new ResponseEntity<>(discoveredInstances,HttpStatus.OK);
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

    private ListBucketsPaginatedRequest paginatdRequest(){
        ListBucketsPaginatedRequest request=new ListBucketsPaginatedRequest();
        request.setContinuationToken(null);
        return request;
 
    }



}