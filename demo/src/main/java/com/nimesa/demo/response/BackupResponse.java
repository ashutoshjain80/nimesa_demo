package com.nimesa.demo.response;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
public class BackupResponse {
    private String snapshotId;
    private String instanceId;
}
