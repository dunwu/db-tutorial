package io.github.dunwu.javadb.elasticsearch.constant;

import cn.hutool.core.util.StrUtil;

import java.util.stream.Stream;

/**
 * 系统级错误码
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @see <a href="https://httpstatuses.com/">HTTP 状态码</a>
 * @see <a href="http://wiki.open.qq.com/wiki/%E9%94%99%E8%AF%AF%E7%A0%81">腾讯开放平台错误码</a>
 * @see <a href="https://open.weibo.com/wiki/Error_code">新浪开放平台错误码</a>
 * @see <a href= "https://docs.open.alipay.com/api_1/alipay.trade.order.settle/">支付宝开放平台API</a>
 * @see <a href=
 * "https://open.weixin.qq.com/cgi-bin/showdocument?action=dir_list&t=resource/res_list&verify=1&id=open1419318634&token=&lang=zh_CN">微信开放平台错误码</a>
 * @since 2019-04-11
 */
public enum ResultCode implements CodeMsg {

    OK(0, "成功"),

    PART_OK(1, "部分成功"),

    FAIL(-1, "失败"),

    // -----------------------------------------------------
    // 系统级错误码
    // -----------------------------------------------------

    ERROR(1000, "服务器错误"),

    PARAM_ERROR(1001, "参数错误"),

    TASK_ERROR(1001, "调度任务错误"),

    CONFIG_ERROR(1003, "配置错误"),

    REQUEST_ERROR(1004, "请求错误"),

    IO_ERROR(1005, "IO 错误"),

    // -----------------------------------------------------
    // 2000 ~ 2999 数据库错误
    // -----------------------------------------------------

    DATA_ERROR(2000, "数据库错误"),

    // -----------------------------------------------------
    // 3000 ~ 3999 三方错误
    // -----------------------------------------------------

    THIRD_PART_ERROR(3000, "三方错误"),

    // -----------------------------------------------------
    // 3000 ~ 3999 认证错误
    // -----------------------------------------------------

    AUTH_ERROR(4000, "认证错误");

    private final int code;

    private final String msg;

    ResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMsg() {
        return msg;
    }

    public static String getNameByCode(int code) {
        return Stream.of(ResultCode.values()).filter(item -> item.getCode() == code).findFirst()
                     .map(ResultCode::getMsg).orElse(null);
    }

    public static ResultCode getEnumByCode(int code) {
        return Stream.of(ResultCode.values()).filter(item -> item.getCode() == code).findFirst().orElse(null);
    }

    public static String getTypeInfo() {
        StringBuilder sb = new StringBuilder();
        ResultCode[] types = ResultCode.values();
        for (ResultCode type : types) {
            sb.append(StrUtil.format("{}:{}, ", type.getCode(), type.getMsg()));
        }
        return sb.toString();
    }
}
