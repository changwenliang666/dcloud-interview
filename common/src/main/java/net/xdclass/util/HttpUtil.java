package net.xdclass.util;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * HTTP 工具类，基于 Hutool 封装
 */
@Slf4j
public class HttpUtil {

    /**
     * 发送 GET 请求
     *
     * @param url     请求地址
     * @param headers 请求头
     * @param params  请求参数
     * @return 响应内容
     */
    public static String get(String url, Map<String, String> headers, Map<String, Object> params) {
        HttpRequest request = HttpRequest.get(url).addHeaders(headers).form(params);
        try (HttpResponse response = request.execute()) {
            if (response.isOk()) {
                return response.body();
            }
            logFailure("HTTP GET", url, response);
        } catch (Exception e) {
            log.error("[HTTP GET 异常] url: {}", url, e);
        }
        return null;
    }

    /**
     * 发送 POST 请求 (JSON)
     *
     * @param url     请求地址
     * @param headers 请求头
     * @param body    请求体对象
     * @return 响应内容
     */
    public static String post(String url, Map<String, String> headers, Object body) {
        String jsonBody = body instanceof String ? (String) body : JSONUtil.toJsonStr(body);
        HttpRequest request = HttpRequest.post(url).addHeaders(headers).body(jsonBody);
        try (HttpResponse response = request.execute()) {
            if (response.isOk()) {
                return response.body();
            }
            logFailure("HTTP POST", url, response);
        } catch (Exception e) {
            log.error("[HTTP POST 异常] url: {}", url, e);
        }
        return null;
    }

    /**
     * 发送 POST 请求 (表单)
     *
     * @param url     请求地址
     * @param headers 请求头
     * @param params  表单参数
     * @return 响应内容
     */
    public static String postForm(String url, Map<String, String> headers, Map<String, Object> params) {
        HttpRequest request = HttpRequest.post(url).addHeaders(headers).form(params);
        try (HttpResponse response = request.execute()) {
            if (response.isOk()) {
                return response.body();
            }
            logFailure("HTTP POST FORM", url, response);
        } catch (Exception e) {
            log.error("[HTTP POST FORM 异常] url: {}", url, e);
        }
        return null;
    }

    /**
     * 4xx/5xx 且响应体为空时，Hutool 会把 body 写成 "Error request, response status: xxx"。
     * 阿里云网关的真实原因在 X-Ca-Error-Message，这里只记录响应头。
     */
    private static void logFailure(String action, String url, HttpResponse response) {
        log.error("[{} 失败] url: {}, status: {}, gateway: {}", action, url, response.getStatus(), response.header("X-Ca-Error-Message"));
    }
} 