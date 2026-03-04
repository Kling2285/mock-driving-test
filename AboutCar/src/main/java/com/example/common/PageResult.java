package com.example.common;

import lombok.Data;
import java.util.List;

@Data
public class PageResult<T> {
    // 当前页码
    private Integer pageNum;
    // 每页条数
    private Integer pageSize;
    // 总条数
    private Long total;
    // 分页数据列表
    private List<T> list;

    // 构造方法
    public PageResult(Integer pageNum, Integer pageSize, Long total, List<T> list) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
        this.list = list;
    }

    // 空数据构造方法
    public static <T> PageResult<T> empty(Integer pageNum, Integer pageSize) {
        return new PageResult<>(pageNum, pageSize, 0L, null);
    }
}