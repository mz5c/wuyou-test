package com.wuyou.common.result;

public enum ResultCode {
    SUCCESS(200, "success"),
    BAD_REQUEST(400, "bad request"),
    UNAUTHORIZED(401, "unauthorized"),
    FORBIDDEN(403, "forbidden"),
    NOT_FOUND(404, "not found"),
    BIZ_ERROR(500, "biz error"),
    PARAM_ERROR(1001, "parameter error"),
    DB_ERROR(1002, "database error"),
    REMOTE_CALL_ERROR(1003, "remote call error");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() { return code; }
    public String getMessage() { return message; }
}
