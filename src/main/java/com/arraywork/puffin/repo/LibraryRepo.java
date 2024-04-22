package com.arraywork.puffin.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.arraywork.puffin.entity.Library;

/**
 * 媒体库持久化
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/02/21
 */
public interface LibraryRepo extends JpaRepository<Library, Long> {}