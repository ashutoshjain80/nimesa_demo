package com.nimesa.demo.service.impl;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.AmazonEC2Exception;
import com.amazonaws.services.ec2.model.AttachVolumeRequest;
import com.amazonaws.services.ec2.model.CreateImageRequest;
import com.amazonaws.services.ec2.model.CreateImageResult;
import com.amazonaws.services.ec2.model.CreateSnapshotRequest;
import com.amazonaws.services.ec2.model.CreateSnapshotResult;
import com.amazonaws.services.ec2.model.CreateVolumeRequest;
import com.amazonaws.services.ec2.model.CreateVolumeResult;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.DescribeVolumesRequest;
import com.amazonaws.services.ec2.model.DescribeVolumesResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.ImageState;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceBlockDeviceMapping;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.nimesa.demo.response.BackupResponse;
import com.nimesa.demo.response.ImageStateResponse;
import com.nimesa.demo.service.EC2BackupRestoreService;

import org.springframework.stereotype.Service;

@Service
public class EC2BackupRestoreServiceImpl implements EC2BackupRestoreService
{

    private final AmazonEC2 ec2Client;
   public EC2BackupRestoreServiceImpl(AmazonEC2 ec2Client){
       this.ec2Client=ec2Client;
       
   }
   
    @Override
    public List<BackupResponse> backupAllInstances() {
        List<BackupResponse> backupDetailsList= new ArrayList<>();
        try{
            
            
            DescribeInstancesRequest request= new DescribeInstancesRequest().withFilters();
            Filter stateFilter = new Filter().withName("instance-state-name").withValues("running");
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
                    // for(InstanceBlockDeviceMapping mapping:instance.getBlockDeviceMappings()){
                    //     String volumeId = mapping.getEbs().getVolumeId();
                    //     CreateSnapshotRequest snapshotRequest = new CreateSnapshotRequest().withVolumeId(volumeId).withDescription("Backup of instance : "+instance.getInstanceId());
                    //     CreateSnapshotResult snapshotResult=ec2Client.createSnapshot(snapshotRequest); 
                    //     snapshotIds.add(snapshotResult.getSnapshot().getSnapshotId());
                        
                    // }
                    backupDetailsList.add(new BackupResponse(instanceId,imageId,snapshotIds));

                }
            }
        }
        catch(AmazonServiceException ase){
            System.err.println("Amazon Service error" + ase.getErrorMessage());
            throw new RuntimeException("Error while taking EC2 Backup ");
        }
        catch(AmazonClientException ace){
            System.err.println("Amazon Client error" + ace.getMessage());
            throw new RuntimeException("Error while taking EC2 Backup ");
        }
        catch(Exception e){
            System.err.println("Error During Backup: "+e.getMessage() );
            throw new RuntimeException("Error while taking EC2 Backup ");
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

        // for(int i=0;i<snapshotIds.size();i++){
        //     String snapshotId= snapshotIds.get(i);
        //     String availabilityZone=runInstancesResult.getReservation().getInstances().get(0).getPlacement().getAvailabilityZone();
        //     CreateVolumeRequest createVolumeRequest =new CreateVolumeRequest().withSnapshotId(snapshotId).withAvailabilityZone(availabilityZone);
        //     CreateVolumeResult createVolumeResult = ec2Client.createVolume(createVolumeRequest);
        //     String volumeId=createVolumeResult.getVolume().getVolumeId();
        //     System.out.println("Volume Created: "+volumeId);   

        //     waitForVolumeAvailable(volumeId, 50, 5000);
        //     AttachVolumeRequest attachVolumeRequest = new AttachVolumeRequest().withVolumeId(volumeId).withInstanceId(newInstanceId).withDevice("/dev/xvd"+(char)('b'+i));
        //     ec2Client.attachVolume(attachVolumeRequest);
        //     System.out.println("Volume attached to response: "+ newInstanceId);
        // }
       }
       catch(AmazonEC2Exception ec2Exception){
        
        System.err.println("Error while restoring EC2"+ ec2Exception.getErrorMessage());
        throw new RuntimeException("Error while creating EC2 Instance");
       }
       return newInstanceId;
    }

    private void waitForVolumeAvailable(String volumeId,int maxAttemps,long sleepmills){
        int attempts=0;
        while(attempts<maxAttemps){
            DescribeVolumesRequest describeVolumesRequest= new DescribeVolumesRequest().withVolumeIds(volumeId);
            DescribeVolumesResult result=ec2Client.describeVolumes(describeVolumesRequest);
            String state=result.getVolumes().get(0).getState();
            if("available".equalsIgnoreCase(state)){
                return;
            }
            try{
                Thread.sleep(sleepmills);
            }
            catch(InterruptedException e){
                Thread.currentThread().interrupt();
                throw new RuntimeException("Thread Interrupted");
            }
            attempts++;
        }
        throw new RuntimeException("Volume: "+volumeId + "did not become available in time");
    }

    @Override
    public List<ImageStateResponse> checkImageBackupStatus(List<String> imageId) {
        DescribeImagesRequest describeImagesRequest=new DescribeImagesRequest().withImageIds(imageId);
        DescribeImagesResult result=ec2Client.describeImages(describeImagesRequest);
        List<ImageStateResponse> imageStateList=new ArrayList<>();
        if(!result.getImages().isEmpty()){
            for(int i=0;i<result.getImages().size();i++){
                ImageStateResponse resp=new ImageStateResponse();
                resp.setImageId(result.getImages().get(i).getImageId());
                resp.setState(result.getImages().get(i).getState());
                imageStateList.add(resp);
            }
            
        }
        return imageStateList;
    }
    
}