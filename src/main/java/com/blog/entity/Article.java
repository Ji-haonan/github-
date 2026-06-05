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
@TableName("article")
public class Article implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String title;
    private String content;
    private String summary;
    private Long categoryId;
    private Long userId;
    private String coverImage;
    private String status;
    private Integer viewCount;
    private Integer isTop;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private Category category;

    @TableField(exist = false)
    private User author;

    @TableField(exist = false)
    private List<Tag> tags;
}