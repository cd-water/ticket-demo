package com.cdwater.cdticket.admin.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 分页响应
 */
@Data
@AllArgsConstructor
public class PageResult<T> {

    private long total;
    private List<T> records;
    private long page;
    private long size;

    public static <T> PageResult<T> of(IPage<T> page) {
        return new PageResult<>(page.getTotal(), page.getRecords(), page.getCurrent(), page.getSize());
    }
}
