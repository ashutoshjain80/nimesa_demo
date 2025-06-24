package com.nimesa.demo.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.amazonaws.Response;
import com.amazonaws.services.medialive.model.SrtSettings;
import com.amazonaws.services.s3.event.S3EventNotification.S3ObjectEntity;
import com.amazonaws.services.s3.model.ListBucketsPaginatedRequest;
import com.nimesa.demo.entity.EC2InstanceDetails;
import com.nimesa.demo.entity.S3BucketDetails;
import com.nimesa.demo.response.BackupResponse;
import com.nimesa.demo.response.ImageStateResponse;
import com.nimesa.demo.response.JobStatusResponse;
import com.nimesa.demo.response.PagedResponse;
import com.nimesa.demo.service.EC2BackupRestoreService;

import org.springframework.data.domain.Page;
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
    
    if (hasEC2) result.add(ec2BackupRestoreService.discover_new("EC2"));
    if (hasS3) result.add(ec2BackupRestoreService.listAllS3Buckets_new(paginatdRequest()));
        return  new ResponseEntity<>(result,HttpStatus.OK);
    }



    @GetMapping("/job/status")
    public ResponseEntity<List<JobStatusResponse>> jobStaus(@RequestParam List<String> jobIds){
      List<JobStatusResponse> jobStatusResp=ec2BackupRestoreService.getJobStatus(jobIds);
      return new ResponseEntity<>(jobStatusResp,HttpStatus.OK);
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

    @GetMapping("/discovery/result")
       public ResponseEntity<PagedResponse<?>> discoverServicesResult(@RequestParam String service,@RequestParam(defaultValue = "0") int page,
        @RequestParam (defaultValue = "10") int size) throws Exception{
        if(service.contains("EC2")){
           Page<EC2InstanceDetails> ec2Response=ec2BackupRestoreService.getEC2InstanceDetails(page,size);
            PagedResponse<EC2InstanceDetails> resp=new PagedResponse<>();
            resp.setContent(ec2Response.getContent());
            resp.setTotalElements(ec2Response.getTotalElements());
            return new ResponseEntity<>(resp,HttpStatus.OK);
        }
        if(service.contains("S3")){
             Page<S3BucketDetails> s3Bucketetails=ec2BackupRestoreService.getS3BucketDetails(page,size);
             PagedResponse<S3BucketDetails> resp=new PagedResponse<>();
            resp.setContent(s3Bucketetails.getContent());
            resp.setTotalElements(s3Bucketetails.getTotalElements());
            return new ResponseEntity<>(resp,HttpStatus.OK);
        }
      throw new Exception("Service Not Supported");
      
    }
    
    @GetMapping("/bucket/details")
    public ResponseEntity<JobStatusResponse> storeS3BucketDtails(@RequestParam String bucketName){
        return new ResponseEntity<>(ec2BackupRestoreService.createJobAndStoreS3Objects(bucketName),HttpStatus.OK);
    }

    @GetMapping("/bucket/objectCount")
    public ResponseEntity<Long> getS3BucketObjetCount(@RequestParam String bucketName){
        return new ResponseEntity<>(ec2BackupRestoreService.getS3ObjectCount(bucketName),HttpStatus.OK);
    }

    @GetMapping("/bucket/objects")
    public ResponseEntity<PagedResponse<?>> getS3BucketObjetCount(@RequestParam  String bucketName,@RequestParam(required=false) String searchPattern,
    @RequestParam(defaultValue = "0") int page,
        @RequestParam (defaultValue = "10") int size){
        Page<com.nimesa.demo.entity.S3ObjectEntity> s3Ojects=ec2BackupRestoreService.getS3Objects(bucketName,searchPattern,page,size);
         PagedResponse<com.nimesa.demo.entity.S3ObjectEntity> resp=new PagedResponse<>();
            resp.setContent(s3Ojects.getContent());
            resp.setTotalElements(s3Ojects.getTotalElements());
            return new ResponseEntity<>(resp,HttpStatus.OK);
    }

    private ListBucketsPaginatedRequest paginatdRequest(){
        ListBucketsPaginatedRequest request=new ListBucketsPaginatedRequest();
        request.setContinuationToken(null);
        return request;
 
    }

    



}