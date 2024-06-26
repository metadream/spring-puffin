package com.arraywork.puffin.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.arraywork.puffin.entity.Preference;
import com.arraywork.springforce.filewatch.DirectoryWatcher;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 * 媒体库服务
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/04/22
 */
@Service
@Slf4j
public class LibraryService {

    private DirectoryWatcher watcher;
    @Resource
    private PreferenceService prefsService;
    @Resource
    private MetadataService metadataService;

    @Autowired
    public LibraryService(LibraryListener listener) {
        watcher = new DirectoryWatcher(10, 5, listener);
    }

    // 随应用启动目录监听
    @PostConstruct
    public void scan() {
        Preference prefs = prefsService.getPreference();
        if (prefs != null) {
            String library = prefs.getLibrary();
            scan(library, false);
        }
    }

    // 异步启动目录监听
    public void scan(String library, boolean emitOnStart) {
        log.info("启动媒体库监听: {}", library);
        watcher.start(library, emitOnStart);
    }

    // 重新扫描媒体库
    public void rescan() {
        String library = prefsService.getPreference().getLibrary();
        metadataService.purge(library);
        scan(library, true);
    }

    // 应用销毁时停止监听进程
    @PreDestroy
    public void onDestroyed() {
        watcher.stop();
    }

}