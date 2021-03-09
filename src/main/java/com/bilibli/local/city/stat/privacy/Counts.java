package com.bilibli.local.city.stat.privacy;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Date;

public class Counts {

    /**
     * id
     */
    @JsonIgnore
    private Long id;

    /**
     * 1-单曲播放数 2-专辑播放数 3-单曲收藏数 4-专辑收藏数 5-标签播放数  6-up主关注数 7-up主播放数 8-投稿tag绑定数
     */
    private Byte type;

    /**
     * 数量
     */
    private Integer count;

    /**
     * 资源id
     */
    private Long rid;

    /**
     * 创建时间
     */
    @JsonIgnore
    private Date ctime;

    /**
     * 最后修改时间
     */
    @JsonIgnore
    private Date mtime;

    private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        return "Counts{" +
                "id=" + id +
                ", type=" + type +
                ", count=" + count +
                ", rid=" + rid +
                ", ctime=" + ctime +
                ", mtime=" + mtime +
                '}';
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Byte getType() {
        return type;
    }

    public void setType(Byte type) {
        this.type = type;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Long getRid() {
        return rid;
    }

    public void setRid(Long rid) {
        this.rid = rid;
    }

    public Date getCtime() {
        return ctime;
    }

    public void setCtime(Date ctime) {
        this.ctime = ctime;
    }

    public Date getMtime() {
        return mtime;
    }

    public void setMtime(Date mtime) {
        this.mtime = mtime;
    }
}
