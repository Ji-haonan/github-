package com.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.entity.Comment;
import com.blog.entity.User;
import com.blog.mapper.CommentMapper;
import com.blog.mapper.UserMapper;
import com.blog.service.CommentService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    private final UserMapper userMapper;

    public CommentServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public List<Comment> getCommentsByArticleId(Long articleId) {
        // Get top-level comments
        List<Comment> allComments = this.lambdaQuery()
                .eq(Comment::getArticleId, articleId)
                .orderByDesc(Comment::getCreateTime)
                .list();

        // Populate user info for each comment
        for (Comment comment : allComments) {
            if (comment.getUserId() != null) {
                User user = userMapper.selectById(comment.getUserId());
                comment.setUser(user);
            }
        }

        // Separate top-level comments and replies
        List<Comment> topLevel = new ArrayList<>();
        Map<Long, List<Comment>> replyMap = new HashMap<>();
        for (Comment comment : allComments) {
            if (comment.getParentId() == null) {
                topLevel.add(comment);
            } else {
                replyMap.computeIfAbsent(comment.getParentId(), k -> new ArrayList<>()).add(comment);
            }
        }

        // Attach replies to parent comments
        for (Comment parent : topLevel) {
            parent.setReplies(replyMap.getOrDefault(parent.getId(), new ArrayList<>()));
        }

        return topLevel;
    }
}