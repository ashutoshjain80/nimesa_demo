package com.nimesa.demo.exception;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.model.AmazonEC2Exception;
import com.nimesa.demo.response.ErrorResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler{

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleAmazonEC2Exception(AmazonEC2Exception ex){
        ErrorResponse resp=new ErrorResponse();
        resp.setErrorMessage(ex.getErrorMessage());
        resp.setErrorCode(ex.getErrorCode());
        return new ResponseEntity<>(resp,HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleAmazonServiceExceprion(AmazonServiceException ex){
        ErrorResponse resp=new ErrorResponse();
        resp.setErrorMessage(ex.getErrorMessage());
        resp.setErrorCode(ex.getErrorCode());
        return new ResponseEntity<>(resp,HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleAmazonClientException(AmazonClientException ex){
        ErrorResponse resp=new ErrorResponse();
        resp.setErrorMessage(ex.getMessage());
        resp.setErrorCode("500");
        return new ResponseEntity<>(resp,HttpStatus.INTERNAL_SERVER_ERROR);
    }



    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleAmazonEC2Exception(Exception ex){
        ErrorResponse resp=new ErrorResponse();
        resp.setErrorMessage(ex.getMessage());
        resp.setErrorCode("500");
        return new ResponseEntity<>(resp,HttpStatus.INTERNAL_SERVER_ERROR);
    }
}