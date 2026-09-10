-- 初始数据脚本，密码明文均为 Aa123456（BCrypt 加密）
-- 超级管理员（cinema_id=0 表示不关联特定影院）
INSERT INTO `admin` (`username`, `password`, `role`, `cinema_id`, `status`) VALUES
    ('admin', '$2a$10$SwwMs7T7E2rWloWEZP9ABeANabe992RETeA4.0HOfr2AqszhwSkiu', 0, 0, 1);

-- 影院管理员（关联影院ID=1）
INSERT INTO `admin` (`username`, `password`, `role`, `cinema_id`, `status`) VALUES
    ('cinema01', '$2a$10$SwwMs7T7E2rWloWEZP9ABeANabe992RETeA4.0HOfr2AqszhwSkiu', 1, 1, 1);

-- 影院
INSERT INTO `cinema` (`name`, `address`, `status`) VALUES
    ('CGV影城（成都SKP店）', '四川省成都市高新区天府大道中段1388号成都SKP购物中心7层', 1);
