package com.cdwater.cdticket.common.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PageResultTest {

    @Test
    void ofBuildsFromMybatisPage() {
        Page<String> page = new Page<>(2, 10, 25);
        page.setRecords(List.of("a", "b"));
        PageResult<String> r = PageResult.of(page);
        assertEquals(25, r.getTotal());
        assertEquals(List.of("a", "b"), r.getRecords());
        assertEquals(2, r.getPage());
        assertEquals(10, r.getSize());
    }

    @Test
    void errorCodesExist() {
        assertNotNull(ResultCode.ADMIN_USERNAME_EXISTS.getCode());
        assertEquals("C501", ResultCode.SCREENING_TIME_CONFLICT.getCode());
        assertEquals("C503", ResultCode.SCREENING_STARTED.getCode());
    }
}
