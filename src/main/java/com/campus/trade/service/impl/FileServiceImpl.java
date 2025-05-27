// src/main/java/com/campus/trade/service/impl/FileServiceImpl.java (实现)
package com.campus.trade.service.impl;

import com.campus.trade.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service
public class FileServiceImpl implements FileService {

    @Override
    public String saveFile(MultipartFile file, String basePath, String accessPrefix) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        if (!StringUtils.hasText(basePath)) {
             throw new IllegalArgumentException("文件基础存储路径不能为空");
        }
         if (!StringUtils.hasText(accessPrefix)) {
             throw new IllegalArgumentException("文件访问前缀不能为空");
        }


        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (StringUtils.hasText(originalFilename) && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // 【健壮性】校验文件后缀名是否允许 (可选)
        // if (!isValidExtension(fileExtension)) { throw new IOException("不支持的文件类型"); }

        // 生成唯一文件名
        String uniqueFileName = UUID.randomUUID().toString().replace("-", "") + fileExtension;

        // 确保上传目录存在
        Path uploadPath = Paths.get(basePath);
        if (!Files.exists(uploadPath)) {
            try {
                Files.createDirectories(uploadPath); // 创建目录，包括父目录
                log.info("创建目录: {}", uploadPath);
            } catch (IOException e) {
                 log.error("创建目录失败: {}", uploadPath, e);
                 throw new IOException("创建存储目录失败", e);
            }
        }

        // 计算文件的完整物理路径
        Path filePath = uploadPath.resolve(uniqueFileName);

        // 使用 try-with-resources 确保流关闭
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            log.info("文件成功保存到: {}", filePath);
        } catch (IOException e){
            log.error("保存文件失败: {}", filePath, e);
            // 可以在这里尝试删除未完全写入的文件
            // Files.deleteIfExists(filePath);
            throw new IOException("保存文件失败", e);
        }

        // 返回相对访问路径
        return accessPrefix + "/" + uniqueFileName; // 例如 /images/item/xxxxx.jpg 或 /images/avatar/yyyyy.png
    }

    // (可选) 校验文件扩展名
    // private boolean isValidExtension(String extension) {
    //     if (!StringUtils.hasText(extension)) return false;
    //     String lowerExt = extension.toLowerCase();
    //     // 允许常见的图片格式
    //     return lowerExt.equals(".jpg") || lowerExt.equals(".jpeg") || lowerExt.equals(".png") || lowerExt.equals(".gif");
    // }

    // (可选) 实现删除文件的逻辑
    // @Override
    // public boolean deleteFile(String relativePath) {
    //     if (!StringUtils.hasText(relativePath)) return false;
    //     try {
    //         // 需要根据 relativePath 和 basePath/accessPrefix 反推出物理路径
    //         // ... 实现推导逻辑 ...
    //         Path physicalPath = ...;
    //         return Files.deleteIfExists(physicalPath);
    //     } catch (IOException e) {
    //          log.error("删除文件失败: {}", relativePath, e);
    //          return false;
    //     }
    // }
}