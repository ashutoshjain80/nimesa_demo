package com.nimesa.demo.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;


import com.nimesa.demo.entity.S3ObjectEntity;



public interface S3ObjectRepository extends JpaRepository<S3ObjectEntity, UUID> {
}

