-- =============================================================
-- 电影票务系统 CD-TICKET 初始数据（幂等：先清空再插入）
-- 与 table.sql 配套执行：mysql -uroot -p cd_ticket < script/sql/seed.sql
-- 密码哈希 $2a$10$... 对应明文均为 Aa123456（BCrypt）
-- =============================================================

TRUNCATE TABLE `t_admin`;
TRUNCATE TABLE `t_cinema`;
TRUNCATE TABLE `t_movie`;
TRUNCATE TABLE `t_banner`;
TRUNCATE TABLE `t_hall`;
TRUNCATE TABLE `t_seat_config`;
TRUNCATE TABLE `t_screening`;

-- 管理员（cinema_id=0 表示不关联特定影院；明文密码 Aa123456）
INSERT INTO `t_admin` (`username`, `password`, `role`, `cinema_id`, `status`) VALUES
    ('admin', '$2a$10$SwwMs7T7E2rWloWEZP9ABeANabe992RETeA4.0HOfr2AqszhwSkiu', 0, 0, 1),
    ('cinema01', '$2a$10$SwwMs7T7E2rWloWEZP9ABeANabe992RETeA4.0HOfr2AqszhwSkiu', 1, 1, 1),
    ('cinema02', '$2a$10$SwwMs7T7E2rWloWEZP9ABeANabe992RETeA4.0HOfr2AqszhwSkiu', 1, 2, 1);

-- 影院
INSERT INTO `t_cinema` (`name`, `address`, `status`) VALUES
    ('CGV影城（成都SKP店）', '四川省成都市高新区天府大道中段1388号成都SKP购物中心7层', 1),
    ('万达影城（金牛万达店）', '四川省成都市金牛区人民北路二段188号金牛万达广场4层', 1);

-- 电影（status 0-下架 1-上架；热映/待映由 release_date 与当前时间比对得出）
-- 上映日期相对今天：3 部热映 + 1 部待映（预售）+ 1 部下架（演示管理端状态过滤）
INSERT INTO `t_movie` (`title`, `poster`, `description`, `duration`, `release_date`, `status`) VALUES
    ('星际穿越', 'https://picsum.photos/seed/m1/300/420', '人类接近灭亡，前宇航员穿越虫洞为人类寻找新家园。', 169, DATE_SUB(CURDATE(), INTERVAL 5 DAY), 1),
    ('流浪地球3', 'https://picsum.photos/seed/m2/300/420', '太阳危机临近，人类再次推动地球踏上流浪之旅。', 173, DATE_SUB(CURDATE(), INTERVAL 2 DAY), 1),
    ('哪吒之魔童闹海', 'https://picsum.photos/seed/m3/300/420', '哪吒与敖丙携手对抗四海龙族的围困。', 110, DATE_SUB(CURDATE(), INTERVAL 10 DAY), 1),
    ('深海寻秘', 'https://picsum.photos/seed/m4/300/420', '一支探险队深入马里亚纳海沟，揭开深海文明的秘密。', 112, DATE_ADD(CURDATE(), INTERVAL 20 DAY), 1),
    ('功夫熊猫5', 'https://picsum.photos/seed/m5/300/420', '阿宝再次出山，守护和平谷的宁静。', 100, DATE_SUB(CURDATE(), INTERVAL 30 DAY), 0);

-- 轮播图（status 0-禁用 1-启用）
INSERT INTO `t_banner` (`image`, `link_url`, `sort`, `status`) VALUES
    ('https://picsum.photos/seed/b1/1200/500', '/movies/1', 1, 1),
    ('https://picsum.photos/seed/b2/1200/500', '/movies/2', 2, 1),
    ('https://picsum.photos/seed/b3/1200/500', '/movies/3', 3, 0);

-- 影厅
INSERT INTO `t_hall` (`cinema_id`, `name`, `seat_rows`, `seat_cols`, `status`) VALUES
    (1, '1号激光厅', 4, 5, 1),
    (1, '2号杜比厅', 5, 6, 1),
    (1, '3号IMAX厅', 6, 8, 1),
    (2, '1号厅', 4, 5, 1);

-- 座位模板：按各影厅行列数一次性生成（status 1-启用；1号激光厅第1排1/5座演示禁用）
INSERT INTO `t_seat_config` (`hall_id`, `seat_row`, `seat_col`, `seat_no`, `status`)
WITH RECURSIVE `seq` AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM `seq` WHERE n < 8
)
SELECT h.id, r.n, c.n, CONCAT(r.n, '排', c.n, '座'),
       CASE WHEN h.id = 1 AND r.n = 1 AND c.n IN (1, 5) THEN 0 ELSE 1 END
FROM `t_hall` h
CROSS JOIN `seq` r
CROSS JOIN `seq` c
WHERE r.n <= h.seat_rows AND c.n <= h.seat_cols;

-- 排场（时间相对今天，保证演示永远未开场；待映的《深海寻秘》含预售排场）
INSERT INTO `t_screening` (`movie_id`, `hall_id`, `cinema_id`, `start_time`, `price`) VALUES
    (1, 1, 1, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '10:00:00'), 39.90),
    (1, 3, 1, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '19:00:00'), 59.90),
    (2, 2, 1, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '14:00:00'), 49.90),
    (3, 1, 1, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '10:00:00'), 39.90),
    (3, 2, 1, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '15:00:00'), 49.90),
    (4, 3, 1, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '18:00:00'), 45.00),
    (1, 4, 2, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '13:00:00'), 35.00),
    (2, 4, 2, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '16:00:00'), 35.00);
