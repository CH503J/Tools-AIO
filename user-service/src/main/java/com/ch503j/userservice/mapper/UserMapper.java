package com.ch503j.userservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ch503j.userservice.pojo.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
