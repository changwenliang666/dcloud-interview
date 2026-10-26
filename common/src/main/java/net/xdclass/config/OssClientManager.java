package net.xdclass.config;

import com.aliyun.oss.OSS;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.net.URL;

@Getter
@AllArgsConstructor
public class OssClientManager {

    //私有
    private final BucketClient secureBucket;

    //公共
    private final BucketClient publicBucket;


    /**
     * 智能路由方法
     */
    public BucketClient resolveByUrl(String fileUrl){

        //参数校验
        if(StringUtils.isBlank(fileUrl)){
            return null;
        }

        //匹配Bucket
        if(secureBucket.matchesDomain(fileUrl)){
            return secureBucket;
        }

        if(publicBucket.matchesDomain(fileUrl)){
            return publicBucket;
        }

        return null;
    }


    @Getter
    public static final class BucketClient{

        /**
         * OSS 客户端
         */
        private final OSS client;
        /**
         * 存储桶
         */
        private final String bucketName;
        /**
         * 域名
         */
        private final String domain;
        /**
         * 是否公共
         */
        private final boolean publicBucket;


        public BucketClient(OSS client, String bucketName, String domain, boolean publicBucket) {
            this.client = client;
            this.bucketName = bucketName;
            this.domain = domain;
            this.publicBucket = publicBucket;
        }

        public boolean hasBucket(){
            return bucketName!=null && !bucketName.isEmpty();
        }

        public boolean hasDomain(){
            return domain!=null && !domain.isEmpty();
        }

        public boolean hasClient(){
            return client!=null;
        }


        /**
         * 返回带斜杠的域名
         */
        public  String domainWithSlash(){
            if(hasDomain()){
                return domain.endsWith("/")?domain:domain+"/";
            }
            return "";
        }

        /**
         * 构建文件的fileKey
         */
        public String buildFileKey(String fileKey){
            return  domainWithSlash()+fileKey;
        }


        /**
         * 判断URL是否属于当前存储桶。
         * 同时识别配置域名，以及 SDK 多拼出来的 {bucket}.{域名}。
         */
        public boolean matchesDomain(String url){
            return extractObjectKey(url) != null;
        }

        /**
         * 从文件 URL 中取出对象键，并去掉签名参数。
         */
        public String extractObjectKey(String fileUrl) {
            if (StringUtils.isBlank(fileUrl) || !hasDomain()) {
                return null;
            }
            String normalizedUrl = stripQuery(fileUrl.trim());
            String domain = domainWithSlash();
            String objectKey = null;
            if (normalizedUrl.startsWith(domain)) {
                objectKey = normalizedUrl.substring(domain.length());
            } else {
                String prefixedDomain = bucketPrefixedDomain();
                if (prefixedDomain != null && normalizedUrl.startsWith(prefixedDomain)) {
                    objectKey = normalizedUrl.substring(prefixedDomain.length());
                } else {
                    objectKey = extractVirtualHostKey(normalizedUrl);
                }
            }
            return StringUtils.isBlank(objectKey) ? null : objectKey;
        }

        /**
         * 签名地址一律改写到配置的访问域名，避免主机名变成 {bucket}.{域名}。
         * V1 签名不包含 Host，替换后签名仍然有效。
         */
        public String toAccessUrl(URL presignedUrl) {
            if (presignedUrl == null) {
                return null;
            }
            if (!hasDomain()) {
                return presignedUrl.toString();
            }
            try {
                URI uri = presignedUrl.toURI();
                String base = normalizeDomain(domain);
                String rawPath = uri.getRawPath() == null ? "" : uri.getRawPath();
                if (!rawPath.startsWith("/")) {
                    rawPath = "/" + rawPath;
                }
                String rawQuery = uri.getRawQuery();
                if (StringUtils.isBlank(rawQuery)) {
                    return base + rawPath;
                }
                return base + rawPath + "?" + rawQuery;
            } catch (Exception e) {
                return presignedUrl.toString();
            }
        }

        /**
         * http://pri-file.mockmate.asia/ → http://mockmate-pri.pri-file.mockmate.asia/
         */
        private String bucketPrefixedDomain() {
            if (!hasBucket()) {
                return null;
            }
            String domain = domainWithSlash();
            int schemeEnd = domain.indexOf("://");
            if (schemeEnd < 0) {
                return bucketName + "." + domain;
            }
            int hostStart = schemeEnd + 3;
            return domain.substring(0, hostStart) + bucketName + "." + domain.substring(hostStart);
        }

        /**
         * 兼容 https://{bucket}.oss-cn-xxx.aliyuncs.com/key 这种虚拟主机地址。
         */
        private String extractVirtualHostKey(String url) {
            if (!hasBucket()) {
                return null;
            }
            int schemeEnd = url.indexOf("://");
            if (schemeEnd < 0) {
                return null;
            }
            int pathStart = url.indexOf('/', schemeEnd + 3);
            if (pathStart < 0 || pathStart == url.length() - 1) {
                return null;
            }
            String host = url.substring(schemeEnd + 3, pathStart);
            boolean sameBucket = host.regionMatches(true, 0, bucketName + ".", 0, bucketName.length() + 1);
            if (sameBucket && host.toLowerCase().endsWith(".aliyuncs.com")) {
                return url.substring(pathStart + 1);
            }
            return null;
        }

        private String stripQuery(String url) {
            int queryIndex = url.indexOf('?');
            if (queryIndex >= 0) {
                return url.substring(0, queryIndex);
            }
            return url;
        }


        /**
         * 统一域名格式,避免`https://public.xdclass.net/`这种带斜杠的情况
         */
        private static String normalizeDomain(String domain) {
            if (domain == null) {
                return "";
            }
            String value = domain.trim();
            // 移除末尾的所有斜杠
            while (value.endsWith("/")) {
                value = value.substring(0, value.length() - 1);
            }
            return value;
        }


    }

}
