package net.xdclass.util;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 小滴课堂,愿景：让技术不再难学
 *
 * @Description
 * @Author 二当家小D
 * @Remark 有问题直接联系我，源码-笔记-技术交流群,官网 https://xdclass.net
 * @Version 1.0
 **/
@Slf4j
public class CommonUtil {





    /**
     * 通用分页查询并转换结果的方法
     *
     * @param pageResult 分页结果对象
     * @param dtoClass   要转换的目标 DTO 类型
     * @param <DO>       数据库实体类型
     * @param <DTO>      数据传输对象类型
     * @return 包含 total_record, total_page, current_data 的分页 map
     */
    public  static <DO, DTO> Map<String, Object> convertToPageMap(IPage<DO> pageResult, Class<DTO> dtoClass) {
        List<DTO> dtoList = SpringBeanUtil.copyProperties(pageResult.getRecords(), dtoClass);
        Map<String, Object> pageMap = new HashMap<>(3);
        pageMap.put("totalRecord", pageResult.getTotal());
        pageMap.put("totalPage", pageResult.getPages());
        pageMap.put("currentData", dtoList);
        return pageMap;
    }


    /**
     * 通用分页结果构建：自定义记录列表和完整的分页元数据
     *
     * @param pageResult 分页结果对象（用于读取元数据）
     * @param records    需要返回给前端的记录列表（通常是 DTO 列表）
     * @param <T>        记录元素类型
     * @return 包含 records, totalRecord, totalPage, currentPage, pageSize 的分页 map
     */
    public static <T> Map<String, Object> convertToPageMap(IPage<?> pageResult, List<T> records) {
        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("totalRecord", pageResult.getTotal());
        result.put("totalPage", pageResult.getPages());
        result.put("currentPage", pageResult.getCurrent());
        result.put("pageSize", pageResult.getSize());
        return result;
    }

    /**
     * 响应json数据给前端
     *
     * @param response
     * @param obj
     */
    public static void sendJsonMessage(HttpServletResponse response, Object obj) {

        response.setContentType("application/json; charset=utf-8");

        try (PrintWriter writer = response.getWriter()) {
            writer.print(JsonUtil.obj2Json(obj));
            response.flushBuffer();

        } catch (IOException e) {
            log.warn("响应json数据给前端异常:{}", e);
        }
    }

    /**
     * 根据文件名称获取文件后缀
     */
    public static String getFileSuffix(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }




    /**
     * 生成uuid
     *
     * @return
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "").substring(0, 32);
    }


    /**
     * 获取随机长度的串
     *
     * @param length
     * @return
     */
    private static final String ALL_CHAR_NUM = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    /**
     * 获取当前时间戳
     *
     * @return
     */
    public static long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }
    public static String generateStringNumRandom(int length) {
        //生成随机数字和字母,
        Random random = new Random();
        StringBuilder saltString = new StringBuilder(length);
        for (int i = 1; i <= length; ++i) {
            saltString.append(ALL_CHAR_NUM.charAt(random.nextInt(ALL_CHAR_NUM.length())));
        }
        return saltString.toString();
    }

    /**
     * 生成6位数字验证码
     */
    public static String generateNumberCode() {
        Random random = new Random();
        // 生成100000到999999之间的随机数，确保总是6位数且无前导零
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    /**
     * 邮箱正则
     */
    private static final Pattern MAIL_PATTERN = Pattern.compile("^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$");

    /**
     * 手机号正则，暂时未用
     */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");

    /**
     * @param email
     * @return
     */
    public static  boolean isEmail(String email) {
        if (null == email || "".equals(email)) {
            return false;
        }
        Matcher m = MAIL_PATTERN.matcher(email);
        return m.matches();
    }

    /**
     * 暂时未用
     * @param phone
     * @return
     */
    public static boolean isPhone(String phone) {
        if (null == phone || "".equals(phone)) {
            return false;
        }
        Matcher m = PHONE_PATTERN.matcher(phone);
        return m.matches();

    }


}