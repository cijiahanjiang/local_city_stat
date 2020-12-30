package com.bilibli.local.city.stat.privacy;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PrivacyDAO {

    @Select("select * from t_city_user_privacy where id>#{offset} order by id limit 100")
    List<PrivacyDO> getPrivacy(@Param("offset") int offset);

    @Select("select switch as is_open,recent_city_id city  from ${table} where uid = #{uid}")
    UserDO getUserDO(@Param("table") String table, @Param("uid") long uid);
}
