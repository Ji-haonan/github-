package com.blog.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.entity.Article;
import com.blog.entity.Category;
import com.blog.entity.Comment;
import com.blog.entity.Tag;
import com.blog.entity.User;
import com.blog.service.ArticleService;
import com.blog.service.CategoryService;
import com.blog.service.CommentService;
import com.blog.service.TagService;
import com.blog.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class IndexController {

    private final ArticleService articleService;
    private final CategoryService categoryService;
    private final TagService tagService;
    private final CommentService commentService;
    private final UserService userService;

    public IndexController(ArticleService articleService, CategoryService categoryService,
                           TagService tagService, CommentService commentService,
                           UserService userService) {
        this.articleService = articleService;
        this.categoryService = categoryService;
        this.tagService = tagService;
        this.commentService = commentService;
        this.userService = userService;
    }

    @GetMapping("/")
    public String index(@RequestParam(required = false) String keyword,
                        @RequestParam(defaultValue = "1") int page,
                        Model model) {
        IPage<Article> articles;
        if (keyword != null && !keyword.trim().isEmpty()) {
            Page<Article> pageObj = new Page<>(page, 10);
            LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Article::getStatus, "PUBLISHED");
            wrapper.and(w -> w.like(Article::getTitle, keyword).or().like(Article::getSummary, keyword));
            wrapper.orderByDesc(Article::getIsTop).orderByDesc(Article::getCreateTime);
            articles = articleService.page(pageObj, wrapper);
            model.addAttribute("keyword", keyword);
        } else {
            articles = articleService.pageWithCategory(page, 10, null);
        }
        List<Category> categories = categoryService.list();
        List<Article> recentArticles = articleService.pageWithCategory(1, 5, null).getRecords();

        model.addAttribute("articles", articles);
        model.addAttribute("categories", categories);
        model.addAttribute("recentArticles", recentArticles);
        return "index";
    }

    @GetMapping("/article/{id}")
    public String articleDetail(@PathVariable Long id, Model model) {
        Article article = articleService.getByIdWithTags(id);
        if (article == null) {
            return "redirect:/";
        }

        articleService.incrementViewCount(id);

        List<Tag> tags = tagService.getTagsByArticleId(id);
        List<Comment> comments = commentService.getCommentsByArticleId(id);
        Category category = null;
        if (article.getCategoryId() != null) {
            category = categoryService.getById(article.getCategoryId());
        }

        model.addAttribute("article", article);
        model.addAttribute("tags", tags);
        model.addAttribute("comments", comments);
        model.addAttribute("category", category);
        return "article/detail";
    }

    @GetMapping("/category/{id}")
    public String categoryArticles(@PathVariable Long id,
                                   @RequestParam(defaultValue = "1") int page,
                                   Model model) {
        IPage<Article> articles = articleService.pageWithCategory(page, 10, id);
        List<Category> categories = categoryService.list();
        Category currentCategory = categoryService.getById(id);

        model.addAttribute("articles", articles);
        model.addAttribute("categories", categories);
        model.addAttribute("currentCategory", currentCategory);
        return "category";
    }

    @PostMapping("/article/{id}/comment")
    public String postComment(@PathVariable Long id,
                              @RequestParam String content,
                              @RequestParam(required = false) Long parentId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        String username = auth.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            return "redirect:/login";
        }

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setArticleId(id);
        comment.setUserId(user.getId());
        comment.setParentId(parentId);
        commentService.save(comment);
        return "redirect:/article/" + id;
    }
}