package com.cdwater.cdticket.admin.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PageResultTest {

    @Test
    void of_copiesPageFields() {
        IPage<String> page = mock(IPage.class);
        when(page.getTotal()).thenReturn(100L);
        when(page.getRecords()).thenReturn(List.of("a", "b"));
        when(page.getCurrent()).thenReturn(2L);
        when(page.getSize()).thenReturn(10L);

        PageResult<String> pr = PageResult.of(page);
        assertThat(pr.getTotal()).isEqualTo(100);
        assertThat(pr.getRecords()).containsExactly("a", "b");
        assertThat(pr.getPage()).isEqualTo(2);
        assertThat(pr.getSize()).isEqualTo(10);
    }
}
