package com.nimesa.demo.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.nimesa.demo.entity.EC2InstanceDetails;


public interface EC2Repository extends JpaRepository<EC2InstanceDetails, UUID>{
    @Query("SELECT o.instanceId FROM EC2InstanceDetails o")
    List<String> findAllInstanceIds();
    @Transactional
    void deleteByInstanceIdIn( Collection<String> instanceId);
    
}