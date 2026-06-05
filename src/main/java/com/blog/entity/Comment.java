package com.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("comment")
public class Comment implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String content;
    private Long articleId;
    private Long userId;
    private Long parentId;
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private User user;

    @TableField(exist = false)
    private List<Comment> replies;
}