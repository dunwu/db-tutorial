package io.github.dunwu.javadb.elasticsearch.exception;

import io.github.dunwu.javadb.elasticsearch.constant.CodeMsg;
import io.github.dunwu.javadb.elasticsearch.constant.ResultCode;

/**
 * 默认异常
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @since 2021-12-30
 */
public class DefaultException extends CodeMsgException {

    private static final long serialVersionUID = -7027578114976830416L;

    public DefaultException() {
        this(ResultCode.FAIL);
    }

    public DefaultException(CodeMsg codeMsg) {
        this(codeMsg.getCode(), codeMsg.getMsg());
    }

    public DefaultException(CodeMsg codeMsg, String msg) {
        this(codeMsg.getCode(), msg, null);
    }

    public DefaultException(CodeMsg codeMsg, String msg, String toast) {
        this(codeMsg.getCode(), msg, toast);
    }

    public DefaultException(String msg) {
        this(ResultCode.FAIL, msg);
    }

    public DefaultException(int code, String msg) {
        this(code, msg, msg);
    }

    public DefaultException(int code, String msg, String toast) {
        super(code, msg, toast);
    }

    public DefaultException(Throwable cause) {
        this(cause, ResultCode.FAIL);
    }

    public DefaultException(Throwable cause, String msg) {
        this(cause, ResultCode.FAIL, msg);
    }

    public DefaultException(Throwable cause, CodeMsg codeMsg) {
        this(cause, codeMsg.getCode(), codeMsg.getMsg());
    }

    public DefaultException(Throwable cause, CodeMsg codeMsg, String msg) {
        this(cause, codeMsg.getCode(), msg, null);
    }

    public DefaultException(Throwable cause, CodeMsg codeMsg, String msg, String toast) {
        this(cause, codeMsg.getCode(), msg, toast);
    }

    public DefaultException(Throwable cause, int code, String msg) {
        this(cause, code, msg, null);
    }

    public DefaultException(Throwable cause, int code, String msg, String toast) {
        super(cause, code, msg, toast);
    }

}
