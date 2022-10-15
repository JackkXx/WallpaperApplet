package com.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MyData<T> {
    private List<T> list;
    //下一条记录的索引(获取壁纸时，接口需要)
    private int nextStart;

}
