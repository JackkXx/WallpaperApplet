package com.util;
//数据接口

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataInter<T> implements Serializable {
    //返回消息
    private String msg;
    //代码状态
    private int code;
    //是否请求成功
    private boolean status;
    //回写数据
    private T data;
    //数据条数
    private Integer total;
    //跳转路径
    private String path;

}
