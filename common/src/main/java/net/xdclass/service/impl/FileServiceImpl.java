package net.xdclass.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.xdclass.service.FileService;
import net.xdclass.service.OssService;
import net.xdclass.util.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;


@Slf4j
@Service
public class FileServiceImpl implements FileService {

    private static final long MAX_FILE_SIZE = 1024 * 1024 * 20;

    private static final String DEFAULT_PUBLIC_FILE_FOLDER = "public/file";

    private static final String DEFAULT_SECURE_FILE_FOLDER = "secure/file/";


    @Autowired
    private OssService ossService;


    @Override
    public JsonData uploadPublicFile(MultipartFile file, String folder) {

        String fileUrl = uploadFileToPublicBucket(file, folder != null ? folder : DEFAULT_PUBLIC_FILE_FOLDER);
        return JsonData.buildSuccess(Map.of("url",fileUrl));
    }

    @Override
    public JsonData uploadPublicFile(MultipartFile file) {
        return uploadPublicFile(file, DEFAULT_PUBLIC_FILE_FOLDER);
    }

    @Override
    public JsonData uploadSecureFile(MultipartFile file, String folder) {
        String fileUrl = uploadFileToSecureBucket(file, folder != null ? folder : DEFAULT_SECURE_FILE_FOLDER);
        return JsonData.buildSuccess(Map.of("url",fileUrl));
    }

    @Override
    public JsonData uploadSecureFile(MultipartFile file) {
        return uploadSecureFile(file, DEFAULT_SECURE_FILE_FOLDER);
    }


    private String uploadFileToPublicBucket(MultipartFile file, String folder) {
        validateFile(file);
        String fileUrl = ossService.uploadPublicFile(file, folder);
        log.info("上传公开文件成功，文件地址：{}", fileUrl);
        return fileUrl;
    }

    private String uploadFileToSecureBucket(MultipartFile file, String folder) {
        validateFile(file);
        String fileUrl = ossService.uploadSecureFile(file, folder);
        log.info("上传私有文件成功，文件地址：{}", fileUrl);
        return fileUrl;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("文件过大");
        }
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new RuntimeException("文件名不能为空");
        }
    }

}
