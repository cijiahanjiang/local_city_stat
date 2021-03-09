package com.bilibli.local.city.stat.privacy;

import java.util.List;

/**
 * Created by user on 2017/6/15.
 */
public class CountRequest {


    @Override
    public String toString() {
        return "CountRequest{" +
                "list=" + list +
                '}';
    }

    public List<CountData> getList() {
        return list;
    }

    public void setList(List<CountData> list) {
        this.list = list;
    }

    List<CountData> list;

    public static class CountData {

        @Override
        public String toString() {
            return "CountData{" +
                    "type=" + type +
                    ", count=" + count +
                    ", rid=" + rid +
                    ", mid=" + mid +
                    '}';
        }

        /**
         * 上报类型 1、单曲播放
         */
        private Integer type;

        /**
         * 数量
         */
        private Integer count;

        /**
         * 资源id
         */
        private Long rid;

        /**
         * 收听歌曲用户id
         */
        private Long mid;


        public Integer getType() {
            return type;
        }

        public void setType(Integer type) {
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

        public Long getMid() {
            return mid;
        }

        public void setMid(Long mid) {
            this.mid = mid;
        }


    }
}
