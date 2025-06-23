package com.nimesa.demo.response;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class JobStatusResponse {
    private UUID jobId;
    private String serviceName;
    private String status;

    public JobStatusResponse(UUID jobId, String status){
        this.jobId=jobId;
        this.status=status;
    }
    
}
