WITH city_info as (
  select
    id,
    city_name
  from
    b_ods.ods_db477_t_city_info_a_d
  where
    log_date = '<%=log_date%>'
)
insert
  overwrite TABLE bili_main.dwb_lc_av_a_a_d PARTITION(log_date = '<%=log_date%>')
SELECT
  a.rid avid,
  a.lng longitude,
  a.lat latitude,
  a.city_id city_id,
  c.city_name city_name,
  "" ip,
  "" buvid,
  a.ctime ctime,
  b.up_platform platform,
  a.uid mid
FROM
  b_ods.ods_db477_t_city_pool_a_d a
  join b_dwb.dwb_ctnt_arch_wide_a_d b on a.rid = b.avid
  join city_info c on a.city_id = c.id
where
  b.log_date = '<%=log_date%>'
  and a.log_date = '<%=log_date%>'
  and a.type=8