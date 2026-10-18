package net.xdclass.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云OSS配置类
 * 用于配置阿里云对象存储服务的相关参数和创建OSS客户端管理器
 */
@Slf4j
@Configuration
@Data
public class OssConfig {

    /**
     * 安全端点地址（HTTPS）
     */
    @Value("${aliyun.oss.secure-endpoint}")
    private String secureEndpoint;

    /**
     * 访问密钥ID
     */
    @Value("${aliyun.oss.access-key-id}")
    private String accessKeyId;

    /**
     * 访问密钥Secret
     */
    @Value("${aliyun.oss.access-key-secret}")
    private String accessKeySecret;

    /**
     * 公开端点地址，如果未配置则使用安全端点作为默认值
     */
    @Value("${aliyun.oss.public-endpoint:${aliyun.oss.secure-endpoint}}")
    private String publicEndpoint;

    /**
     * 安全桶名称
     */
    @Value("${aliyun.oss.secure-bucket-name}")
    private String secureBucketName;

    /**
     * 安全域名
     */
    @Value("${aliyun.oss.secure-domain}")
    private String secureDomain;

    /**
     * 公共桶名称
     */
    @Value("${aliyun.oss.public-bucket-name}")
    private String publicBucketName;

    /**
     * 公共域名
     */
    @Value("${aliyun.oss.public-domain}")
    private String publicDomain;

    @Bean
    public OssClientManager ossClientManager() {
        // 创建OSS客户端
        OSS secureClient = new OSSClientBuilder().build(secureEndpoint, accessKeyId, accessKeySecret);
        OSS publicClient = new OSSClientBuilder().build(publicEndpoint, accessKeyId, accessKeySecret);

        //创建对应的私有buket客户端
        OssClientManager.BucketClient secureBucket = new OssClientManager.BucketClient(secureClient, secureBucketName, secureDomain, false);
        //创建对应的公共buket客户端
        OssClientManager.BucketClient publicBucket = new OssClientManager.BucketClient(publicClient, publicBucketName, publicDomain, true);

        return new OssClientManager(secureBucket, publicBucket);
    }


}
