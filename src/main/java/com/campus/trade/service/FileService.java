// src/main/java/com/campus/trade/service/FileService.java (接口)
package com.campus.trade.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface FileService {

    /**
     * 保存上传的文件到指定的基础路径下
     *
     * @param file         上传的文件
     * @param basePath     文件存储的基础物理路径 (例如 D:/.../images/item/)
     * @param accessPrefix 文件访问的 URL 前缀 (例如 /images/item)
     * @return 文件保存后的相对访问路径 (例如 /images/item/xxxxx.jpg)
     * @throws IOException 保存失败
     */
    String saveFile(MultipartFile file, String basePath, String accessPrefix) throws IOException;

    // 可以添加删除文件的接口等...
    // boolean deleteFile(String relativePath);
}