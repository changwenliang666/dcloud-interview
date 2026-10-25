package net.xdclass.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;

/**
 * 微信支付配置类
 * 负责配置绑定和证书路径处理
 * <p>
 * 证书路径处理说明：
 * 微信支付 SDK v3 不支持 classpath: 路径，因此需要将打包在 jar 内的证书文件
 * 复制到系统临时目录，获取绝对路径后供 SDK 使用。
 * <p>
 * 配置示例（application.yml）：
 * <pre>
 * wechat:
 *   pay:
 *     mch-id: 1234567890
 *     app-id: wx1234567890abcdef
 *     cert-serial-no: XXXXXXXX
 *     api-v3-key: XXXXXXXX
 *     private-key-path: classpath:/cert/apiclient_key.pem
 *     public-key-path: classpath:/cert/pub_key.pem
 *     public-key-id: PUB_KEY_ID_XXXX
 *     notify-url: https://example.com/callback
 * </pre>
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "wechat.pay")
@Slf4j
public class WechatPayConfig {

    /** 商户号 */
    private String mchId;

    /** 应用ID */
    private String appId;

    /** 商户API证书序列号 */
    private String certSerialNo;

    /** APIv3密钥 */
    private String apiV3Key;

    /** 商户API证书私钥路径（支持 classpath: 前缀） */
    private String privateKeyPath;

    /** 微信支付公钥路径（支持 classpath: 前缀） */
    private String publicKeyPath;

    /** 微信支付公钥ID */
    private String publicKeyId;

    /** 支付回调通知URL */
    private String notifyUrl;

    /** 私钥绝对路径（内部处理，供SDK使用） */
    private String privateKeyAbsolutePath;

    /** 公钥绝对路径（内部处理，供SDK使用） */
    private String publicKeyAbsolutePath;

    private final ResourceLoader resourceLoader;


    public WechatPayConfig(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /**
     * 初始化证书路径
     * 将 classpath: 路径转换为绝对路径
     */
    @PostConstruct
    public void init() throws Exception {
        this.privateKeyAbsolutePath = resolveCertPath(privateKeyPath, "wechat_private_key.pem");
        this.publicKeyAbsolutePath = resolveCertPath(publicKeyPath, "wechat_public_key.pem");
        log.info("微信支付配置初始化完成");
    }

    /**
     * 解析证书路径
     * 如果路径以 classpath: 开头，则将资源复制到临时目录并返回绝对路径
     * 否则直接返回原路径
     *
     * @param path         配置中的路径
     * @param tempFileName 临时文件名
     * @return 绝对路径
     */
    private String resolveCertPath(String path, String tempFileName) throws Exception {
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("证书路径不能为空");
        }

        // 处理 classpath: 路径
        if (path.startsWith("classpath:")) {
            Resource resource = resourceLoader.getResource(path);
            if (!resource.exists()) {
                throw new RuntimeException("证书文件不存在: " + path);
            }

            // 复制到系统临时目录
            File tempFile = new File(System.getProperty("java.io.tmpdir"), tempFileName);
            try (InputStream is = resource.getInputStream();
                 FileOutputStream fos = new FileOutputStream(tempFile)) {
                FileCopyUtils.copy(is, fos);
            }

            log.debug("证书文件已复制到临时目录: {}", tempFile.getAbsolutePath());
            return tempFile.getAbsolutePath();
        }

        // 非 classpath 路径直接返回
        return path;
    }
}