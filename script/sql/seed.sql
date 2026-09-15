-- =============================================================
-- 电影票务系统 CD-TICKET 初始数据（幂等：先清空再插入）
-- 与 table.sql 配套执行：mysql -uroot -p cd_ticket < script/sql/seed.sql
-- 密码哈希 $2a$10$SwwMs7T7E2rWloWEZP9ABeANabe992RETeA4.0HOfr2AqszhwSkiu 对应明文均为 Aa123456（BCrypt）
-- =============================================================

-- 清空业务表（按依赖反向顺序；t_local_message 一并清空但不入数据）
TRUNCATE TABLE `t_refund`;
TRUNCATE TABLE `t_payment`;
TRUNCATE TABLE `t_order_item`;
TRUNCATE TABLE `t_order`;
TRUNCATE TABLE `t_screening`;
TRUNCATE TABLE `t_seat_config`;
TRUNCATE TABLE `t_hall`;
TRUNCATE TABLE `t_cinema`;
TRUNCATE TABLE `t_banner`;
TRUNCATE TABLE `t_movie`;
TRUNCATE TABLE `t_user`;
TRUNCATE TABLE `t_admin`;
TRUNCATE TABLE `t_local_message`;

-- =============================================================
-- 管理员（明文密码 Aa123456）
-- =============================================================
INSERT INTO `t_admin` (`username`, `password`, `status`)
VALUES ('admin', '$2a$10$SwwMs7T7E2rWloWEZP9ABeANabe992RETeA4.0HOfr2AqszhwSkiu', 1),
       ('admin2', '$2a$10$SwwMs7T7E2rWloWEZP9ABeANabe992RETeA4.0HOfr2AqszhwSkiu', 1),
       ('admin3', '$2a$10$SwwMs7T7E2rWloWEZP9ABeANabe992RETeA4.0HOfr2AqszhwSkiu', 1);

-- =============================================================
-- C 端用户（5 个有密码 / 5 个免密登录）
-- =============================================================
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

