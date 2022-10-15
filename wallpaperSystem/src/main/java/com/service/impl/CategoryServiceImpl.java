package com.service.impl;

import com.dao.CategoryMapper;
import com.entity.Category;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.service.CategoryService;
import com.service.RedisCacheService;
import com.util.redis.RedisConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RedisCacheService redisCacheService;

    //微信小程序前台获取壁纸分类信息
    @Override
    public List<Category> getAll() throws JsonProcessingException {
        //1.先从redis中查询有无壁纸分类信息
        List<String> wallcategory = stringRedisTemplate.opsForList().range(RedisConfig.WALLCATEGORY_KEY, 0, -1);
        List<Category> res = new ArrayList<Category>();
        //2.1 Redis中如果没有数据
        if (wallcategory.size() == 0){
            // 2.2 从Mysql中查询数据
            res = categoryMapper.findAll();
            // 2.3 写入Redis
            for (Category category : res){
                stringRedisTemplate.opsForList().leftPush(RedisConfig.WALLCATEGORY_KEY,objectMapper.writeValueAsString(category));
            }
            return res;
        //3.1 如果Redis中有数据，就读取Redis中的数据
        }else {
            //从Redis中读取数据
            for (String str : wallcategory){
                Category category = null;
                try {
                    category = objectMapper.readValue(str, Category.class);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                res.add(category);
            }
        }
        return res;
    }

    @Override
    public List<Category> getList() {
        return categoryMapper.findList();
    }

    @Override
    public List<Category> findByCid(Integer cid) {
        return categoryMapper.findByCid(cid);
    }

    @Override
    public boolean add(Category category) {
        if (categoryMapper.add(category)>0){
            stringRedisTemplate.delete(RedisConfig.WALLCATEGORY_KEY);
            return true;
        }
        return false;

    }

    @Override
    public boolean delete(Integer cid) {
        if (categoryMapper.delete(cid)>0){
            stringRedisTemplate.delete(RedisConfig.WALLCATEGORY_KEY);
            return true;
        }
        return false;
    }

    @Override
    public boolean edit(Category category) {
        if (categoryMapper.update(category)>0){
            stringRedisTemplate.delete(RedisConfig.WALLCATEGORY_KEY);
            return true;
        }
        return false;
    }

    @Override
    public boolean updateShow(Category category) {
        int rows = categoryMapper.updateShow(category);
        if (rows > 0){
            //更新缓存
            redisCacheService.updateWallCategoryCache();
            return true;
        }
        return false;

    }
}
