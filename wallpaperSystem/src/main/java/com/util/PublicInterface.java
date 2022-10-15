package com.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicInterface<T> implements Serializable {
    //返回消息
    private String msg;
    //代码状态
    private int code;
    //回写数据
    private MyData data;
    //数据条数
    private int total;

}
