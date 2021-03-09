package com.bilibli.local.city.stat.privacy;

/**
 * Created by user on 2017/6/1.
 */
public enum CountType {


// 统计
// 1-单曲播放数 2-专辑播放数 3-单曲收藏数 4-专辑收藏数 5-标签播放数  6-up主关注数 7-up主播放数 8-投稿tag绑定数
// 9-用户播放数　10-单曲投币数 11-用户评论数

    SONG_PLAY_NUM((byte) 1, "单曲播放数"),
    ALBUM_PLAY_NUM((byte) 2, "专辑播放数"),
    SONG_COLLECT_NUM((byte) 3, "单曲收藏数"),
    ALBUM_COLLECT_NUM((byte) 4, "专辑收藏数"),
    TAG_PLAY_NUM((byte) 5, "标签播放数"),
    UP_WATCH_NUM((byte) 6, "up主关注数"),
    UP_PLAY_NUM((byte) 7, "up主播放数"),
    TAG_BIND_NUM((byte) 8, "投稿tag绑定数"),
    USER_PLAY_NUM((byte) 9, "用户播放数"),
    SONG_COIN_NUM((byte) 10, "单曲投币数"),
    SONG_COMMENT_NUM((byte) 11, "用户评论数"),
    SONG_SHARE_NUM((byte) 12, "单曲分享数"),
    MENU_PLAY_NUM((byte) 13, "歌单播放数"),
    MENU_COLLECT_NUM((byte) 14, "歌单收藏数"),
    MENU_SHARE_NUM((byte) 15, "歌单分享数"),
    SUBMIT_PAGE_STAT((byte)16,"创作中心统计");


    private Byte type;

    private String desc;

    public String getDesc() {
        return desc;
    }

    public Byte getType() {
        return type;
    }

    public void setType(Byte type) {
        this.type = type;
    }
    CountType(Byte type, String desc) {
        this.type = type;
        this.desc = desc;
    }


    public void setDesc(String desc) {
        this.desc = desc;
    }
}
