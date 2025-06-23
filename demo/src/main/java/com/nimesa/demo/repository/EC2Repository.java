package com.nimesa.demo.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.nimesa.demo.entity.EC2InstanceDetails;


public interface EC2Repository extends JpaRepository<EC2InstanceDetails, UUID>{
    Page<EC2InstanceDetails> findAllByJobId(UUID jobId, Pageable pageable);
    
}