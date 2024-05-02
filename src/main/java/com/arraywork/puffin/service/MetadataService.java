package com.arraywork.puffin.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.arraywork.puffin.entity.MediaInfo;
import com.arraywork.puffin.entity.Metadata;
import com.arraywork.puffin.entity.VideoInfo;
import com.arraywork.puffin.enums.Quality;
import com.arraywork.puffin.repo.MetadataRepo;
import com.arraywork.puffin.repo.MetadataSpec;
import com.arraywork.springforce.util.Assert;
import com.arraywork.springforce.util.Files;
import com.arraywork.springforce.util.KeyGenerator;
import com.arraywork.springforce.util.Pagination;
import com.arraywork.springforce.util.Times;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 * 元数据服务
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/04/22
 */
@Service
@Slf4j
public class MetadataService {

    @Resource
    private FfmpegService ffmpegService;
    @Resource
    @Lazy
    private PreferenceService prefsService;
    @Resource
    private MetadataRepo metadataRepo;

    @Value("${puffin.page-size}")
    private int pageSize;

    @Value("${puffin.cover.base-dir}")
    private String coverBaseDir;

    // 查询分页元数据
    public Pagination<Metadata> getMetadatas(String page, Metadata condition) {
        Sort sort = Sort.by("lastModified").descending().and(Sort.by("code").descending());
        page = page != null && page.matches("\\d+") ? page : "1";
        Pageable pageable = PageRequest.of(Integer.parseInt(page) - 1, pageSize, sort);
        Page<Metadata> pageInfo = metadataRepo.findAll(new MetadataSpec(condition), pageable);
        return new Pagination<Metadata>(pageInfo);
    }

    // 根据ID获取元数据
    public Metadata getById(String id) {
        Optional<Metadata> optional = metadataRepo.findById(id);
        Assert.isTrue(optional.isPresent(), HttpStatus.NOT_FOUND);
        return optional.get();
    }

    // 根据编号获取元数据
    public Metadata getByCode(String code) {
        Metadata metadata = metadataRepo.findByCode(code.toUpperCase());
        Assert.notNull(metadata, HttpStatus.NOT_FOUND);
        return metadata;
    }

    // 根据文件构建元数据
    @Transactional(rollbackFor = Exception.class)
    public Metadata build(File file, boolean rebuildCover) {
        Metadata metadata = metadataRepo.findByFilePath(file.getPath());
        if (metadata != null && !rebuildCover) return null;
        MediaInfo mediaInfo = ffmpegService.extract(file);
        if (mediaInfo == null || mediaInfo.getVideo() == null) return null;

        if (metadata == null) {
            metadata = new Metadata();
            VideoInfo video = mediaInfo.getVideo();
            metadata.setMediaInfo(mediaInfo);
            metadata.setQuality(adaptQuality(video.getWidth(), video.getHeight()));
            metadata.setTitle(Files.getName(file.getName()));
            metadata.setFilePath(file.getPath());
            metadata.setFileSize(file.length());
            metadata.setLastModified(Times.toLocal(file.lastModified()));
            metadata.setCode(KeyGenerator.nanoId(9, "0123456789"));
            metadataRepo.save(metadata); // 先保存以便截图获取ID
        }

        // 视频截图（已存在则更新）
        File coverFile = getCoverPath(metadata.getId()).toFile();
        ffmpegService.screenshot(file, coverFile, mediaInfo.getDuration() / 2);
        return metadata;
    }

    // 根据文件删除元数据
    @Transactional(rollbackFor = Exception.class)
    public Metadata delete(File file) {
        Metadata metadata = metadataRepo.findByFilePath(file.getPath());
        return delete(metadata);
    }

    // 保存元数据
    @Transactional(rollbackFor = Exception.class)
    public Metadata save(Metadata metadata) {
        String code = metadata.getCode().toUpperCase();
        Metadata _metadata = metadataRepo.findByCode(code);
        Assert.isTrue(_metadata == null || _metadata.getId().equals(metadata.getId()), "元数据编号冲突");

        if (_metadata == null) {
            _metadata = metadataRepo.getReferenceById(metadata.getId());
        }
        metadata.setCode(code);
        metadata.setFilePath(_metadata.getFilePath());
        metadata.setFileSize(_metadata.getFileSize());
        metadata.setLastModified(_metadata.getLastModified());
        metadata.setMediaInfo(_metadata.getMediaInfo());

        // 如果未手动设置画质则采用自动匹配的画质
        if (metadata.getQuality() == null) {
            metadata.setQuality(_metadata.getQuality());
        }
        // 重命名文件
        if (prefsService.getPreference().isAutoRename()) {
            String filePath = metadata.getFilePath();
            String extension = Files.getExtension(filePath);
            File oldFile = new File(filePath);
            File newFile = Path.of(oldFile.getParent(), "[" + code + "] " + metadata.getTitle() + extension).toFile();
            oldFile.renameTo(newFile);
            metadata.setFilePath(newFile.getPath());
        }
        return metadataRepo.save(metadata);
    }

    // 上传封面图片
    public String upload(String id, MultipartFile multipartFile) throws IOException {
        Metadata metadata = getById(id);
        Path coverPath = Path.of(coverBaseDir, metadata.getId() + ".jpg");
        multipartFile.transferTo(coverPath);
        return coverPath.toString();
    }

    // 清理元数据
    @Transactional(rollbackFor = Exception.class)
    public int purge(String library) {
        List<Metadata> metadatas = metadataRepo.findAll();
        List<Metadata> toDelete = new ArrayList<>();

        for (Metadata metadata : metadatas) {
            // 如果原始文件不存在、或者不是媒体库下的文件则删除
            File file = new File(metadata.getFilePath());
            if (!file.exists() || !file.getParent().equals(library)) {
                toDelete.add(metadata);
            }
        }
        toDelete.forEach(v -> delete(v));
        log.info("共清理 {} 条元数据", toDelete.size());
        return toDelete.size();
    }

    // 删除元数据
    @Transactional(rollbackFor = Exception.class)
    private Metadata delete(Metadata metadata) {
        if (metadata != null) {
            metadataRepo.delete(metadata); // 删除元数据
            File coverFile = getCoverPath(metadata.getId()).toFile();
            if (coverFile.exists()) coverFile.delete(); // 删除封面图片
        }
        return metadata;
    }

    // 根据ID获取封面路径
    private Path getCoverPath(String id) {
        return Path.of(coverBaseDir, id + ".jpg");
    }

    // 根据分辨率适配画质
    private Quality adaptQuality(int width, int height) {
        if (height >= width) return Quality.XX;
        if (height > 4000) return Quality.EK;   // 8192×4320
        if (height > 2000) return Quality.FK;   // 4096×2160
        if (height > 1000) return Quality.FHD;  // 1920×1080
        if (height > 700) return Quality.HD;    // 1280×720
        if (height > 400) return Quality.SD;    // 640×480
        return Quality.XX;
    }

}