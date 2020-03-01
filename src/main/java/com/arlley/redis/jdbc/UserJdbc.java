package com.arlley.redis.jdbc;

import com.arlley.redis.Cache;
import com.arlley.redis.domain.User;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Repository
public class UserJdbc {

    @Resource
    private JdbcTemplate jdbcTemplate;

    public static final String key = "USER-ID-KEY";

    @Resource
    private RedisTemplate<String, User> redisTemplate;

//    @Cacheable(cacheManager = "cacheManager", value = key, key = "#id")
    @Cache(key = "#id", pre = key)
    public User getUser(int id){
        // 判断缓存中是否存在
//        if(redisTemplate.hasKey(key)){
//            User user = (User) redisTemplate.opsForHash().get(key, id);
//            if(!Objects.isNull(user)){
//                System.out.println("缓存中获取到user id="+id);
//                return user;
//            }
//        }
        Object[] args = new Object[1];
        args[0] = id;
        List<User> userList = jdbcTemplate.query("select * from user where id = ?", args, new RowMapper<User>() {
            @Override
            public User mapRow(ResultSet rs, int rowNum) throws SQLException {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setName(rs.getString("name"));
                return user;
            }
        });

        //redisTemplate.opsForHash().put(key, id, userList.get(0));
//        System.out.println("把数据库获取到的放入缓存中id="+id);

        return userList.get(0);
    }
}
