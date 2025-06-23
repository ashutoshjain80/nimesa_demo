package com.nimesa.demo.response;

import java.util.UUID;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class JobStatusResponse {
    private UUID jobId;
    private String serviceName;
    
}
