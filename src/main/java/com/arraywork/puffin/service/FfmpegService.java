package com.arraywork.puffin.service;

import java.io.File;

import org.springframework.stereotype.Service;

import com.arraywork.puffin.entity.AudioInfo;
import com.arraywork.puffin.entity.MediaInfo;
import com.arraywork.puffin.entity.VideoInfo;

import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.ScreenExtractor;
import ws.schild.jave.info.MultimediaInfo;
import ws.schild.jave.info.VideoSize;

/**
 * FFMPEG服务
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/04/22
 */
@Service
public class FfmpegService {

    // 获取媒体信息
    public MediaInfo getMediaInfo(File file) {
        MediaInfo mediaInfo = null;
        try {
            MultimediaInfo mInfo = new MultimediaObject(file).getInfo();

            // 获取媒体信息
            if (mInfo.getFormat() != null && mInfo.getDuration() > 0) {
                mediaInfo = new MediaInfo();
                mediaInfo.setDuration(mInfo.getDuration());
                mediaInfo.setFormat(mInfo.getFormat());

                // 获取音频信息
                ws.schild.jave.info.AudioInfo aInfo = mInfo.getAudio();
                if (aInfo != null) {
                    AudioInfo audio = new AudioInfo();
                    audio.setDecoder(aInfo.getDecoder().replaceAll(" \\(\\w+ / \\w+\\)", ""));
                    audio.setChannels(aInfo.getChannels());
                    audio.setBitRate(aInfo.getBitRate());
                    audio.setSamplingRate(aInfo.getSamplingRate());
                    mediaInfo.setAudio(audio);
                }

                // 获取视频信息
                ws.schild.jave.info.VideoInfo vInfo = mInfo.getVideo();
                if (vInfo != null) {
                    VideoInfo video = new VideoInfo();
                    video.setDecoder(vInfo.getDecoder().replaceAll(" \\(\\w+ / \\w+\\)", ""));
                    video.setBitRate(vInfo.getBitRate());
                    video.setFrameRate(vInfo.getFrameRate());

                    // 视频尺寸
                    VideoSize vSize = vInfo.getSize();
                    if (vSize != null) {
                        video.setWidth(vSize.getWidth());
                        video.setHeight(vSize.getHeight());
                    }
                    mediaInfo.setVideo(video);
                }
            }
        } catch (EncoderException e) {
            mediaInfo = null;
        }
        return mediaInfo;
    }

    // 视频截图
    public void screenshot(File videoFile, File outputFile, long millis) {
        MultimediaObject mObject = new MultimediaObject(videoFile);
        ScreenExtractor screenExtractor = new ScreenExtractor();
        try {
            screenExtractor.renderOneImage(mObject, -1, -1, millis, outputFile, 1);
        } catch (EncoderException e) {
            e.printStackTrace();
        }
    }

}