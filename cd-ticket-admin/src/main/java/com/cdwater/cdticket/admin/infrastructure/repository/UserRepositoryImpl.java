package com.cdwater.cdticket.admin.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cdwater.cdticket.admin.domain.UserRepository;
import com.cdwater.cdticket.admin.infrastructure.entity.User;
import com.cdwater.cdticket.admin.infrastructure.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserMapper userMapper;

    @Override
    public User findByPhone(String phone) {
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
    }

    @Override
    public User findById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            userMapper.insert(user);
        } else {
            userMapper.updateById(user);
        }
        return user;
    }

    @Override
    public IPage<User> pageByPhone(IPage<User> page, String phone) {
        return userMapper.selectPage(page, new LambdaQueryWrapper<User>()
                .like(phone != null && !phone.isBlank(), User::getPhone, phone)
                .orderByDesc(User::getId));
    }
}