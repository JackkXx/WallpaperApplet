package com.service.impl;

import com.dao.FeedbackMapper;
import com.entity.Feedback;
import com.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeedbackServiceImpl implements FeedbackService {
    @Autowired
    private FeedbackMapper feedbackMapper;
    @Override
    public List<Feedback> findAll() {
        return feedbackMapper.findAll();
    }

    @Override
    public boolean delete(Integer id) {
        return feedbackMapper.delete(id)>0?true:false;
    }

    @Override
    public boolean add(Feedback feedback) {
        return feedbackMapper.add(feedback)>0?true:false;
    }
}
