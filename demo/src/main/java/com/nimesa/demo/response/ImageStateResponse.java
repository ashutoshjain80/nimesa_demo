package com.nimesa.demo.response;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ImageStateResponse {
    private String imageId;
    private String state;
    
}
