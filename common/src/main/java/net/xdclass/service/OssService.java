package net.xdclass.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 阿里云OSS文件上传服务接口
 */
public interface OssService {

    /**
     * 上传文件到加密访问的OSS Bucket
     * @param file 上传的文件
     * @param folder 文件夹路径（可选）
     * @return 文件访问URL
     */
    String uploadSecureFile(MultipartFile file, String folder);

    /**
     * 上传文件到公开可访问的Bucket
     * @param file 上传的文件
     * @param folder 文件夹路径
     * @return 公开访问URL
     */
    String uploadPublicFile(MultipartFile file, String folder);


    /**
     * 删除OSS文件
     * @param fileUrl 文件URL
     * @return 是否删除成功
     */
    boolean deleteFile(String fileUrl);

    /**
     * 从文件URL中提取文件key
     * @param fileUrl 文件URL
     * @return 文件key
     */
    String extractFileKey(String fileUrl);

    /**
     * 生成文件预览URL（带签名，支持直接访问）
     * @param fileUrl 文件URL
     * @return 预览URL
     */
    String generatePreviewUrl(String fileUrl);
}