-- =============================================================
-- 电影（20 部：混合热映 / 待映 / 下架，演示 B 端状态过滤）
-- 状态：0-下架 1-上架；热映/待映由 release_date 与当前日期比对得出
-- =============================================================
INSERT INTO `t_movie` (`title`, `poster`, `description`, `duration`, `release_date`, `status`)
VALUES ('流浪地球3', 'http://localhost:9000/cd-ticket/avatar.jpg',
        '太阳异变，人类重启流浪地球计划，带着家园踏上 2500 年的宇宙征途。', 173, DATE_SUB(CURDATE(), INTERVAL 3 DAY), 1),
       ('哪吒之魔童闹海', 'http://localhost:9000/cd-ticket/avatar.jpg',
        '哪吒与敖丙冲破偏见，再度联手对抗命中注定的天劫。',
        144, DATE_SUB(CURDATE(), INTERVAL 10 DAY), 1),
       ('唐探1900', 'http://localhost:9000/cd-ticket/avatar.jpg', '唐仁与秦风穿越到 1900 年的旧金山，破解一桩华人血案。',
        130,
        DATE_SUB(CURDATE(), INTERVAL 5 DAY), 1),
       ('封神第二部：战火西岐', 'http://localhost:9000/cd-ticket/avatar.jpg', '姬发率西岐军民抵御殷商大军，封神战火再燃。',
        148, DATE_SUB(CURDATE(), INTERVAL 15 DAY), 1),
       ('飞驰人生2', 'http://localhost:9000/cd-ticket/avatar.jpg', '张驰重返巴音布鲁克，这次他不是为自己赢。', 121,
        DATE_SUB(CURDATE(), INTERVAL 7 DAY), 1),
       ('维和防暴队', 'http://localhost:9000/cd-ticket/avatar.jpg', '中国维和警察远赴海外，在枪林弹雨中守护和平。', 115,
        DATE_SUB(CURDATE(), INTERVAL 2 DAY), 1),
       ('银河写手', 'http://localhost:9000/cd-ticket/avatar.jpg', '三个编剧死磕一个剧本，把北漂写成一部荒诞喜剧。', 102,
        DATE_SUB(CURDATE(), INTERVAL 8 DAY), 1),
       ('周处除三害', 'http://localhost:9000/cd-ticket/avatar.jpg', '通缉犯陈桂林在自我救赎的尽头，遇见人性最后的光。',
        134,
        DATE_SUB(CURDATE(), INTERVAL 20 DAY), 1),
       ('满江红', 'http://localhost:9000/cd-ticket/avatar.jpg', '南宋小兵张大与亲兵营副统领孙均一夜破局。', 159,
        DATE_SUB(CURDATE(), INTERVAL 150 DAY), 0),
       ('长津湖', 'http://localhost:9000/cd-ticket/avatar.jpg', '志愿军连队在长津湖战役中坚守阵地，全歼北极熊团。', 176,
        DATE_SUB(CURDATE(), INTERVAL 800 DAY), 0),
       ('误判', 'http://localhost:9000/cd-ticket/avatar.jpg', '检察官韩明在情与法的夹缝中寻找真相。', 118,
        DATE_SUB(CURDATE(), INTERVAL 12 DAY), 1),
       ('九龙城寨之围城', 'http://localhost:9000/cd-ticket/avatar.jpg', '上世纪八十年代九龙城寨里的热血兄弟情。', 141,
        DATE_SUB(CURDATE(), INTERVAL 30 DAY), 1),
       ('末路狂花钱', 'http://localhost:9000/cd-ticket/avatar.jpg', '中年保安在生命倒计时里，花光所有存款。', 104,
        DATE_SUB(CURDATE(), INTERVAL 25 DAY), 1),
       ('头脑特工队2', 'http://localhost:9000/cd-ticket/avatar.jpg', '莱莉进入青春期，大脑总部迎来全新情绪入住。', 96,
        DATE_SUB(CURDATE(), INTERVAL 45 DAY), 1),
       ('死侍与金刚狼', 'http://localhost:9000/cd-ticket/avatar.jpg', '死侍拉上金刚狼组队，穿越多元宇宙拯救一切。', 128,
        DATE_SUB(CURDATE(), INTERVAL 100 DAY), 0),
       ('749局', 'http://localhost:9000/cd-ticket/avatar.jpg', '少年马山觉醒异能力，加入 749 局对抗神秘外星生物。', 110,
        DATE_ADD(CURDATE(), INTERVAL 5 DAY), 1),
       ('角斗士2', 'http://localhost:9000/cd-ticket/avatar.jpg', '罗马英雄卢修斯踏上复仇之路，重夺荣耀与自由。', 148,
        DATE_ADD(CURDATE(), INTERVAL 10 DAY), 1),
       ('毒液：最后一舞', 'http://localhost:9000/cd-ticket/avatar.jpg', '埃迪与毒液背水一战，对抗来自共生体母星的追杀。',
        109,
        DATE_ADD(CURDATE(), INTERVAL 20 DAY), 1),
       ('那个不为人知的故事', 'http://localhost:9000/cd-ticket/avatar.jpg',
        '残疾出租车司机与失聪女大学生跨越阶层的爱情挽歌。', 117, DATE_ADD(CURDATE(), INTERVAL 30 DAY), 1),
       ('窗前明月，咣！', 'http://localhost:9000/cd-ticket/avatar.jpg', '一场乌龙绑架案，让中年危机男遇上失控少女。', 99,
        DATE_ADD(CURDATE(), INTERVAL 45 DAY), 1);

-- =============================================================
-- 轮播图（4 张，全部启用；sort 小的在前）
-- =============================================================
INSERT INTO `t_banner` (`image`, `link_url`, `sort`, `status`)
VALUES ('http://localhost:9000/cd-ticket/avatar.jpg', 'https://www.baidu.com/', 1, 1),
       ('http://localhost:9000/cd-ticket/avatar.jpg', 'https://www.baidu.com/', 2, 1),
       ('http://localhost:9000/cd-ticket/avatar.jpg', 'https://www.baidu.com/', 3, 1),
       ('http://localhost:9000/cd-ticket/avatar.jpg', 'https://www.baidu.com/', 4, 1);

