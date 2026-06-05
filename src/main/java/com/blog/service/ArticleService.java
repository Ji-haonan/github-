package com.blog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.entity.Article;

import java.util.List;

public interface ArticleService extends IService<Article> {

    IPage<Article> pageWithCategory(int pageNum, int pageSize, Long categoryId);

    Article getByIdWithTags(Long id);

    void incrementViewCount(Long id);

    boolean saveArticle(Article article, List<Long> tagIds);

    boolean updateArticle(Article article, List<Long> tagIds);
}