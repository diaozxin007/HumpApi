package com.xilidou.entity;


import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class ApiResult {

    private int code;

    private String msg;

    private List<String> list;

}
