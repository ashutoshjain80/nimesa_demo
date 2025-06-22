package com.nimesa.demo.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_EMPTY)

public class BackupResponse {
    private List<String> snapshotId;
    private String instanceId;
    private String imageId;

    public BackupResponse(String instanceId, String imageId, List<String> snapshotIds){
        this.imageId=imageId;
        this.instanceId=instanceId;
        this.snapshotId=snapshotIds;
    }
}