-- =============================================================
-- 影院（11 家，跨 7 个城市）
-- =============================================================
INSERT INTO `t_cinema` (`name`, `address`, `status`)
VALUES ('北京万达影城（CBD店）', '北京市朝阳区建国路93号万达广场B座5层', 1),
       ('北京金逸影城（中关村店）', '北京市海淀区中关村大街27号中关村大厦8层', 1),
       ('上海百丽宫影城（南京西路店）', '上海市静安区南京西路1601号嘉里中心L4层', 1),
       ('上海SFC上影影城（徐家汇店）', '上海市徐汇区肇嘉浜路1029号美罗城5层', 1),
       ('广州飞扬影城（天河城店）', '广州市天河区天河路208号天河城购物中心5层', 1),
       ('深圳CGV影城（万象城店）', '深圳市罗湖区宝安南路1881号华润中心万象城L4层', 1),
       ('成都万达影城（锦华路店）', '成都市锦江区锦华路一段68号万达广场4层', 1),
       ('成都太平洋影城（春熙路店）', '成都市锦江区春熙路东大街8号IFS国际金融中心6层', 1),
       ('杭州金逸影城（湖滨店）', '杭州市上城区延安路398号湖滨银泰in77 L4层', 1),
       ('武汉金逸影城（光谷店）', '武汉市洪山区珞瑜路717号光谷世界城B区4层', 1),
       ('西安博纳国际影城（大雁塔店）', '西安市雁塔区慈恩西路66号大悦城购物中心L4层', 1);

-- =============================================================
-- 影厅（每个影院一个，规模不一：含 IMAX 大厅与 VIP 小厅）
-- =============================================================
INSERT INTO `t_hall` (`cinema_id`, `name`, `seat_rows`, `seat_cols`, `status`)
VALUES (1, '1号激光厅', 5, 8, 1),
       (2, '1号厅', 4, 6, 1),
       (3, '1号IMAX厅', 6, 10, 1),
       (4, '1号厅', 5, 7, 1),
       (5, '1号厅', 5, 8, 1),
       (6, '1号IMAX厅', 6, 10, 1),
       (7, '1号厅', 5, 8, 1),
       (8, '1号厅', 4, 7, 1),
       (9, '1号VIP厅', 5, 9, 1),
       (10, '1号IMAX厅', 6, 9, 1),
       (11, '1号厅', 5, 8, 1);

-- =============================================================
-- 座位模板（递归生成所有影厅的座位；hall 1 第1排两端演示禁用）
-- =============================================================
INSERT INTO `t_seat_config` (`hall_id`, `seat_row`, `seat_col`, `seat_no`, `status`)
WITH RECURSIVE `seq` AS (SELECT 1 AS n
                         UNION ALL
                         SELECT n + 1
                         FROM `seq`
                         WHERE n < 10)
SELECT h.id,
       r.n,
       c.n,
       CONCAT(r.n, '排', c.n, '座'),
       CASE WHEN h.id = 1 AND r.n = 1 AND c.n IN (1, 8) THEN 0 ELSE 1 END
FROM `t_hall` h
         CROSS JOIN `seq` r
         CROSS JOIN `seq` c
WHERE r.n <= h.seat_rows
  AND c.n <= h.seat_cols;

