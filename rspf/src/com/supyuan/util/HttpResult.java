package com.supyuan.util;

/**
 * http的返回结果
 * 
 * @author zhanzhan 2015年12月14日 下午7:23:57
 */
public class HttpResult {

    private int httpStatusCode;
    private String responseBody;

    public HttpResult(int httpStatusCode, String responseBody){
        this.httpStatusCode = httpStatusCode;
        this.responseBody = responseBody;
    }

    @Override
    public String toString() {
        return "HttpResult [httpStatusCode=" + httpStatusCode + ", responseBody=" + responseBody
               + "]";
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public void setHttpStatusCode(int httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

}

