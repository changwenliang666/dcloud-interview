package net.xdclass.util;

import jakarta.servlet.http.HttpServletRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class WechatUtil {

    /**
     * 读取请求体内容
     */
    public static String readRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    /**
     * 解析XML数据
     * 这里简化处理，实际项目中建议使用专门的XML解析库
     */
    public static Map<String, String> parseXmlData(String xmlData) {
        // 简化的XML解析，实际项目中建议使用DOM4J或JAXB
        Map<String, String> result = new HashMap<>();

        // 提取关键字段
        result.put("ToUserName", extractXmlValue(xmlData, "ToUserName"));
        result.put("FromUserName", extractXmlValue(xmlData, "FromUserName"));
        result.put("CreateTime", extractXmlValue(xmlData, "CreateTime"));
        result.put("MsgType", extractXmlValue(xmlData, "MsgType"));
        result.put("Event", extractXmlValue(xmlData, "Event"));
        result.put("EventKey", extractXmlValue(xmlData, "EventKey"));
        result.put("Ticket", extractXmlValue(xmlData, "Ticket"));

        return result;
    }

    /**
     * 从XML中提取指定标签的值，支持CDATA
     */
    public static String extractXmlValue(String xml, String tagName) {
        String startTag = "<" + tagName + ">";
        String endTag = "</" + tagName + ">";

        int startIndex = xml.indexOf(startTag);
        if (startIndex == -1) {
            return null;
        }

        startIndex += startTag.length();
        int endIndex = xml.indexOf(endTag, startIndex);
        if (endIndex == -1) {
            return null;
        }

        String value = xml.substring(startIndex, endIndex);

        // 处理CDATA标签
        if (value.startsWith("<![CDATA[") && value.endsWith("]]>")) {
            value = value.substring(9, value.length() - 3);
        }

        return value;
    }
}