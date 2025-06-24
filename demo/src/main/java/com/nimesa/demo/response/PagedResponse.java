package com.nimesa.demo.response;

import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
@Setter
@Getter
@Data
public class PagedResponse<T> {
    private List<T> content;
    private long totalElements;

}
