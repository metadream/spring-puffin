package com.arraywork.puffin.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import com.arraywork.puffin.basedata.Censorship;
import com.arraywork.puffin.basedata.Quality;
import com.arraywork.puffin.basedata.Rating;
import com.arraywork.puffin.basedata.Region;
import com.arraywork.springhood.LongIdGenerator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.hypersistence.utils.hibernate.type.json.JsonStringType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 元数据
 * @author AiChen
 * @created 2024/04/21
 */
@Entity
@JsonIgnoreProperties(value = { "hibernateLazyInitializer", "handler" }) // 序列化时忽略懒加载的属性
@DynamicInsert // 如果字段值为null则不会加入到insert语句中（此处的作用是为了使初始化空实体对象时产生带默认值的空数据行）
@Data
public class Metadata {

    @Id
    @Column(length = 20, insertable = false, updatable = false)
    @GenericGenerator(name = "long-id-generator", type = LongIdGenerator.class)
    @GeneratedValue(generator = "long-id-generator")
    private long id;

    // 编号
    @Column(unique = true)
    @NotBlank(message = "编号不能为空")
    @Size(max = 20, message = "编号不能超过{max}个字符")
    private String code;

    // 标题
    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题不能超过 {max} 个字符")
    private String title;

    // 路径
    @NotBlank(message = "路径不能为空")
    @Size(max = 100, message = "路径不能超过 {max} 个字符")
    private String path;

    // 发行日期
    private LocalDate releaseDate;

    // private long videoSize;
    // private int videoWidth;
    // private int videoHeight;
    // private int duration;

    // 出品方
    @Type(JsonStringType.class)
    @Column(columnDefinition = "JSON DEFAULT (JSON_ARRAY())")
    private String[] producers;

    // 导演
    @Type(JsonStringType.class)
    @Column(columnDefinition = "JSON DEFAULT (JSON_ARRAY())")
    private String[] directors;

    // 主演
    @Type(JsonStringType.class)
    @Column(columnDefinition = "JSON DEFAULT (JSON_ARRAY())")
    private String[] starring;

    // 题材
    @Type(JsonStringType.class)
    @Column(columnDefinition = "JSON DEFAULT (JSON_ARRAY())")
    private String[] genres;

    // 地区
    @Type(JsonStringType.class)
    @Column(columnDefinition = "JSON DEFAULT (JSON_ARRAY())")
    private Region[] regions;

    // 画质
    @Enumerated(value = EnumType.STRING)
    private Quality quality;

    // 审查
    @Enumerated(value = EnumType.STRING)
    private Censorship censorship;

    // 分级
    @Enumerated(value = EnumType.STRING)
    private Rating rating;

    // 系列
    @Size(max = 50, message = "系列不能超过 {max} 个字符")
    private String series;

    // 封面地址
    @Size(max = 50, message = "封面地址不能超过 {max} 个字符")
    private String coverUrl;

    // 是否标星
    private boolean starred;

    // 媒体文件更新时间
    private LocalDateTime lastModified;

}