-- =============================================================
-- 排场（11 个影厅 × 3 场 = 33 场，覆盖 +1/+2/+3 天三个时段）
-- =============================================================
INSERT INTO `t_screening` (`movie_id`, `hall_id`, `cinema_id`, `start_time`, `price`)
VALUES
-- hall 1 北京万达 激光厅
(1, 1, 1, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '10:00:00'), 49.90),
(2, 1, 1, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '14:00:00'), 39.90),
(3, 1, 1, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 3 DAY), '19:00:00'), 45.00),
-- hall 2 北京金逸
(4, 2, 2, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '14:00:00'), 42.00),
(5, 2, 2, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '19:00:00'), 55.00),
(6, 2, 2, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 3 DAY), '10:00:00'), 38.00),
-- hall 3 上海百丽宫 IMAX
(7, 3, 3, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '19:00:00'), 65.00),
(8, 3, 3, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '10:00:00'), 49.90),
(9, 3, 3, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 3 DAY), '14:00:00'), 42.00),
-- hall 4 上海SFC
(10, 4, 4, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '10:00:00'), 39.00),
(11, 4, 4, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '14:00:00'), 49.90),
(12, 4, 4, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 3 DAY), '19:00:00'), 39.00),
-- hall 5 广州飞扬
(13, 5, 5, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '14:00:00'), 45.00),
(14, 5, 5, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '19:00:00'), 49.90),
(15, 5, 5, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 3 DAY), '10:00:00'), 42.00),
-- hall 6 深圳CGV IMAX（含预售场次演示待映购票）
(16, 6, 6, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '10:00:00'), 49.90),
(1, 6, 6, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '14:00:00'), 39.00),
(2, 6, 6, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 3 DAY), '19:00:00'), 49.90),
-- hall 7 成都万达
(3, 7, 7, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '19:00:00'), 39.00),
(4, 7, 7, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '10:00:00'), 42.00),
(5, 7, 7, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 3 DAY), '14:00:00'), 55.00),
-- hall 8 成都太平洋
(6, 8, 8, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '14:00:00'), 38.00),
(7, 8, 8, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '19:00:00'), 65.00),
(17, 8, 8, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 3 DAY), '10:00:00'), 49.90),
-- hall 9 杭州金逸VIP
(8, 9, 9, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '10:00:00'), 49.90),
(9, 9, 9, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '14:00:00'), 42.00),
(18, 9, 9, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 3 DAY), '19:00:00'), 49.90),
-- hall 10 武汉金逸IMAX
(10, 10, 10, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '19:00:00'), 39.00),
(11, 10, 10, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '10:00:00'), 49.90),
(19, 10, 10, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 3 DAY), '14:00:00'), 42.00),
-- hall 11 西安博纳
(12, 11, 11, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '14:00:00'), 39.00),
(20, 11, 11, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '19:00:00'), 49.90),
(13, 11, 11, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 3 DAY), '10:00:00'), 45.00);

-- =============================================================
-- 订单（11 单：6 已支付 + 3 待支付 + 2 已取消）
-- 已支付订单对应支付流水；订单 11 已退款，对应退款流水
-- =============================================================

-- 1 已支付（影迷小张 / 流浪地球3 / 北京万达 1号激光厅）
INSERT INTO `t_order` (`order_no`, `user_id`, `screening_id`, `movie_id`, `cinema_id`, `status`,
                       `total_amount`, `pay_expire_time`, `pay_time`, `ticket_code`)
VALUES (2099782383724138496, 1, 1, 1, 1, 1, 99.80,
        DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 2 HOUR), LPAD(FLOOR(RAND() * 90000000 + 10000000), 8, '0'));
INSERT INTO `t_order_item` (`order_id`, `screening_id`, `seat_row`, `seat_col`, `seat_no`, `price`)
VALUES (1, 1, 3, 4, '3排4座', 49.90),
       (1, 1, 3, 5, '3排5座', 49.90);

-- 2 已支付（电影达人 / 银河写手 / 上海百丽宫 IMAX — 3 张）
INSERT INTO `t_order` (`order_no`, `user_id`, `screening_id`, `movie_id`, `cinema_id`, `status`,
                       `total_amount`, `pay_expire_time`, `pay_time`, `ticket_code`)
VALUES (2099782383795445797, 2, 7, 7, 3, 1, 195.00,
        DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 3 HOUR), LPAD(FLOOR(RAND() * 90000000 + 10000000), 8, '0'));
INSERT INTO `t_order_item` (`order_id`, `screening_id`, `seat_row`, `seat_col`, `seat_no`, `price`)
VALUES (2, 7, 4, 6, '4排6座', 65.00),
       (2, 7, 4, 7, '4排7座', 65.00),
       (2, 7, 4, 8, '4排8座', 65.00);

-- 3 已支付（爆米花爱好者 / 唐探1900 / 成都万达）
INSERT INTO `t_order` (`order_no`, `user_id`, `screening_id`, `movie_id`, `cinema_id`, `status`,
                       `total_amount`, `pay_expire_time`, `pay_time`, `ticket_code`)
VALUES (2099782383866753098, 5, 19, 3, 7, 1, 78.00,
        DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 4 HOUR), LPAD(FLOOR(RAND() * 90000000 + 10000000), 8, '0'));
INSERT INTO `t_order_item` (`order_id`, `screening_id`, `seat_row`, `seat_col`, `seat_no`, `price`)
VALUES (3, 19, 2, 3, '2排3座', 39.00),
       (3, 19, 2, 4, '2排4座', 39.00);

