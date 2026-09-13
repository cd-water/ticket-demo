package com.cdwater.cdticket.admin.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cdwater.cdticket.admin.common.exception.BizException;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

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

    public static void check(int page, int size) {
        if (page < 1 || size < 1 || size > 100) {
            throw new BizException(ResultCode.BAD_REQUEST);
        }
    }
}
