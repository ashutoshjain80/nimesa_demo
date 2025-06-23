package com.nimesa.demo.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nimesa.demo.entity.JobStatusEntity;



public interface JobRepository extends JpaRepository<JobStatusEntity, UUID> {
}