-- 4 已支付（IMAX发烧友 / 周处除三害 / 杭州金逸VIP）
INSERT INTO `t_order` (`order_no`, `user_id`, `screening_id`, `movie_id`, `cinema_id`, `status`,
                       `total_amount`, `pay_expire_time`, `pay_time`, `ticket_code`)
VALUES (2099782383938060399, 7, 25, 8, 9, 1, 49.90,
        DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 6 HOUR), LPAD(FLOOR(RAND() * 90000000 + 10000000), 8, '0'));
INSERT INTO `t_order_item` (`order_id`, `screening_id`, `seat_row`, `seat_col`, `seat_no`, `price`)
VALUES (4, 25, 3, 5, '3排5座', 49.90);

-- 5 已支付（约会看片 / 末路狂花钱 / 广州飞扬）
INSERT INTO `t_order` (`order_no`, `user_id`, `screening_id`, `movie_id`, `cinema_id`, `status`,
                       `total_amount`, `pay_expire_time`, `pay_time`, `ticket_code`)
VALUES (2099782384009367700, 9, 13, 13, 5, 1, 90.00,
        DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 8 HOUR), LPAD(FLOOR(RAND() * 90000000 + 10000000), 8, '0'));
INSERT INTO `t_order_item` (`order_id`, `screening_id`, `seat_row`, `seat_col`, `seat_no`, `price`)
VALUES (5, 13, 4, 4, '4排4座', 45.00),
       (5, 13, 4, 5, '4排5座', 45.00);

-- 6 已支付（电影达人 / 封神第二部 / 北京金逸 — 4 张）
INSERT INTO `t_order` (`order_no`, `user_id`, `screening_id`, `movie_id`, `cinema_id`, `status`,
                       `total_amount`, `pay_expire_time`, `pay_time`, `ticket_code`)
VALUES (2099782384080675001, 2, 4, 4, 2, 1, 168.00,
        DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 12 HOUR), LPAD(FLOOR(RAND() * 90000000 + 10000000), 8, '0'));
INSERT INTO `t_order_item` (`order_id`, `screening_id`, `seat_row`, `seat_col`, `seat_no`, `price`)
VALUES (6, 4, 3, 1, '3排1座', 42.00),
       (6, 4, 3, 2, '3排2座', 42.00),
       (6, 4, 3, 3, '3排3座', 42.00),
       (6, 4, 3, 4, '3排4座', 42.00);

-- 7 待支付（小李飞刀 / 749局 待映 / 深圳CGV IMAX）
INSERT INTO `t_order` (`order_no`, `user_id`, `screening_id`, `movie_id`, `cinema_id`, `status`,
                       `total_amount`, `pay_expire_time`)
VALUES (2099782384151982302, 3, 16, 16, 6, 0, 99.80,
        DATE_ADD(NOW(), INTERVAL 15 MINUTE));
INSERT INTO `t_order_item` (`order_id`, `screening_id`, `seat_row`, `seat_col`, `seat_no`, `price`)
VALUES (7, 16, 4, 3, '4排3座', 49.90),
       (7, 16, 4, 4, '4排4座', 49.90);

-- 8 待支付（学生党小王 / 角斗士2 待映 / 成都太平洋）
INSERT INTO `t_order` (`order_no`, `user_id`, `screening_id`, `movie_id`, `cinema_id`, `status`,
                       `total_amount`, `pay_expire_time`)
VALUES (2099782384223289603, 8, 24, 17, 8, 0, 49.90,
        DATE_ADD(NOW(), INTERVAL 12 MINUTE));
INSERT INTO `t_order_item` (`order_id`, `screening_id`, `seat_row`, `seat_col`, `seat_no`, `price`)
VALUES (8, 24, 2, 4, '2排4座', 49.90);

-- 9 待支付（周末观影团 / 那个不为人知的故事 待映 / 武汉金逸IMAX）
INSERT INTO `t_order` (`order_no`, `user_id`, `screening_id`, `movie_id`, `cinema_id`, `status`,
                       `total_amount`, `pay_expire_time`)
VALUES (2099782384294596904, 4, 30, 19, 10, 0, 84.00,
        DATE_ADD(NOW(), INTERVAL 10 MINUTE));
INSERT INTO `t_order_item` (`order_id`, `screening_id`, `seat_row`, `seat_col`, `seat_no`, `price`)
VALUES (9, 30, 5, 2, '5排2座', 42.00),
       (9, 30, 5, 3, '5排3座', 42.00);

