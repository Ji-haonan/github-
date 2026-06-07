package com.blog.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.entity.*;
import com.blog.service.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ArticleService articleService;
    private final CategoryService categoryService;
    private final TagService tagService;
    private final CommentService commentService;
    private final UserService userService;

    public AdminController(ArticleService articleService, CategoryService categoryService,
                           TagService tagService, CommentService commentService, UserService userService) {
        this.articleService = articleService;
        this.categoryService = categoryService;
        this.tagService = tagService;
        this.commentService = commentService;
        this.userService = userService;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            String username = auth.getName();
            return userService.findByUsername(username);
        }
        throw new RuntimeException("用户未登录");
    }

    private void checkAdmin() {
        User user = getCurrentUser();
        if (user == null || !"ADMIN".equals(user.getRole())) {
            throw new RuntimeException("无权限访问");
        }
    }

    @GetMapping("")
    public String dashboard(Model model) {
        checkAdmin();
        long articleCount = articleService.count();
        long commentCount = commentService.count();
        long userCount = userService.count();
        long viewCount = 0;
        List<Article> allArticles = articleService.list();
        for (Article a : allArticles) {
            if (a.getViewCount() != null) viewCount += a.getViewCount();
        }
        model.addAttribute("articleCount", articleCount);
        model.addAttribute("commentCount", commentCount);
        model.addAttribute("userCount", userCount);
        model.addAttribute("viewCount", viewCount);
        return "admin/dashboard";
    }

    @GetMapping("/articles")
    public String articles(@RequestParam(defaultValue = "1") int pageNum,
                           @RequestParam(defaultValue = "10") int pageSize,
                           Model model) {
        checkAdmin();
        Page<Article> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Article::getCreateTime);
        IPage<Article> articles = articleService.page(page, wrapper);
        model.addAttribute("articles", articles);
        return "admin/article-list";
    }

    @GetMapping("/article/new")
    public String newArticleForm(Model model) {
        checkAdmin();
        List<Category> categories = categoryService.list();
        List<Tag> tags = tagService.list();
        model.addAttribute("categories", categories);
        model.addAttribute("tags", tags);
        return "admin/article-form";
    }

    @PostMapping("/article")
    public String saveArticle(@RequestParam String title,
                              @RequestParam String content,
                              @RequestParam String summary,
                              @RequestParam Long categoryId,
                              @RequestParam(required = false) String coverImage,
                              @RequestParam(defaultValue = "DRAFT") String status,
                              @RequestParam(required = false) String tagIds) {
        checkAdmin();
        User user = getCurrentUser();

        Article article = new Article();
        article.setTitle(title);
        article.setContent(content);
        article.setSummary(summary);
        article.setCategoryId(categoryId);
        article.setCoverImage(coverImage);
        article.setStatus(status);
        article.setUserId(user.getId());
        article.setViewCount(0);
        article.setIsTop(0);

        List<Long> tagIdList = null;
        if (tagIds != null && !tagIds.trim().isEmpty()) {
            tagIdList = Arrays.stream(tagIds.split(","))
                    .map(String::trim)
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
        }

        articleService.saveArticle(article, tagIdList);
        return "redirect:/admin/articles";
    }

    @GetMapping("/article/edit/{id}")
    public String editArticleForm(@PathVariable Long id, Model model) {
        checkAdmin();
        Article article = articleService.getById(id);
        if (article == null) {
            return "redirect:/admin/articles";
        }

        List<Category> categories = categoryService.list();
        List<Tag> tags = tagService.list();
        List<Tag> articleTags = tagService.getTagsByArticleId(id);

        model.addAttribute("article", article);
        model.addAttribute("categories", categories);
        model.addAttribute("tags", tags);
        model.addAttribute("articleTags", articleTags);
        return "admin/article-form";
    }

    @PostMapping("/article/update")
    public String updateArticle(@RequestParam Long id,
                                @RequestParam String title,
                                @RequestParam String content,
                                @RequestParam String summary,
                                @RequestParam Long categoryId,
                                @RequestParam(required = false) String coverImage,
                                @RequestParam(defaultValue = "DRAFT") String status,
                                @RequestParam(required = false) String tagIds) {
        checkAdmin();

        Article article = articleService.getById(id);
        if (article == null) {
            return "redirect:/admin/articles";
        }

        article.setTitle(title);
        article.setContent(content);
        article.setSummary(summary);
        article.setCategoryId(categoryId);
        article.setCoverImage(coverImage);
        article.setStatus(status);

        List<Long> tagIdList = null;
        if (tagIds != null && !tagIds.trim().isEmpty()) {
            tagIdList = Arrays.stream(tagIds.split(","))
                    .map(String::trim)
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
        }

        articleService.updateArticle(article, tagIdList);
        return "redirect:/admin/articles";
    }

    @GetMapping("/article/delete/{id}")
    public String deleteArticle(@PathVariable Long id) {
        checkAdmin();
        articleService.removeById(id);
        return "redirect:/admin/articles";
    }

    @GetMapping("/comments")
    public String comments(@RequestParam(defaultValue = "1") int pageNum,
                           @RequestParam(defaultValue = "10") int pageSize,
                           Model model) {
        checkAdmin();
        Page<Comment> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Comment::getCreateTime);
        IPage<Comment> comments = commentService.page(page, wrapper);
        model.addAttribute("comments", comments);
        return "admin/comments";
    }

    @GetMapping("/comment/delete/{id}")
    public String deleteComment(@PathVariable Long id) {
        checkAdmin();
        commentService.removeById(id);
        return "redirect:/admin/comments";
    }

    @GetMapping("/categories")
    public String categories(Model model) {
        checkAdmin();
        List<Category> categories = categoryService.list();
        model.addAttribute("categories", categories);
        return "admin/categories";
    }

    @PostMapping("/category")
    public String addCategory(@RequestParam String name,
                              @RequestParam(required = false) String description,
                              @RequestParam(defaultValue = "0") Integer sort) {
        checkAdmin();
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        category.setSort(sort);
        categoryService.save(category);
        return "redirect:/admin/categories";
    }

    @GetMapping("/category/delete/{id}")
    public String deleteCategory(@PathVariable Long id) {
        checkAdmin();
        categoryService.removeById(id);
        return "redirect:/admin/categories";
    }
}