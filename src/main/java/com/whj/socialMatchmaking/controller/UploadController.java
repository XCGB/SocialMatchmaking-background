package com.whj.socialMatchmaking.controller;

import com.whj.socialMatchmaking.common.BaseResponse;
import com.whj.socialMatchmaking.common.ResultUtils;
import com.whj.socialMatchmaking.utils.AliOSSUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @author: Baldwin
 * @createTime: 2023-07-13 19:28
 * @description: 上传文件控制器
 */
@Slf4j
@RestController
@RequestMapping("/upload")
public class UploadController {
    @Resource
    private AliOSSUtils aliOSSUtils;

    @PostMapping("/avatar")
    public BaseResponse<String> upload(MultipartFile image) throws IOException {
        log.info("文件上传，文件名{}",image.getOriginalFilename());
        String url = aliOSSUtils.upload(image);
        log.info("文件上传成功，文件访问URL：{}",url);
        return ResultUtils.success(url);
    }

}