-- 10 已取消（夜场常客 / 维和防暴队 / 成都太平洋 — 超时未支付，无支付/退款记录）
INSERT INTO `t_order` (`order_no`, `user_id`, `screening_id`, `movie_id`, `cinema_id`, `status`,
                       `total_amount`, `pay_expire_time`, `cancel_reason`)
VALUES (2099782384365904205, 6, 22, 6, 8, 2, 76.00,
        DATE_SUB(NOW(), INTERVAL 30 MINUTE), '超时未支付，座位已释放');
INSERT INTO `t_order_item` (`order_id`, `screening_id`, `seat_row`, `seat_col`, `seat_no`, `price`)
VALUES (10, 22, 3, 5, '3排5座', 38.00),
       (10, 22, 3, 6, '3排6座', 38.00);

-- 11 已取消（退休老影迷 / 头脑特工队2 / 武汉金逸IMAX — 已支付后取消，已退款）
INSERT INTO `t_order` (`order_no`, `user_id`, `screening_id`, `movie_id`, `cinema_id`, `status`,
                       `total_amount`, `pay_expire_time`, `pay_time`, `cancel_reason`)
VALUES (2099782384437211506, 10, 28, 14, 10, 2, 78.00,
        DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), '用户取消');
INSERT INTO `t_order_item` (`order_id`, `screening_id`, `seat_row`, `seat_col`, `seat_no`, `price`)
VALUES (11, 28, 4, 3, '4排3座', 39.00),
       (11, 28, 4, 4, '4排4座', 39.00);

-- =============================================================
-- 支付流水（10 条：6 成功 + 3 待支付 + 1 已退款的底层成功支付）
-- =============================================================
INSERT INTO `t_payment` (`payment_no`, `order_id`, `channel`, `amount`, `status`,
                         `out_trade_no`, `trade_no`, `paid_time`)
VALUES (2099782384508518807, 1, 1, 99.80, 1, 'OUT20250914120001001', '2025091422001234567890123456',
        DATE_SUB(NOW(), INTERVAL 2 HOUR)),
       (2099782384579826108, 2, 2, 195.00, 1, 'OUT20250914120001002', '4200001234202309141234567890',
        DATE_SUB(NOW(), INTERVAL 3 HOUR)),
       (2099782384651133409, 3, 1, 78.00, 1, 'OUT20250914120001003', '2025091422001234567890234567',
        DATE_SUB(NOW(), INTERVAL 4 HOUR)),
       (2099782384722440710, 4, 2, 49.90, 1, 'OUT20250914120001004', '4200001234202309141234567891',
        DATE_SUB(NOW(), INTERVAL 6 HOUR)),
       (2099782384793748011, 5, 1, 90.00, 1, 'OUT20250914120001005', '2025091422001234567890345678',
        DATE_SUB(NOW(), INTERVAL 8 HOUR)),
       (2099782384865055312, 6, 2, 168.00, 1, 'OUT20250914120001006', '4200001234202309141234567892',
        DATE_SUB(NOW(), INTERVAL 12 HOUR)),
       (2099782384936362613, 7, 1, 99.80, 0, 'OUT20250914120001007', NULL, NULL),
       (2099782385007669914, 8, 2, 49.90, 0, 'OUT20250914120001008', NULL, NULL),
       (2099782385078977215, 9, 1, 84.00, 0, 'OUT20250914120001009', NULL, NULL),
       (2099782385150284516, 11, 1, 78.00, 1, 'OUT20250914120001011', '2025091422001234567890456789',
        DATE_SUB(NOW(), INTERVAL 2 DAY));

-- =============================================================
-- 退款流水（1 条：订单 11 退款成功）
-- payment_id=10 对应上面 11 订单那笔支付
-- =============================================================
INSERT INTO `t_refund` (`refund_no`, `payment_id`, `order_id`, `channel`, `refund_amount`, `status`,
                        `out_refund_no`, `trade_no`, `reason`, `refund_time`)
VALUES (2099782385221591817, 10, 11, 1, 78.00, 2,
        'REF20250914120001', '2025091422001234567890012345',
        '用户主动申请退款', DATE_SUB(NOW(), INTERVAL 1 DAY));
