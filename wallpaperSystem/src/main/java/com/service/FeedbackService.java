package com.service;

import com.entity.Feedback;

import java.util.List;

public interface FeedbackService {
    List<Feedback> findAll();
    boolean delete(Integer id);
    boolean add(Feedback feedback);
}
