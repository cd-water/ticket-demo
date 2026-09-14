-- =============================================================
-- 电影票务系统 CD-TICKET 初始数据（幂等：先清空再插入）
-- 与 table.sql 配套执行：mysql -uroot -p cd_ticket < script/sql/seed.sql
-- 密码哈希 $2a$10$... 对应明文均为 Aa123456（BCrypt）
-- =============================================================

TRUNCATE TABLE `t_admin`;
TRUNCATE TABLE `t_user`;
TRUNCATE TABLE `t_cinema`;
TRUNCATE TABLE `t_movie`;
TRUNCATE TABLE `t_banner`;
TRUNCATE TABLE `t_hall`;
TRUNCATE TABLE `t_seat_config`;
TRUNCATE TABLE `t_screening`;
TRUNCATE TABLE `t_order_item`;
TRUNCATE TABLE `t_order`;
TRUNCATE TABLE `t_payment_record`;

-- 管理员（明文密码 Aa123456）
INSERT INTO `t_admin` (`username`, `password`, `status`)
VALUES ('admin', '$2a$10$SwwMs7T7E2rWloWEZP9ABeANabe992RETeA4.0HOfr2AqszhwSkiu', 1),
       ('cinema01', '$2a$10$SwwMs7T7E2rWloWEZP9ABeANabe992RETeA4.0HOfr2AqszhwSkiu', 1),
       ('cinema02', '$2a$10$SwwMs7T7E2rWloWEZP9ABeANabe992RETeA4.0HOfr2AqszhwSkiu', 1);

-- 影院
INSERT INTO `t_cinema` (`name`, `address`, `status`)
VALUES ('CGV影城（成都SKP店）', '四川省成都市高新区天府大道中段1388号成都SKP购物中心7层', 1),
       ('万达影城（金牛万达店）', '四川省成都市金牛区人民北路二段188号金牛万达广场4层', 1);

-- 电影（status 0-下架 1-上架；热映/待映由 release_date 与当前时间比对得出）
-- 上映日期相对今天：3 部热映 + 1 部待映（预售）+ 1 部下架（演示管理端状态过滤）
INSERT INTO `t_movie` (`title`, `poster`, `description`, `duration`, `release_date`, `status`)
VALUES ('星际穿越', 'https://picsum.photos/seed/m1/300/420', '人类接近灭亡，前宇航员穿越虫洞为人类寻找新家园。', 169,
        DATE_SUB(CURDATE(), INTERVAL 5 DAY), 1),
       ('流浪地球3', 'https://picsum.photos/seed/m2/300/420', '太阳危机临近，人类再次推动地球踏上流浪之旅。', 173,
        DATE_SUB(CURDATE(), INTERVAL 2 DAY), 1),
       ('哪吒之魔童闹海', 'https://picsum.photos/seed/m3/300/420', '哪吒与敖丙携手对抗四海龙族的围困。', 110,
        DATE_SUB(CURDATE(), INTERVAL 10 DAY), 1),
       ('深海寻秘', 'https://picsum.photos/seed/m4/300/420', '一支探险队深入马里亚纳海沟，揭开深海文明的秘密。', 112,
        DATE_ADD(CURDATE(), INTERVAL 20 DAY), 1),
       ('功夫熊猫5', 'https://picsum.photos/seed/m5/300/420', '阿宝再次出山，守护和平谷的宁静。', 100,
        DATE_SUB(CURDATE(), INTERVAL 30 DAY), 0);

-- 轮播图（status 0-禁用 1-启用）
INSERT INTO `t_banner` (`image`, `link_url`, `sort`, `status`)
VALUES ('https://picsum.photos/seed/b1/1200/500', '/movies/1', 1, 1),
       ('https://picsum.photos/seed/b2/1200/500', '/movies/2', 2, 1),
       ('https://picsum.photos/seed/b3/1200/500', '/movies/3', 3, 0);

-- 影厅
INSERT INTO `t_hall` (`cinema_id`, `name`, `seat_rows`, `seat_cols`, `status`)
VALUES (1, '1号激光厅', 4, 5, 1),
       (1, '2号杜比厅', 5, 6, 1),
       (1, '3号IMAX厅', 6, 8, 1),
       (2, '1号厅', 4, 5, 1);

-- 座位模板：按各影厅行列数一次性生成（status 1-启用；1号激光厅第1排1/5座演示禁用）
INSERT INTO `t_seat_config` (`hall_id`, `seat_row`, `seat_col`, `seat_no`, `status`)
WITH RECURSIVE `seq` AS (SELECT 1 AS n
                         UNION ALL
                         SELECT n + 1
                         FROM `seq`
                         WHERE n < 8)
SELECT h.id,
       r.n,
       c.n,
       CONCAT(r.n, '排', c.n, '座'),
       CASE WHEN h.id = 1 AND r.n = 1 AND c.n IN (1, 5) THEN 0 ELSE 1 END
