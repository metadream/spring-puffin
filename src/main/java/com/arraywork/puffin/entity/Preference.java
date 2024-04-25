package com.arraywork.puffin.entity;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.UpdateTimestamp;

import com.arraywork.puffin.metafield.Metafield;
import com.arraywork.puffin.metafield.MetafieldConverter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 偏好实体
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/04/21
 */
@Entity
@DynamicInsert
@Data
public class Preference {

    @Id
    private long id = Long.MAX_VALUE;

    // 用户名
    @Column(unique = true, updatable = false)
    @NotBlank(message = "用户名不能为空")
    @Size(max = 20, message = "用户名不能超过 {max} 个字符")
    private String username;

    // 密码
    @NotBlank(message = "密码不能为空")
    @Size(max = 60, message = "密码不能超过 {max} 个字符")
    private String password;

    // 媒体库路径
    @NotBlank(message = "媒体库路径不能为空")
    @Size(max = 120, message = "媒体库路径不能超过 {max} 个字符")
    private String library;

    // 元字段
    @JsonDeserialize(converter = MetafieldConverter.class)
    @Convert(converter = MetafieldConverter.class)
    @Column(columnDefinition = "JSON DEFAULT (JSON_ARRAY())")
    private List<Metafield> metafields;

    // 同步重命名文件
    private boolean syncRenameFile;

    // 最后更新时间
    @UpdateTimestamp
    private LocalDateTime lastModified;

}