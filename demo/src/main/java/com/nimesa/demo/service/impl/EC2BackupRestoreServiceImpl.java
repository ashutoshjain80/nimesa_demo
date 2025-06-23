package com.nimesa.demo.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.AmazonEC2Exception;
import com.amazonaws.services.ec2.model.CreateImageRequest;
import com.amazonaws.services.ec2.model.CreateImageResult;

import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;

import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ListBucketsPaginatedRequest;
import com.amazonaws.services.s3.model.ListBucketsPaginatedResult;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.nimesa.demo.entity.EC2InstanceDetails;
import com.nimesa.demo.entity.JobStatusEntity;
import com.nimesa.demo.entity.S3BucketDetails;
import com.nimesa.demo.entity.S3ObjectEntity;
import com.nimesa.demo.repository.EC2Repository;
import com.nimesa.demo.repository.JobRepository;
import com.nimesa.demo.repository.S3BucketRepository;
import com.nimesa.demo.repository.S3ObjectRepository;
import com.nimesa.demo.response.BackupResponse;

import com.nimesa.demo.response.ImageStateResponse;
import com.nimesa.demo.response.JobStatusResponse;
import com.nimesa.demo.service.EC2BackupRestoreService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EC2BackupRestoreServiceImpl implements EC2BackupRestoreService
{

    private final AmazonEC2 ec2Client;
    private final AmazonS3 s3Client;
    private final JobRepository jobRepository;
    private final S3BucketRepository s3BucketRepository;
    private final EC2Repository ec2Repository;
    private final S3ObjectRepository s3ObjectRepository;

    public EC2BackupRestoreServiceImpl(AmazonEC2 ec2Client, AmazonS3 s3Client ,JobRepository jobRepository,
    S3BucketRepository s3BucketRepository, EC2Repository ec2Repository,S3ObjectRepository s3ObjectRepository){
        this.ec2Client=ec2Client;
        this.s3Client=s3Client;
        this.jobRepository=jobRepository;
        this.s3BucketRepository=s3BucketRepository;
        this.ec2Repository=ec2Repository;
        this.s3ObjectRepository=s3ObjectRepository;
        
    }
    
    @Override
    public List<BackupResponse> backupAllInstances() {
        List<BackupResponse> backupDetailsList= new ArrayList<>();
        try{
            DescribeInstancesRequest request= new DescribeInstancesRequest().withFilters();
            Filter stateFilter = new Filter().withName("instance-state-name").withValues("running","stopped");
            request.withFilters(stateFilter);
            DescribeInstancesResult response = ec2Client.describeInstances(request);
            
            for(Reservation reservation : response.getReservations()){
                for(Instance instance : reservation.getInstances()){
                    String instanceId=instance.getInstanceId();
                    CreateImageRequest createImageRequest=new CreateImageRequest().withInstanceId(instanceId).withName("backup-" +instanceId +"-" +System.currentTimeMillis());
                    CreateImageResult createImageResult=ec2Client.createImage(createImageRequest);
                    String imageId=createImageResult.getImageId();
                    System.out.println("Created AMI: "+createImageResult.getImageId());
                    List<String> snapshotIds=new ArrayList<>();
                    backupDetailsList.add(new BackupResponse(instanceId,imageId,snapshotIds));
                }
            }
        }
        catch(AmazonServiceException ase){
            System.err.println("Amazon Service error" + ase.getErrorMessage());
            throw new AmazonServiceException(ase.getErrorMessage());
        }
        catch(AmazonClientException ace){
            System.err.println("Amazon Client error" + ace.getMessage());
            throw new RuntimeException(ace.getMessage());
        }
        catch(Exception e){
            System.err.println("Error During Backup: "+e.getMessage() );
            throw new RuntimeException("Error while taking EC2 Backup");
        }
        return backupDetailsList;
    }

    @Override
    public String restore(List<String> snapshotIds, String imageId ) {
        String newInstanceId="";
        try{
            RunInstancesRequest runInstanceRequest=new RunInstancesRequest().withImageId(imageId).withInstanceType("t2.micro").withMinCount(1).withMaxCount(1);
            RunInstancesResult runInstancesResult=ec2Client.runInstances(runInstanceRequest);
            newInstanceId=runInstancesResult.getReservation().getInstances().get(0).getInstanceId();
            System.out.println("New EC2 instanceId: "+ newInstanceId);
        }
        catch(AmazonEC2Exception ec2Exception){
            
            System.err.println("Error while restoring EC2"+ ec2Exception.getErrorMessage());
            throw new AmazonEC2Exception(ec2Exception.getErrorMessage());
        }
        return newInstanceId;
    }

    @Override
    public List<ImageStateResponse> checkImageBackupStatus(List<String> imageId) {
        List<ImageStateResponse> imageStateList=new ArrayList<>();
        try{
            DescribeImagesRequest describeImagesRequest=new DescribeImagesRequest().withImageIds(imageId);
            DescribeImagesResult result=ec2Client.describeImages(describeImagesRequest);
        
            if(!result.getImages().isEmpty()){
                for(int i=0;i<result.getImages().size();i++){
                    ImageStateResponse resp=new ImageStateResponse();
                    resp.setImageId(result.getImages().get(i).getImageId());
                    resp.setState(result.getImages().get(i).getState());
                    imageStateList.add(resp);
                }
                
            }
        }
        catch(AmazonEC2Exception ex){
            throw new AmazonEC2Exception(ex.getErrorMessage());
        }

        return imageStateList;
    }

    @Override
    public JobStatusResponse listAllS3Buckets_new(ListBucketsPaginatedRequest paginatedRequest) {
       // List<String> bucketNames=new ArrayList<>();
        
        JobStatusResponse jobStatusResponse=storeJobDetails("S3","IN-PROGRESS");
        storeS3BucketDetails(jobStatusResponse.getJobId(),paginatedRequest);
        return jobStatusResponse;

        
}

     @Override
    public JobStatusResponse discover_new(String serviceName) {
        
        JobStatusResponse jobStatusResponse=storeJobDetails("EC2","IN-PROGRESS");
        storeEC2InstanceDetails(jobStatusResponse.getJobId());
        return jobStatusResponse;

   
    }

    public List<JobStatusResponse> getJobStatus(List<String> jobIds){
          return jobIds.stream()
        .map(id -> jobRepository.findById(UUID.fromString(id))
            .map(job -> new JobStatusResponse(job.getId(), job.getStatus()))
            .orElse(null))
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
    }

     public Page<EC2InstanceDetails> getEC2InstanceDetails(UUID jobId,int page ,int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ec2Repository.findAllByJobId(jobId, pageable);
     }

     public Page<S3BucketDetails> getS3BucketDetails(UUID jobId,int page ,int size){
        Pageable pageable = PageRequest.of(page, size);
        return s3BucketRepository.findAllByJobId(jobId, pageable);
     }

     public JobStatusResponse createJobAndStoreS3Objects(String bucketName){
       JobStatusResponse response=storeJobDetails("S3-Object","IN-PROGRESS");
        storeS3ObjectsAsync(response.getJobId(), bucketName);  // fire and forget
        return response;
    }

    
    private JobStatusResponse storeJobDetails(String serviceName,String status){
        JobStatusEntity job=new JobStatusEntity();
        JobStatusResponse response=new JobStatusResponse();
        job.setService(serviceName);
        job.setStatus("IN-PROGRESS");
         job.setCreatedAt(LocalDateTime.now());
        job.setUpdatedAt(LocalDateTime.now());
        job= jobRepository.save(job);
        response.setJobId(job.getId());
        response.setStatus(job.getStatus());
        response.setServiceName(job.getService());
        return response;
    }
    
   @Async
    public CompletableFuture<Void> storeS3ObjectsAsync(UUID jobId, String bucketName) {
       try{
        String continuationToken = null;
        ListObjectsV2Result result;

        List<S3ObjectEntity> allObjects = new ArrayList<>();
        
        do {
            ListObjectsV2Request request = new ListObjectsV2Request()
                    .withBucketName(bucketName)
                    .withContinuationToken(continuationToken)
                    .withMaxKeys(10);

            result = s3Client.listObjectsV2(request);

            for (S3ObjectSummary summary : result.getObjectSummaries()) {
                allObjects.add(new S3ObjectEntity(jobId,bucketName, summary.getKey()));
            }

            continuationToken = result.getNextContinuationToken();
        } while (result.isTruncated());

        s3ObjectRepository.saveAll(allObjects);

        jobRepository.findById(jobId).ifPresent(job -> {
                job.setStatus("COMPLETED");
                jobRepository.save(job);
            });
    }
    catch (Exception e) {
            // Mark job as FAILED
            jobRepository.findById(jobId).ifPresent(job -> {
                job.setStatus("FAILED");
                jobRepository.save(job);
            });
       
    }
     return CompletableFuture.completedFuture(null);
}

@Async
private CompletableFuture<Void> storeEC2InstanceDetails(UUID jobId){
    String nextToken=null;
    List<EC2InstanceDetails> instanceDetailsList=new ArrayList<>();
    List<ImageStateResponse> instanceStateList = new ArrayList<>();
    try{
        do {
        DescribeInstancesRequest request = new DescribeInstancesRequest()
            .withNextToken(nextToken)
            .withFilters(new Filter()
                .withName("instance-state-name")
                .withValues("running", "stopped"));

        DescribeInstancesResult response = ec2Client.describeInstances(request);

        for (Reservation reservation : response.getReservations()) {
            for (Instance instance : reservation.getInstances()) {
                ImageStateResponse resp = new ImageStateResponse();
                resp.setImageId(instance.getInstanceId());
                resp.setState(instance.getState().getName());
                 EC2InstanceDetails instanceDetails=new EC2InstanceDetails();
                 instanceDetails.setJobId(jobId);
                 instanceDetails.setInstanceId(instance.getInstanceId());
                 instanceDetails.setInstanceType(instance.getInstanceType());
                 instanceDetails.setState(instance.getState().getName());
                instanceDetailsList.add(instanceDetails);

                instanceStateList.add(resp);
            }
        }

        nextToken = response.getNextToken();
    } while (nextToken != null);
       
         
        ec2Repository.saveAll(instanceDetailsList);
        jobRepository.findById(jobId).ifPresent(job -> {
                job.setStatus("COMPLETED");
                jobRepository.save(job);
            });


    // Inform the user that all instances have been listed
    System.out.println("All instances have been listed. Total count: " + instanceStateList.size());
        }
    catch(Exception e){
        jobRepository.findById(jobId).ifPresent(job -> {
                job.setStatus("FAILED");
                jobRepository.save(job);
        });
    }

    return CompletableFuture.completedFuture(null);

}
private CompletableFuture<Void> storeS3BucketDetails(UUID jobId,ListBucketsPaginatedRequest paginatedRequest){
    List<S3BucketDetails> s3DetailsList=new ArrayList<>();       
        String continuationToken=paginatedRequest.getContinuationToken();
       try{
        do{
            paginatedRequest.setContinuationToken(continuationToken);
            ListBucketsPaginatedResult result= s3Client.listBuckets(paginatedRequest);
            for(Bucket bucket: result.getBuckets()){
                 S3BucketDetails bucketDetails=new S3BucketDetails();
                bucketDetails.setBucketName(bucket.getName());
                bucketDetails.setJobId(jobId);
                s3DetailsList.add(bucketDetails);
               // bucketNames.add(bucket.getName());
            }
            continuationToken=result.getContinuationToken();
        }while (continuationToken!=null);
        s3BucketRepository.saveAll(s3DetailsList);
        jobRepository.findById(jobId).ifPresent(job -> {
                job.setStatus("COMPLETED");
                jobRepository.save(job);
            });
        }
        catch(Exception e){
        jobRepository.findById(jobId).ifPresent(job -> {
                job.setStatus("FAILED");
                jobRepository.save(job);
        });
    }

return CompletableFuture.completedFuture(null);
}
}