FROM `t_hall` h
         CROSS JOIN `seq` r
         CROSS JOIN `seq` c
WHERE r.n <= h.seat_rows
  AND c.n <= h.seat_cols;

-- 排场（时间相对今天，保证演示永远未开场；待映的《深海寻秘》含预售排场）
INSERT INTO `t_screening` (`movie_id`, `hall_id`, `cinema_id`, `start_time`, `price`)
VALUES (1, 1, 1, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '10:00:00'), 39.90),
       (1, 3, 1, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '19:00:00'), 59.90),
       (2, 2, 1, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '14:00:00'), 49.90),
       (3, 1, 1, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '10:00:00'), 39.90),
       (3, 2, 1, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '15:00:00'), 49.90),
       (4, 3, 1, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '18:00:00'), 45.00),
       (1, 4, 2, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '13:00:00'), 35.00),
       (2, 4, 2, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '16:00:00'), 35.00);

-- =============================================================
-- 补充数据
-- =============================================================

-- C端用户（密码哈希同上，明文 Aa123456）
INSERT INTO `t_user` (`phone`, `password`, `nickname`, `status`)
VALUES ('13800000001', '$2a$10$SwwMs7T7E2rWloWEZP9ABeANabe992RETeA4.0HOfr2AqszhwSkiu', '影迷小张', 1),
       ('13800000002', '$2a$10$SwwMs7T7E2rWloWEZP9ABeANabe992RETeA4.0HOfr2AqszhwSkiu', '电影达人', 1),
       ('13800000003', NULL, '小李飞刀', 1),
       ('13800000004', NULL, '周末观影团', 1),
       ('13800000005', '$2a$10$SwwMs7T7E2rWloWEZP9ABeANabe992RETeA4.0HOfr2AqszhwSkiu', '爆米花爱好者', 1),
       ('13800000006', NULL, '夜场常客', 1),
       ('13800000007', '$2a$10$SwwMs7T7E2rWloWEZP9ABeANabe992RETeA4.0HOfr2AqszhwSkiu', 'IMAX发烧友', 1),
       ('13800000008', NULL, '学生党小王', 1),
       ('13800000009', '$2a$10$SwwMs7T7E2rWloWEZP9ABeANabe992RETeA4.0HOfr2AqszhwSkiu', '约会看片', 1),
       ('13800000010', NULL, '退休老影迷', 1);

-- 更多电影
INSERT INTO `t_movie` (`title`, `poster`, `description`, `duration`, `release_date`, `status`)
VALUES ('封神第三部', 'https://picsum.photos/seed/m6/300/420', '姬发率天下诸侯讨伐纣王，封神大战一触即发。', 148,
        DATE_SUB(CURDATE(), INTERVAL 1 DAY), 1),
       ('长安三万里', 'https://picsum.photos/seed/m7/300/420', '高适回忆与李白跨越数十年的友情，展现大唐盛世的诗与远方。',
        168, DATE_SUB(CURDATE(), INTERVAL 15 DAY), 1),
       ('年会不能停！', 'https://picsum.photos/seed/m8/300/420', '一场阴差阳错的人事调动，揭露大厂荒诞的职场众生相。', 118,
        DATE_SUB(CURDATE(), INTERVAL 20 DAY), 1),
       ('唐探1900', 'https://picsum.photos/seed/m9/300/420', '唐仁与秦风回到1900年的旧金山，破解一起离奇命案。', 130,
        DATE_ADD(CURDATE(), INTERVAL 30 DAY), 1),
       ('熊出没·重启未来', 'https://picsum.photos/seed/m10/300/420', '光头强与熊大熊二穿越到未来世界，开启全新冒险。', 99,
        DATE_ADD(CURDATE(), INTERVAL 15 DAY), 1),
       ('热辣滚烫', 'https://picsum.photos/seed/m11/300/420', '乐莹在人生低谷决定为自己赢一次，开启拳击生涯。', 129,
        DATE_SUB(CURDATE(), INTERVAL 45 DAY), 1),
       ('第二十条', 'https://picsum.photos/seed/m12/300/420', '检察官韩明在办案中陷入情与法的两难抉择。', 120,
        DATE_SUB(CURDATE(), INTERVAL 50 DAY), 1);

-- 更多轮播图
INSERT INTO `t_banner` (`image`, `link_url`, `sort`, `status`)
VALUES ('https://picsum.photos/seed/b4/1200/500', '/movies/6', 4, 1),
       ('https://picsum.photos/seed/b5/1200/500', '/movies/7', 5, 1),
       ('https://picsum.photos/seed/b6/1200/500', '/movies/9', 6, 0);

