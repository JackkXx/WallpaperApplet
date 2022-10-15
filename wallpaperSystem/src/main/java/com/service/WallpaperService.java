package com.service;

import com.entity.WallPhoto;

import java.util.List;

public interface WallpaperService {
    List<WallPhoto> findPhotoByCid(Integer cid);

    boolean add(List<WallPhoto> wallPhotos);

    boolean delete(Integer id,String fileKey);

    boolean addonline(WallPhoto wallPhoto);

    int countForCount(Integer cid);

    int countTotal();
}
