package com.nimesa.demo.service.impl;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.AmazonEC2Exception;
import com.amazonaws.services.ec2.model.AttachVolumeRequest;
import com.amazonaws.services.ec2.model.CreateSnapshotRequest;
import com.amazonaws.services.ec2.model.CreateSnapshotResult;
import com.amazonaws.services.ec2.model.CreateVolumeRequest;
import com.amazonaws.services.ec2.model.CreateVolumeResult;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.nimesa.demo.config.AmazonEC2Client;
import com.nimesa.demo.response.BackupResponse;
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
            DescribeInstancesRequest request= new DescribeInstancesRequest();
            DescribeInstancesResult response = ec2Client.describeInstances(request);
            
            for(Reservation reservation : response.getReservations()){
                for(Instance instance : reservation.getInstances()){    
                    String volumeId = instance.getBlockDeviceMappings().get(0).getEbs().getVolumeId();
                    BackupResponse resp= new BackupResponse();
                    CreateSnapshotRequest snapshotRequest = new CreateSnapshotRequest().withVolumeId(volumeId).withDescription("Backup of instance : "+instance.getInstanceId());
                    CreateSnapshotResult snapshotResult=ec2Client.createSnapshot(snapshotRequest);
                    resp.setInstanceId(instance.getInstanceId());
                    resp.setSnapshotId(snapshotResult.getSnapshot().getSnapshotId());
                    backupDetailsList.add(resp);
                    System.out.println(instance.getBlockDeviceMappings().get(0).getDeviceName());
                    System.out.println ("Snapshot Created: "+ snapshotResult.getSnapshot().getSnapshotId());

                }
            }
        }
        catch(AmazonServiceException ase){
            System.err.println("Amazon Service error" + ase.getErrorMessage());
        }
        catch(AmazonClientException ace){
            System.err.println("Amazon Client error" + ace.getMessage());
        }
        catch(Exception e){
            System.err.println("Error During Backup: "+e.getMessage() );
        }
        return backupDetailsList;
    }

    @Override
    public String restore(String snapshotId, String instanceId ) {
       try{
        CreateVolumeRequest createVolumeRequest =new CreateVolumeRequest().withSnapshotId(snapshotId).withAvailabilityZone("ap-south-1b");
       CreateVolumeResult createVolumeResult = ec2Client.createVolume(createVolumeRequest);
       String volumeId=createVolumeResult.getVolume().getVolumeId();
       System.out.println("Volume Created: "+volumeId);
       
       AttachVolumeRequest attachVolumeRequest = new AttachVolumeRequest().withVolumeId(volumeId).withInstanceId(instanceId).withDevice("/dev/xvdc");
       ec2Client.attachVolume(attachVolumeRequest);
       System.out.println("Volume attached to response: "+ instanceId);
       }
       catch(AmazonEC2Exception ec2Exception){
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
           
            e.printStackTrace();
        }
       }
       return instanceId;
    }
    
}