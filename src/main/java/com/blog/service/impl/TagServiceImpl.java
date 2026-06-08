package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.entity.ArticleTag;
import com.blog.entity.Tag;
import com.blog.mapper.ArticleTagMapper;
import com.blog.mapper.TagMapper;
import com.blog.service.TagService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    private final ArticleTagMapper articleTagMapper;

    public TagServiceImpl(ArticleTagMapper articleTagMapper) {
        this.articleTagMapper = articleTagMapper;
    }

    @Override
    public List<Tag> getTagsByArticleId(Long articleId) {
        LambdaQueryWrapper<ArticleTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleTag::getArticleId, articleId);
        List<ArticleTag> articleTags = articleTagMapper.selectList(wrapper);
        List<Long> tagIds = articleTags.stream()
                .map(ArticleTag::getTagId)
                .collect(Collectors.toList());

        if (tagIds.isEmpty()) {
            return List.of();
        }
        return this.baseMapper.selectBatchIds(tagIds);
    }
}