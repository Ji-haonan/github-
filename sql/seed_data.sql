-- =====================================================
-- MyBlog 示例数据 - 10 篇博客文章 + 评论 + 标签
-- 初始化方式：
--   mysql -uroot -p < schema.sql
--   mysql -uroot -p blog < seed_data.sql
-- =====================================================

-- 标签数据
INSERT INTO tag (id, name, create_time) VALUES
(1, 'Java', NOW()),
(2, 'Spring Boot', NOW()),
(3, 'MyBatis-Plus', NOW()),
(4, 'Thymeleaf', NOW()),
(5, '前端', NOW()),
(6, '教程', NOW()),
(7, '思考', NOW()),
(8, '随笔', NOW()),
(9, '旅行', NOW()),
(10, '美食', NOW()),
(11, '读书', NOW()),
(12, '工具', NOW()),
(13, '效率', NOW()),
(14, '架构', NOW()),
(15, '数据库', NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 10 篇示例文章
INSERT INTO article (title, summary, content, category_id, user_id, cover_image, status, view_count, is_top, create_time, update_time) VALUES
('Spring Boot 3 实战：从零搭建现代化博客系统',
 '从架构设计、技术选型到代码实现，全方位讲解如何用 Spring Boot 3 + MyBatis-Plus + Thymeleaf 搭建一个生产级博客系统。',
 '<h2>前言</h2><p>经过三个月的打磨，我用 <strong>Spring Boot 3.2</strong> + <strong>MyBatis-Plus 3.5.5</strong> + <strong>Thymeleaf 3.1.2</strong> 搭建了一套完整的博客系统。这篇文章把整个开发过程的关键决策、技术坑点、最佳实践都整理出来。</p><h2>技术选型</h2><ul><li><strong>后端框架</strong>: Spring Boot 3.2.0（基于 Spring Framework 6.1）</li><li><strong>ORM</strong>: MyBatis-Plus 3.5.5（增强型 MyBatis）</li><li><strong>模板引擎</strong>: Thymeleaf 3.1.2（服务端渲染）</li><li><strong>数据库</strong>: MySQL 8.0</li><li><strong>连接池</strong>: HikariCP（Spring Boot 默认）</li><li><strong>安全</strong>: Spring Security 6.x</li></ul><h2>核心架构</h2><blockquote><p>好的架构不是设计出来的，是演化出来的。</p></blockquote><p>整个项目采用经典的三层架构：</p><pre><code>Controller → Service → Mapper → Database</code></pre><h2>踩过的坑</h2><p>1. <strong>Lombok 与 JDK 25 兼容</strong>：Lombok 1.18.30 还不支持 JDK 25，必须移除所有 @Data @Builder 注解</p><p>2. <strong>Spring Framework 6.1.1 + JDK 17.0.19+</strong>：FactoryBeanRegistrySupport 字节码不兼容，需要修补</p><p>3. <strong>Thymeleaf 嵌套表达式</strong>：<code>${x} != null</code> 是错的，必须写 <code>${x != null}</code></p><h2>总结</h2><p>这套系统跑起来很顺，启动只要 4 秒，QPS 能到 2000+。下次准备加 Redis 缓存和全文检索。</p>',
 1, 1, 'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=800', 'PUBLISHED', 1245, 1, '2026-06-01 10:30:00', NOW()),

('MyBatis-Plus 分页插件的 5 个最佳实践',
 '分页看似简单，但 90% 的开发者都没用对。本文分享 MyBatis-Plus 分页插件的正确打开方式。',
 '<h2>为什么分页这么重要？</h2><p>不分页的列表查询，<strong>100 万条数据直接 OOM</strong>。分页是后端工程师的基本功。</p><h2>5 个最佳实践</h2><h3>1. 必须配置分页插件</h3><pre><code>@Bean
public MybatisPlusInterceptor mybatisPlusInterceptor() {
    MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
    interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
    return interceptor;
}</code></pre><h3>2. 用 IPage 而不是 List</h3><p><code>page</code> 方法返回的是 <code>IPage&lt;T&gt;</code>，包含 records、total、current、size、pages 等完整信息。</p><h3>3. 前端需要的 total 来自 .total，不是 .size</h3><p>这是新手最容易踩的坑。记录数是 <code>total</code>，每页大小是 <code>size</code>，总页数是 <code>pages</code>。</p><h3>4. 复杂查询用 LambdaQueryWrapper</h3><pre><code>LambdaQueryWrapper&lt;Article&gt; wrapper = new LambdaQueryWrapper&lt;&gt;();
wrapper.eq(Article::getStatus, "PUBLISHED")
       .like(Article::getTitle, keyword)
       .orderByDesc(Article::getCreateTime);</code></pre><h3>5. 大表分页优化</h3><p>深分页（page &gt; 1000）建议用游标分页或 ES 搜索。</p><h2>写在最后</h2><p>分页用得好，并发能翻倍；分页用得差，老板要加班。</p>',
 1, 1, 'https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=800', 'PUBLISHED', 892, 0, '2026-05-28 14:20:00', NOW()),

('Thymeleaf 模板引擎避坑指南',
 '从嵌套表达式到安全漏洞，Thymeleaf 新手最容易踩的 8 个坑。',
 '<h2>Thymeleaf 的哲学</h2><p>Thymeleaf 是服务端模板引擎，主张<strong>自然模板</strong>：HTML 可以在浏览器直接打开预览，由 Thymeleaf 处理后才是动态页面。</p><h2>8 个常见坑</h2><h3>坑 1：嵌套 <code>${}</code></h3><pre><code>❌ ${user.${prop}}
✅ ${user[prop]}</code></pre><h3>坑 2：转义 vs 不转义</h3><p><code>th:text</code> 会转义 HTML，<code>th:utext</code> 不会。后台存的文章内容要用 utext。</p><h3>坑 3：空指针</h3><p><code>${user.name}</code> 在 user 为 null 时会抛异常，必须用三元表达式或 <code>${user?.name}</code>（Safe Navigation Operator）。</p><h3>坑 4：布尔判断</h3><pre><code>❌ ${flag == true}
✅ ${flag}</code></pre><h2>工具推荐</h2><ul><li>IDEA 插件：Thymeleaf 插件（语法高亮+提示）</li><li>在线文档：thymeleaf.org</li></ul>',
 1, 1, 'https://images.unsplash.com/photo-1542831371-29b0f74f9713?w=800', 'PUBLISHED', 567, 0, '2026-05-25 09:15:00', NOW()),

('从单体到微服务：我踩过的架构演进 3 个大坑',
 '系统从 0 到 100 万用户的演进历程，以及我在分库分表、缓存、消息队列上踩过的真实坑。',
 '<h2>背景</h2><p>我们团队从 2019 年开始做电商系统，到 2023 年日活突破 100 万。这篇文章复盘架构演进的 3 个关键节点。</p><h2>阶段 1：单体应用（0-10 万用户）</h2><p>Spring Boot 单体部署，MySQL 单库单表，Redis 缓存热点数据。这一阶段<strong>什么都不要想，堆机器就完事了</strong>。</p><h2>阶段 2：读写分离 + 分库分表（10-50 万）</h2><p>引入 MyCAT 做分库分表，按 user_id 哈希到 16 个库。坑：</p><ul><li>❌ 跨库 JOIN 直接废了，必须反范式设计</li><li>❌ 分布式事务用 XA 性能掉一半，改用最终一致性</li><li>❌ 全局 ID 不能用自增，改用 Snowflake</li></ul><h2>阶段 3：微服务化（50-100 万+）</h2><p>按业务域拆分成 20+ 服务，Spring Cloud Alibaba 全家桶。坑：</p><ul><li>❌ 服务调用链路过长，一个慢调用拖垮整个系统</li><li>❌ 配置中心推错配置，全站 502 持续 10 分钟</li><li>❌ 日志分散，排查问题要登 10 台机器</li></ul><h2>血的教训</h2><blockquote><p>微服务不是银弹，过早微服务化 = 自杀。</p></blockquote><p>建议：日活 50 万以下，别碰微服务。单体 + 模块化就够了。</p>',
 1, 1, 'https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=800', 'PUBLISHED', 1834, 0, '2026-05-20 16:45:00', NOW()),

('为什么我放弃了 VS Code，回到 IntelliJ IDEA',
 '用了 VS Code 三年，最近又回到了 IDEA。说说我的真实体验和取舍。',
 '<h2>VS Code 的辉煌</h2><p>2018-2021 这三年，我用 VS Code 写完了 5 个项目，<strong>轻量、启动快、插件丰富</strong>，是它的最大优势。</p><h2>为什么回去了？</h2><h3>1. 智能提示差距</h3><p>写 Java 时，IDEA 的智能提示、重构能力是 VS Code + 插件组合<strong>追不上的</strong>。</p><h3>2. 调试体验</h3><p>IDEA 的 Debug 工具是真的<strong>武器级</strong>：条件断点、表达式求值、Stream 调试、Lambda 反编译、内存分析。</p><h3>3. 重构能力</h3><p>改一个类名，IDEA 会找出所有引用并安全改名；VS Code？不好意思，搜索替换走起。</p><h2>VS Code 仍然无敌的场景</h2><ul><li>写前端（React/Vue/HTML）</li><li>写 Python 脚本</li><li>写 Markdown</li><li>改配置文件</li></ul><h2>我的工具链现状</h2><p>后端 Java：<strong>IDEA Ultimate</strong>；前端开发：<strong>VS Code + Volar</strong>；Markdown 写作：<strong>Typora</strong>；数据库：<strong>DataGrip</strong>。</p><h2>总结</h2><p>工具没有最好的，只有最合适的。别站队，选对的。</p>',
 1, 1, 'https://images.unsplash.com/photo-1555099962-4199c345e5dd?w=800', 'PUBLISHED', 421, 0, '2026-05-15 11:00:00', NOW()),

('成都 7 日：把我的胃和灵魂都留在了这里',
 '在成都待了 7 天，吃了 23 家馆子，逛了 8 个景点。这是一份不踩雷的深度攻略。',
 '<h2>关于成都的初印象</h2><p>赵雷那首《成都》骗了多少人。我去了才知道：<strong>成都的浪漫不是玉林路的小酒馆</strong>，是巷子里的盖碗茶、是凌晨 2 点的火锅店、是宽窄巷子拐角处那只打盹的猫。</p><h2>Day 1-2：市井烟火</h2><ul><li><strong>魁星楼街</strong>：本地人的早餐集合地</li><li><strong>建设路</strong>：电子科大学生养活的小吃街</li><li><strong>玉林路</strong>：晚上去，氛围感拉满</li></ul><h2>Day 3-4：文化巡礼</h2><p>上午 <strong>杜甫草堂</strong>，下午 <strong>武侯祠</strong>，晚上 <strong>锦里</strong>。三个地方挨得很近。</p><h2>Day 5-6：美食打卡</h2><p>火锅推荐：<strong>蜀大侠、小龙坎、电台巷</strong>；串串推荐：<strong>玉林串串、钢管厂五区</strong>；川菜推荐：<strong>陈麻婆豆腐、夫妻肺片</strong>。</p><h2>Day 7：人民公园的慢生活</h2><p>点一碗 8 块钱的盖碗茶，找个竹椅坐下，看大爷下棋、听大姐摆龙门阵。<strong>这一刻我才懂成都</strong>。</p><h2>必带物品</h2><blockquote><p>肠胃药、便携水杯、一双好走路的鞋、一颗不着急的心。</p></blockquote>',
 2, 1, 'https://images.unsplash.com/photo-1599930113854-d6d7fd521f10?w=800', 'PUBLISHED', 1567, 1, '2026-06-03 08:00:00', NOW()),

('独居一年，我学会的 5 件事',
 '一个人住，听起来很爽，住下来才知道多的是你不知道的事。',
 '<h2>为什么选择独居？</h2><p>2025 年 5 月，我搬出了合租房，开始一个人住。<strong>不为自由，为思考</strong>。</p><h2>学到的 5 件事</h2><h3>1. 做饭是生存技能，不是生活情趣</h3><p>点外卖一个月 4000，自己做饭 1500。<strong>省钱是次要的，关键是健康</strong>。三个月我的皮肤状态好了 30%。</p><h3>2. 独处不等于孤独</h3><p>刚开始我也慌，下班回家没人说话。后来我开始<strong>写日记、读书、练字</strong>，发现独处时大脑才真正在思考。</p><h3>3. 房子是租的，生活是自己的</h3><p>3000 块的出租屋，我买了 2000 块的装饰品：香薰、桌布、抱枕。<strong>仪式感不能省</strong>。</p><h3>4. 安全感要自己给</h3><p>门锁、监控、报警器，<strong>三件套必备</strong>。不是被害妄想，是成年人该有的自我保护。</p><h3>5. 独居让我重新认识自己</h3><p>没有人打扰时，我才发现自己<strong>喜欢什么、讨厌什么、想要什么</strong>。这比任何心理测试都准。</p><h2>给独居者的建议</h2><blockquote><p>把家布置成自己喜欢的样子，买一个好的枕头，养一盆绿植。</p></blockquote><p>独居不是逃避，是选择。</p>',
 2, 1, 'https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?w=800', 'PUBLISHED', 723, 0, '2026-05-22 20:30:00', NOW()),

('30 岁，我想对自己说的 10 句话',
 '30 是个坎。跨过这个数字，我想对未来 10 年的自己说这些话。',
 '<h2>写在前面</h2><p>30 岁前的最后一个晚上，我给自己写了一封信。以下是信里的 10 句话：</p><h2>10 句话</h2><p><strong>1. 身体是第一位的</strong> — 别再熬夜了，你不是 25 岁。</p><p><strong>2. 不要为了合群而合群</strong> — 高质量的独处，胜过 100 个无效社交。</p><p><strong>3. 钱很重要，但不是全部</strong> — 把 80% 的精力放在提升自己上。</p><p><strong>4. 选一个能让你"老去"的事业</strong> — 不一定要高薪，但要让你 60 岁时还能津津有味地讲出来。</p><p><strong>5. 家人永远在你身后</strong> — 别把最差的脾气留给最亲的人。</p><p><strong>6. 保持好奇</strong> — 这个年纪，最可怕的不是没钱，是<strong>对什么都没兴趣</strong>。</p><p><strong>7. 运动是抗衰老的灵药</strong> — 每周 3 次、每次 30 分钟，胜过任何保健品。</p><p><strong>8. 别停止学习</strong> — 30 岁不是终点，是新的起点。</p><p><strong>9. 理财从 30 岁开始</strong> — 复利是世界第八大奇迹。</p><p><strong>10. 学会说"不"</strong> — 你的时间和精力是有限的。</p><h2>写在最后</h2><blockquote><p>30 岁，不是青春的结束，是精彩的开场。</p></blockquote>',
 3, 1, 'https://images.unsplash.com/photo-1499209974431-9dddcece7f88?w=800', 'PUBLISHED', 2103, 1, '2026-05-30 22:00:00', NOW()),

('读书 200 本后，我推荐这 10 本',
 '2018-2025，我读了 200+ 本书。这 10 本改变了我的人生轨迹。',
 '<h2>为什么读书？</h2><p>在信息爆炸的时代，读书是<strong>性价比最高的成长方式</strong>。一年 30 本，5 年后你就和别人拉开巨大差距。</p><h2>10 本改变我的书</h2><h3>认知类</h3><p><strong>1.《思考，快与慢》- 卡尼曼</strong>：让你看清自己大脑的 bug，从此少做傻事。</p><p><strong>2.《影响力》- 西奥迪尼</strong>：搞懂 6 大说服原理，无论职场还是生活都用得上。</p><h3>财富类</h3><p><strong>3.《穷爸爸富爸爸》- 清崎</strong>：财商启蒙第一书。</p><p><strong>4.《小狗钱钱》- 博多·舍费尔</strong>：小白入门必读，<strong>我 7 岁的侄子都能读懂</strong>。</p><h3>思维类</h3><p><strong>5.《学会提问》- 尼尔·布朗</strong>：批判性思维入门。</p><p><strong>6.《刻意练习》- 安德斯·艾利克森</strong>：颠覆"一万小时定律"的神作。</p><h3>文学类</h3><p><strong>7.《活着》- 余华</strong>：读完沉默了一下午。</p><p><strong>8.《三体》- 刘慈欣</strong>：中国科幻的巅峰。</p><h3>生活类</h3><p><strong>9.《被讨厌的勇气》- 岸见一郎</strong>：阿德勒心理学入门，<strong>治愈了我的讨好型人格</strong>。</p><p><strong>10.《非暴力沟通》- 马歇尔·卢森堡</strong>：亲密关系的圣经。</p><h2>我的读书方法</h2><ol><li>选书：豆瓣 8.0 分以上 + 朋友推荐</li><li>时间：通勤 + 睡前 1 小时</li><li>笔记：印象笔记 + 卡片笔记法</li><li>输出：每本书写一篇短评</li></ol>',
 3, 1, 'https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=800', 'PUBLISHED', 1456, 0, '2026-05-18 19:00:00', NOW()),

('关于"内卷"：我们到底在卷什么？',
 '内卷是这个时代最大的谎言。说说我的看法。',
 '<h2>什么是内卷？</h2><p>内卷：<strong>无意义的竞争，让所有人更累，但没有人更幸福</strong>。</p><p>举几个例子：</p><ul><li>本来 5 点下班能完成的工作，因为大家都在加班，你也不得不加班</li><li>本来 985 学历够用，但所有人都考研，你也跟着考</li><li>本来 30 岁结婚刚好，但大家都在比谁结婚早</li></ul><h2>内卷的根源</h2><p>内卷的根源是<strong>资源的有限性</strong>和<strong>评价标准的单一化</strong>。</p><h3>1. 资源有限</h3><p>好大学、好工作、好房子，<strong>总量就那么多</strong>。大家都想要，怎么办？</p><h3>2. 标准单一</h3><p>评价一个人成功与否，<strong>只用钱和地位</strong>。于是大家都在比这两个。</p><h2>怎么破？</h2><h3>1. 重新定义成功</h3><p>成功 = 身体健康 + 心理富足 + 经济独立 + 良好关系。<strong>少一条都不算成功</strong>。</p><h3>2. 找到自己的赛道</h3><p>当所有人都去互联网时，<strong>蓝领技术工人反而稀缺</strong>。</p><h3>3. 学会拒绝</h3><p>不参加无效竞争。同事在卷加班？<strong>把工作做漂亮，按时下班</strong>。</p><h3>4. 培养"无用"的兴趣</h3><p>读书、写作、画画、钓鱼、徒步。<strong>这些事不能让你升职加薪，但能让你快乐</strong>。</p><h2>写在最后</h2><blockquote><p>别被时代的洪流裹挟，<strong>活成你自己</strong>。</p></blockquote>',
 3, 1, 'https://images.unsplash.com/photo-1517048676732-d65bc937f952?w=800', 'PUBLISHED', 892, 0, '2026-05-12 10:00:00', NOW())
ON DUPLICATE KEY UPDATE title=VALUES(title);

-- 文章-标签关联
INSERT IGNORE INTO article_tag (article_id, tag_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 6),
(2, 3), (2, 15), (2, 6),
(3, 4), (3, 5), (3, 6),
(4, 1), (4, 14), (4, 15),
(5, 12), (5, 13), (5, 7),
(6, 9), (6, 10), (6, 8),
(7, 7), (7, 13), (7, 8),
(8, 7), (8, 11), (8, 8),
(9, 11), (9, 7), (9, 8),
(10, 7), (10, 8);

-- 评论数据
INSERT INTO comment (content, article_id, user_id, parent_id, create_time) VALUES
('博主写得真好，学到了很多，期待更多干货！', 1, 1, NULL, '2026-06-01 15:30:00'),
('收藏了，这篇 Spring Boot 实战我准备照着搭一个。', 1, 1, NULL, '2026-06-02 09:15:00'),
('我也有同感，IDEA 的重构是真的强，VS Code 还得再加油。', 5, 1, NULL, '2026-05-16 10:30:00'),
('30 岁的我看完默默流泪...', 8, 1, NULL, '2026-05-31 08:00:00'),
('成都真的是去了就不想走的城市！', 6, 1, NULL, '2026-06-04 12:00:00'),
('关于内卷那段说到我心坎里了。', 10, 1, NULL, '2026-05-13 14:20:00'),
('求更新更多 MyBatis-Plus 的进阶用法！', 2, 1, NULL, '2026-05-29 11:00:00');
