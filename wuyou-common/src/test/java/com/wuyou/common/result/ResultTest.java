package com.wuyou.common.result;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ResultTest {

    @Test
    void testSuccess() {
        Result<String> r = Result.success("hello");
        assertTrue(r.isSuccess());
        assertEquals(200, r.getCode());
        assertEquals("hello", r.getData());
    }

    @Test
    void testFail() {
        Result<String> r = Result.fail(ResultCode.BIZ_ERROR, "biz error");
        assertFalse(r.isSuccess());
        assertEquals(500, r.getCode());
    }
}
