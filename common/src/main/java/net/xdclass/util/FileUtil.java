package net.xdclass.util;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.UUID;

/**
 * 文件处理工具类
 */
public class FileUtil {

    /**
     * 验证上传文件
     */
    public static void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }

        // 检查文件大小（10MB）
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new RuntimeException("文件大小不能超过10MB");
        }

        // 检查文件类型
        String filename = file.getOriginalFilename();
        if (filename == null || !isValidFileType(filename)) {
            throw new RuntimeException("不支持的文件类型，仅支持PDF、DOC、DOCX格式");
        }
    }

    /**
     * 检查文件类型是否有效
     */
    public static boolean isValidFileType(String filename) {
        String extension = FileUtil.getFileExtension(filename).toLowerCase();
        return Arrays.asList("pdf", "doc", "docx").contains(extension);
    }

    /**
     * 获取文件扩展名 1.png
     */
    public static String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex == -1 ? "" : filename.substring(lastDotIndex + 1);
    }

    /**
     * 获取文件类型（大写）
     */
    public static String getFileType(String filename) {
        return getFileExtension(filename).toUpperCase();
    }

    /**
     * 生成唯一文件名
     * @param originalFilename 原始文件名
     * @return UUID + 文件扩展名
     */
    public static String generateUniqueFileName(String originalFilename) {
        if (originalFilename == null || originalFilename.isEmpty()) {
            return UUID.randomUUID().toString();
        }

        String extension = getFileExtension(originalFilename);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return uuid + "." + extension;
    }

    /**
     * 构建文件存储路径（按日期分级目录）
     * @param folder 文件夹名称（如 "resume"、"avatar" 等）
     * @param fileName 文件名
     * @return 完整的文件路径，格式：folder/yyyy/MM/dd/fileName
     */
    public static String buildFilePath(String folder, String fileName) {
        // 按日期分文件夹存储
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

        if (folder == null || folder.isEmpty()) {
            return datePath + "/" + fileName;
        }

        return folder + "/" + datePath + "/" + fileName;
    }

    /**
     * 从 PDF 文件提取文本
     */
    public static String extractTextFromPDF(String filePath) throws IOException {
        try (PDDocument document = Loader.loadPDF(new File(filePath))) {
            // 检查文档是否被加密
            if (document.isEncrypted()) {
                System.out.println("PDF已加密，无法直接提取文本（需解密）");
                return "";
            }

            // 创建文本提取器
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            System.out.println("提取的文本内容：\n" + text);
            return text;
        }
    }

    /**
     * 从 DOC 文件提取文本
     */
    public static String extractTextFromDOC(String filePath) throws IOException {
        try (HWPFDocument document = new HWPFDocument(Files.newInputStream(Paths.get(filePath)));
             WordExtractor extractor = new WordExtractor(document)) {
            return extractor.getText();
        }
    }

    /**
     * 从 DOCX 文件提取文本
     */
    public static String extractTextFromDOCX(String filePath) throws IOException {
        try (XWPFDocument document = new XWPFDocument(Files.newInputStream(Paths.get(filePath)));
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        }
    }
}