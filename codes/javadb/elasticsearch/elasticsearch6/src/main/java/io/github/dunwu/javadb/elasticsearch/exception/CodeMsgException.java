package io.github.dunwu.javadb.elasticsearch.exception;

import cn.hutool.core.util.StrUtil;
import io.github.dunwu.javadb.elasticsearch.constant.CodeMsg;
import io.github.dunwu.javadb.elasticsearch.constant.ResultCode;

/**
 * 基础异常
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @since 2021-09-25
 */
public class CodeMsgException extends RuntimeException implements CodeMsg {

    private static final long serialVersionUID = 6146660782281445735L;

    /**
     * 状态码
     */
    protected int code;

    /**
     * 响应信息
     */
    protected String msg;

    /**
     * 提示信息
     */
    protected String toast;

    public CodeMsgException() {
        this(ResultCode.FAIL);
    }

    public CodeMsgException(CodeMsg codeMsg) {
        this(codeMsg.getCode(), codeMsg.getMsg());
    }

    public CodeMsgException(CodeMsg codeMsg, String msg) {
        this(codeMsg.getCode(), msg, null);
    }

    public CodeMsgException(CodeMsg codeMsg, String msg, String toast) {
        this(codeMsg.getCode(), msg, toast);
    }

    public CodeMsgException(String msg) {
        this(ResultCode.FAIL, msg);
    }

    public CodeMsgException(int code, String msg) {
        this(code, msg, msg);
    }

    public CodeMsgException(int code, String msg, String toast) {
        super(msg);
        setCode(code);
        setMsg(msg);
        setToast(toast);
    }

    public CodeMsgException(Throwable cause) {
        this(cause, ResultCode.FAIL);
    }

    public CodeMsgException(Throwable cause, String msg) {
        this(cause, ResultCode.FAIL, msg);
    }

    public CodeMsgException(Throwable cause, CodeMsg codeMsg) {
        this(cause, codeMsg.getCode(), codeMsg.getMsg());
    }

    public CodeMsgException(Throwable cause, CodeMsg codeMsg, String msg) {
        this(cause, codeMsg.getCode(), msg, null);
    }

    public CodeMsgException(Throwable cause, CodeMsg codeMsg, String msg, String toast) {
        this(cause, codeMsg.getCode(), msg, toast);
    }

    public CodeMsgException(Throwable cause, int code, String msg) {
        this(cause, code, msg, null);
    }

    public CodeMsgException(Throwable cause, int code, String msg, String toast) {
        super(msg, cause);
        setCode(code);
        setMsg(msg);
        setToast(toast);
    }

    @Override
    public String getMessage() {
        if (StrUtil.isNotBlank(msg)) {
            return StrUtil.format("[{}]{}", code, msg);
        }
        return super.getMessage();
    }

    @Override
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getToast() {
        return toast;
    }

    public void setToast(String toast) {
        this.toast = toast;
    }

}