-- 更多影院
INSERT INTO `t_cinema` (`name`, `address`, `status`)
VALUES ('博纳国际影城（春熙路店）', '四川省成都市锦江区春熙路步行街99号IFS国际金融中心7层', 1),
       ('太平洋影城（天府广场店）', '四川省成都市青羊区天府广场西侧城市之心3层', 1);

-- 更多影厅
INSERT INTO `t_hall` (`cinema_id`, `name`, `seat_rows`, `seat_cols`, `status`)
VALUES (3, '1号厅', 5, 6, 1),
       (3, '2号VIP厅', 3, 4, 1),
       (3, '3号IMAX厅', 7, 10, 1),
       (4, '1号厅', 4, 6, 1),
       (4, '2号厅', 5, 5, 1);

-- 更多排场
INSERT INTO `t_screening` (`movie_id`, `hall_id`, `cinema_id`, `start_time`, `price`)
VALUES
    -- 博纳春熙路店
    (1, 5, 3, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '10:30:00'), 42.00),
    (1, 7, 3, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '14:30:00'), 68.00),
    (2, 6, 3, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '13:00:00'), 88.00),
    (3, 5, 3, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '11:00:00'), 42.00),
    (6, 7, 3, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '19:30:00'), 58.00),
    (7, 5, 3, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '16:00:00'), 39.90),
    -- 太平洋天府广场店
    (1, 8, 4, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '11:30:00'), 36.00),
    (2, 8, 4, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '19:00:00'), 36.00),
    (3, 9, 4, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '14:00:00'), 38.00),
    (6, 9, 4, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '20:30:00'), 38.00),
    (7, 8, 4, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 3 DAY), '10:00:00'), 32.00);

-- =============================================================
-- 订单数据（三种状态各若干条）
-- =============================================================

-- 已支付订单（今天+1天，星际穿越 CGV 1号激光厅）
INSERT INTO `t_order` (`order_no`, `user_id`, `screening_id`, `movie_id`, `cinema_id`, `status`, `total_amount`,
                       `pay_expire_time`, `pay_time`)
VALUES (1000000000000001, 1, 1, 1, 1, 1, 79.80, DATE_ADD(TIMESTAMP(CURDATE(), '10:00:00'), INTERVAL -30 MINUTE),
        DATE_ADD(TIMESTAMP(CURDATE(), '10:00:00'), INTERVAL -25 MINUTE)),
       (1000000000000002, 2, 1, 1, 1, 1, 59.90, DATE_ADD(TIMESTAMP(CURDATE(), '10:00:00'), INTERVAL -30 MINUTE),
        DATE_ADD(TIMESTAMP(CURDATE(), '10:00:00'), INTERVAL -20 MINUTE));
INSERT INTO `t_order_item` (`order_id`, `seat_row`, `seat_col`, `seat_no`, `price`)
VALUES (1, 3, 4, '3排4座', 39.90),
       (1, 3, 5, '3排5座', 39.90),
       (2, 2, 6, '2排6座', 59.90);

INSERT INTO `t_payment_record` (`payment_no`, `order_id`, `order_no`, `amount`, `status`, `pay_time`)
VALUES (CONCAT('PAY', DATE_FORMAT(NOW(), '%Y%m%d%H%i%s'), '001'), 1, 1000000000000001, 79.80, 1,
        DATE_ADD(TIMESTAMP(CURDATE(), '10:00:00'), INTERVAL -25 MINUTE)),
       (CONCAT('PAY', DATE_FORMAT(NOW(), '%Y%m%d%H%i%s'), '002'), 2, 1000000000000002, 59.90, 1,
        DATE_ADD(TIMESTAMP(CURDATE(), '10:00:00'), INTERVAL -20 MINUTE));

-- 待支付订单（今天+2天，流浪地球3）
INSERT INTO `t_order` (`order_no`, `user_id`, `screening_id`, `movie_id`, `cinema_id`, `status`, `total_amount`,
                       `version`, `pay_expire_time`)
VALUES (1000000000000003, 3, 3, 2, 1, 0, 49.90, 0, DATE_ADD(NOW(), INTERVAL 15 MINUTE));
INSERT INTO `t_order_item` (`order_id`, `seat_row`, `seat_col`, `seat_no`, `price`)
VALUES (3, 5, 3, '5排3座', 49.90);

-- 已取消订单（今天+1天，哪吒 IMAX厅）
INSERT INTO `t_order` (`order_no`, `user_id`, `screening_id`, `movie_id`, `cinema_id`, `status`, `total_amount`,
                       `pay_expire_time`, `pay_time`)
VALUES (1000000000000004, 4, 6, 3, 1, 2, 39.90, DATE_SUB(NOW(), INTERVAL 2 HOUR), NULL);
INSERT INTO `t_order_item` (`order_id`, `seat_row`, `seat_col`, `seat_no`, `price`)
VALUES (4, 4, 2, '4排2座', 39.90);
