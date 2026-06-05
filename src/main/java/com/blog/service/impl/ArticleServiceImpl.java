package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.entity.Article;
import com.blog.entity.ArticleTag;
import com.blog.entity.Category;
import com.blog.entity.Tag;
import com.blog.entity.User;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.ArticleTagMapper;
import com.blog.mapper.CategoryMapper;
import com.blog.mapper.TagMapper;
import com.blog.mapper.UserMapper;
import com.blog.service.ArticleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    private final ArticleTagMapper articleTagMapper;
    private final TagMapper tagMapper;
    private final CategoryMapper categoryMapper;
    private final UserMapper userMapper;

    public ArticleServiceImpl(ArticleTagMapper articleTagMapper, TagMapper tagMapper,
                              CategoryMapper categoryMapper, UserMapper userMapper) {
        this.articleTagMapper = articleTagMapper;
        this.tagMapper = tagMapper;
        this.categoryMapper = categoryMapper;
        this.userMapper = userMapper;
    }

    @Override
    public IPage<Article> pageWithCategory(int pageNum, int pageSize, Long categoryId) {
        Page<Article> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Article> wrapper = new QueryWrapper<>();
        wrapper.eq("a.status", "PUBLISHED");
        wrapper.orderByDesc("a.is_top");
        wrapper.orderByDesc("a.create_time");
        if (categoryId != null) {
            wrapper.eq("a.category_id", categoryId);
        }
        IPage<Article> result = this.baseMapper.selectPageWithCategory(page, wrapper);

        // Populate category, author, and tags for each article
        for (Article article : result.getRecords()) {
            if (article.getCategoryId() != null) {
                article.setCategory(categoryMapper.selectById(article.getCategoryId()));
            }
            if (article.getUserId() != null) {
                article.setAuthor(userMapper.selectById(article.getUserId()));
            }
            // Populate tags
            LambdaQueryWrapper<ArticleTag> tagWrapper = new LambdaQueryWrapper<>();
            tagWrapper.eq(ArticleTag::getArticleId, article.getId());
            List<ArticleTag> articleTags = articleTagMapper.selectList(tagWrapper);
            if (!articleTags.isEmpty()) {
                List<Long> tagIds = articleTags.stream().map(ArticleTag::getTagId).collect(Collectors.toList());
                List<Tag> tags = tagMapper.selectBatchIds(tagIds);
                article.setTags(tags);
            }
        }
        return result;
    }

    @Override
    public Article getByIdWithTags(Long id) {
        return this.getById(id);
    }

    @Override
    public void incrementViewCount(Long id) {
        Article article = this.getById(id);
        if (article != null) {
            article.setViewCount(article.getViewCount() == null ? 1 : article.getViewCount() + 1);
            this.updateById(article);
        }
    }

    @Override
    @Transactional
    public boolean saveArticle(Article article, List<Long> tagIds) {
        boolean saved = this.save(article);
        if (tagIds != null && !tagIds.isEmpty()) {
            for (Long tagId : tagIds) {
                ArticleTag articleTag = new ArticleTag();
                articleTag.setArticleId(article.getId());
                articleTag.setTagId(tagId);
                articleTagMapper.insert(articleTag);
            }
        }
        return saved;
    }

    @Override
    @Transactional
    public boolean updateArticle(Article article, List<Long> tagIds) {
        boolean updated = this.updateById(article);

        LambdaQueryWrapper<ArticleTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleTag::getArticleId, article.getId());
        articleTagMapper.delete(wrapper);

        if (tagIds != null && !tagIds.isEmpty()) {
            for (Long tagId : tagIds) {
                ArticleTag articleTag = new ArticleTag();
                articleTag.setArticleId(article.getId());
                articleTag.setTagId(tagId);
                articleTagMapper.insert(articleTag);
            }
        }
        return updated;
    }
}