package com.bilibli.local.city.stat.picture;

import com.bilibli.local.city.stat.new_user.CityDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface PictureDAO {

    @Select("SELECT * from t_city_pool_${city_id} where id<#{offset} and online=1 order by id desc limit 10")
    List<PictureDO> list(@Param("city_id") int city, @Param("offset") int offset);

    @Select("SELECT * from t_city_pool_${city_id} where id<#{offset} and src_type=9 order by id desc limit 200")
    List<PictureDO> listBadCover(@Param("city_id") int city, @Param("offset") int offset);

    @Select("SELECT * from cover where id>#{offset}  order by id limit 200")
    List<PictureDO> getFromLocal(int offset);

    @Update("update cover set same_line_rate=#{same_line_rate} where id=#{id}")
    int update(@Param("id") int id, @Param("same_line_rate") double same_line_rate);

    @Update("update cover set same_line_rate=#{same_line_rate},color_rate=#{color_rate},size=#{size} where id=#{id}")
    int updateAll(@Param("id") int id, @Param("same_line_rate") double same_line_rate, @Param("color_rate") double color_rate, @Param("size") int size);

    @Update("update t_city_pool_${city_id} set online=0,status=2 where dynamic_id=#{dynamic_id}")
    int updateBadData(@Param("city_id") String city, @Param("dynamic_id") String dynamic_id);

    @Select("SELECT * from t_city_pool_1 where src_type=9 and id<#{id} order by id desc limit 100")
    List<PictureDO> getBadCover(@Param("id") int id);

    @Select("SELECT * from t_city_pool_1 where vertical_cover!='' order by id desc limit 1000")
    List<PictureDO> getVerticalVideo();

    @Select("SELECT * from t_city_pool_1 where id<#{id} and online=1 order by id desc limit 100")
    List<PictureDO> getCover(@Param("id") int id);

    @Select("SELECT id,city_name from t_city_info where status=0")
    List<CityDO> getCity();
}
