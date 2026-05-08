package com.wuyou.common.page;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;
import java.util.List;

@Data
public class PageResult<T> {
    private long total;
    private long page;
    private long size;
    private List<T> list;

    public static <T> PageResult<T> of(IPage<T> page) {
        PageResult<T> result = new PageResult<>();
        result.setTotal(page.getTotal());
        result.setPage(page.getCurrent());
        result.setSize(page.getSize());
        result.setList(page.getRecords());
        return result;
    }
}
