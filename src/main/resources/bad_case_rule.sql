select * from cover1 where color_rate<0.003 and same_line_rate>0;

select * from cover where same_line_rate>0.3;

select *
from cover
where bad_case = 1
  and id not in (select id
                 from cover
                 where (same_line_rate > 0.3)
                    or (color_rate < 0.003 and same_line_rate > 0)
                    or (cover_url like '%.gif%')
                    or (colors < 300 and same_line_rate > 0)
                    or (colors < 300 and size < 100000));