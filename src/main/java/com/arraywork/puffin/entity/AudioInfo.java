package com.arraywork.puffin.entity;

import lombok.Data;

/**
 * 音频信息
 * @author AiChen
 * @created 2024/04/21
 */
@Data
public class AudioInfo {

    private String codec;
    private int channels;
    private int sampleRate;
    private int bitRate;

}