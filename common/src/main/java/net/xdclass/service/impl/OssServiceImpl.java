package net.xdclass.service.impl;

import com.aliyun.oss.HttpMethod;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.model.ObjectMetadata;
import lombok.extern.slf4j.Slf4j;
import net.xdclass.config.OssClientManager;
import net.xdclass.service.OssService;
import net.xdclass.util.FileUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

@Slf4j
@Service
public class OssServiceImpl implements OssService {
    /**
     * 预览URL过期时间，设置为1小时（毫秒）
     */
    private static final long PREVIEW_URL_EXPIRATION_MILLIS = 3600 * 1000L; // 1小时

    @Autowired
    private OssClientManager ossClientManager;

    /**
     * 上传安全文件到私有存储桶
     *
     * @param file   要上传的文件
     * @param folder 文件存放的目录
     * @return 上传后的文件访问URL
     */
    @Override
    public String uploadSecureFile(MultipartFile file, String folder) {
        return uploadToBucket(file, folder, ossClientManager.getSecureBucket());
    }

    /**
     * 上传公共文件到公共存储桶
     *
     * @param file   要上传的文件
     * @param folder 文件存放的目录
     * @return 上传后的文件访问URL
     */
    @Override
    public String uploadPublicFile(MultipartFile file, String folder) {
        return uploadToBucket(file, folder, ossClientManager.getPublicBucket());
    }


    /**
     * 将文件上传到指定的存储桶
     *
     * @param file   要上传的文件
     * @param folder 文件存放的目录
     * @param bucket 目标存储桶客户端
     * @return 上传后的文件访问URL
     */
    private String uploadToBucket(MultipartFile file, String folder, OssClientManager.BucketClient bucket) {
        validateBucket(bucket);
        try {
            String fileName = FileUtil.generateUniqueFileName(file.getOriginalFilename());
            String filePath = FileUtil.buildFilePath(folder, fileName);

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            try (InputStream inputStream = file.getInputStream()) {
                bucket.getClient().putObject(bucket.getBucketName(), filePath, inputStream, metadata);
            }

            String fileUrl = bucket.buildFileKey(filePath);
            log.info("文件上传成功，文件名：{}，URL：{}", fileName, fileUrl);
            return fileUrl;

        } catch (IOException e) {
            log.error("文件上传失败：{}", e.getMessage(), e);
            throw new RuntimeException("文件上传失败：" + e.getMessage());
        }
    }

    /**
     * 验证存储桶配置是否有效
     *
     * @param bucket 待验证的存储桶客户端
     * @throws IllegalStateException 当存储桶配置无效时抛出异常
     */
    private void validateBucket(OssClientManager.BucketClient bucket) {
        if (bucket == null) {
            throw new IllegalStateException("目标存储空间未配置，无法上传文件");
        }
        if (!bucket.hasBucket()) {
            throw new IllegalStateException("OSS Bucket未配置，无法上传文件");
        }
        if (!bucket.hasClient()) {
            throw new IllegalStateException("OSS Client未配置，无法上传文件");
        }
    }


    /**
     * 通过URL去解析bucket
     */
    private OssClientManager.BucketClient resolveBucket(String fileUrl) {
        return ossClientManager.resolveByUrl(fileUrl);
    }

    /**
     * 删除指定URL的文件
     * 1、判断bucket类型
     * 2、获取FileKey
     * 3、删除操作
     */
    @Override
    public boolean deleteFile(String fileUrl) {
        try {
            OssClientManager.BucketClient bucket = resolveBucket(fileUrl);
            String fileKey = extractFileKey(fileUrl);

            if(StringUtils.isBlank(fileKey)){
                log.warn("无法从URL中提取文件Key：{}", fileUrl);
                return false;
            }

            bucket.getClient().deleteObject(bucket.getBucketName(), fileKey);
            log.info("文件删除成功:fileKey{}",fileKey);
            return true;

        }catch (Exception e){
            log.error("删除文件失败：{}", e.getMessage(), e);
            return false;
        }

    }

    @Override
    public String extractFileKey(String fileUrl) {
        if(StringUtils.isBlank(fileUrl)){
            return null;
        }
        try {
            OssClientManager.BucketClient bucket = resolveBucket(fileUrl);
            if(bucket == null){
                return null;
            }
            String domainWithSlash = bucket.domainWithSlash();
            return fileUrl.startsWith(domainWithSlash)?fileUrl.substring(domainWithSlash.length()):null;
        }catch (Exception e){
            log.error("无法从URL中提取文件Key：{}", fileUrl);
            return null;
        }

    }

      /**
     * 生成文件预览URL
     * 对于公开存储桶的文件直接返回原URL，对于私有存储桶的文件生成带签名的临时预览URL
     *
     * @param fileUrl 原始文件URL
     * @return 预览URL字符串，如果生成失败则返回null
     */
    @Override
    public String generatePreviewUrl(String fileUrl) {

        try {
            OssClientManager.BucketClient bucket = resolveBucket(fileUrl);
            if(bucket == null){
                log.warn("无法解析URL对应的存储桶：{}", fileUrl);
                return null;
            }

            if(bucket.isPublicBucket()){
                return fileUrl;
            }


            String fileKey = extractFileKey(fileUrl);


            // 配置过期时间
            Date expiration = new Date(System.currentTimeMillis() + PREVIEW_URL_EXPIRATION_MILLIS);

            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket.getBucketName(), fileKey,
                    HttpMethod.GET);
            request.setExpiration(expiration);

            URL generatePresignedUrl = bucket.getClient().generatePresignedUrl(request);
            String presignedUrlString = generatePresignedUrl.toString();
            log.info("生成预览URL成功：{}", presignedUrlString);
            return presignedUrlString;

        }catch (Exception e){
            log.error("无法生成预览URL：{}", e.getMessage(), e);
            return null;
        }

    }

}
