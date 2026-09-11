package com.cdwater.cdticket.user.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cdwater.cdticket.user.domain.UserRepository;
import com.cdwater.cdticket.user.infrastructure.entity.User;
import com.cdwater.cdticket.user.infrastructure.mapper.UserMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final UserMapper userMapper;

    public UserRepositoryImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public Optional<User> findByPhone(String phone) {
        return Optional.ofNullable(userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getPhone, phone)));
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(userMapper.selectById(id));
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
}
