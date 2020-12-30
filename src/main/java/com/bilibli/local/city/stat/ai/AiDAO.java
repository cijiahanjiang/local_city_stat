package com.bilibli.local.city.stat.ai;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AiDAO {

    @Select("SELECT * from t_city_pool_${city} where id>#{offset} and ctime>'2020-11-25' and online=1 order by id limit 400")
    List<DynDO> getData(@Param("city") int city, @Param("offset") int offset);

    @Select("select heat from t_city_heat where dynamic_id = #{dynamic_id}")
    int getHeat(@Param("dynamic_id") long dynamic_id);
}
