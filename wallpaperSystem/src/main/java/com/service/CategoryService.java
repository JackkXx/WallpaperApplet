package com.service;

import com.entity.Category;
import com.entity.ProfileCategory;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;

public interface CategoryService {
    List<Category> getAll() throws JsonProcessingException;
    List<Category> getList();
    List<Category> findByCid(Integer cid);
    boolean add(Category category);
    boolean delete(Integer cid);
    boolean edit(Category category);
    boolean updateShow(Category category);
}
