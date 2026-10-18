package net.xdclass.config;

import com.aliyun.oss.OSS;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

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
         * 判断URL是否以指定域名开头
         */
        public boolean matchesDomain(String url){
            return hasDomain() && url.startsWith(domainWithSlash());
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
