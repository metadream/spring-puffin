package com.arraywork.puffin.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.arraywork.puffin.entity.Metadata;
import com.arraywork.puffin.service.FfmpegService;
import com.arraywork.puffin.service.MetadataService;
import com.arraywork.puffin.service.PreferenceService;
import com.arraywork.springforce.StaticResourceHandler;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 资源控制器
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/04/28
 */
@Controller
@Slf4j
public class ResourceController {

    @Resource
    private StaticResourceHandler resourceHandler;
    @Resource
    private FfmpegService ffmpegService;
    @Resource
    private PreferenceService preferService;
    @Resource
    private MetadataService metadataService;

    @Value("${puffin.cover.base-dir}")
    private String coverBaseDir;

    // 封面资源
    @GetMapping("/cover/{id}")
    public void cover(HttpServletRequest request, HttpServletResponse response,
        @PathVariable String id) throws IOException {
        Path coverPath = Path.of(coverBaseDir, id + ".jpg");
        resourceHandler.serve(coverPath, request, response);
    }

    // 视频资源
    @GetMapping("/video/{id}")
    public void video(HttpServletRequest request, HttpServletResponse response,
        @PathVariable String id) throws IOException {
        String library = preferService.getPreference().getLibrary();
        Metadata metadata = metadataService.getById(id);
        Path videoPath = Path.of(library, metadata.getFilePath());
        resourceHandler.serve(videoPath, request, response);
    }

    // 视频转码
    @GetMapping("/video/{id}/{transId}")
    public void transcode(@PathVariable String id, @PathVariable String transId,
        HttpServletResponse response) {
        response.setContentType("video/mp4");

        try {
            Metadata metadata = metadataService.getById(id);
            InputStream input = ffmpegService.transcode(transId, metadata.getFilePath());
            OutputStream output = response.getOutputStream();
            resourceHandler.copy(input, output);
        } catch (Exception e) {
            log.error("Ignored: {}", e.getMessage());
            destroy(transId);
        }
    }

    // 终止转码进程
    @PostMapping("/video/{transId}")
    @ResponseBody
    public void destroy(@PathVariable String transId) {
        ffmpegService.destroy(transId);
    }

}