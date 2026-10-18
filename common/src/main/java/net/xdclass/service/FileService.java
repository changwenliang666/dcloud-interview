package net.xdclass.service;

import net.xdclass.util.JsonData;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传服务接口
 */
public interface FileService {

    /**
     * 上传文件到公开Bucket
     * @param file 文件
     * @param folder 文件夹路径(可选,如果为空则使用默认路径)
     * @return 文件URL
     */
    JsonData uploadPublicFile(MultipartFile file, String folder);

    /**
     * 上传文件到公开Bucket(使用默认文件夹)
     * @param file 文件
     * @return 文件URL
     */
    JsonData uploadPublicFile(MultipartFile file);

    /**
     * 上传文件到私有Bucket
     * @param file 文件
     * @param folder 文件夹路径(可选,如果为空则使用默认路径)
     * @return 文件URL
     */
    JsonData uploadSecureFile(MultipartFile file, String folder);

    /**
     * 上传文件到私有Bucket(使用默认文件夹)
     * @param file 文件
     * @return 文件URL
     */
    JsonData uploadSecureFile(MultipartFile file);

}
