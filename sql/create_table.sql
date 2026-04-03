create database if not exists `z_picture` default character set utf8mb4 collate utf8mb4_unicode_ci;

-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin',
    editTime     datetime     default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    UNIQUE KEY uk_userAccount (userAccount),
    INDEX idx_userName (userName)
) comment '用户' collate = utf8mb4_unicode_ci;


-- 图片表
create table if not exists picture
(
    id           bigint auto_increment comment 'id' primary key,
    url          varchar(512)                       not null comment '图片 url',
    name         varchar(128)                       not null comment '图片名称',
    introduction varchar(512)                       null comment '简介',
    category     varchar(64)                        null comment '分类',
    tags         varchar(512)                      null comment '标签（JSON 数组）',
    picSize      bigint                             null comment '图片体积',
    picWidth     int                                null comment '图片宽度',
    picHeight    int                                null comment '图片高度',
    picScale     double                             null comment '图片宽高比例',
    picFormat    varchar(32)                        null comment '图片格式',
    userId       bigint                             not null comment '创建用户 id',
    createTime   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    editTime     datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    updateTime   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint  default 0                 not null comment '是否删除',
    INDEX idx_name (name),                 -- 提升基于图片名称的查询性能
    INDEX idx_introduction (introduction), -- 用于模糊搜索图片简介
    INDEX idx_category (category),         -- 提升基于分类的查询性能
    INDEX idx_tags (tags),                 -- 提升基于标签的查询性能
    INDEX idx_userId (userId)              -- 提升基于用户 ID 的查询性能
) comment '图片' collate = utf8mb4_unicode_ci;

ALTER TABLE picture
    -- 添加新列
    ADD COLUMN reviewStatus INT DEFAULT 0 NOT NULL COMMENT '审核状态：0-待审核; 1-通过; 2-拒绝',
    ADD COLUMN reviewMessage VARCHAR(512) NULL COMMENT '审核信息',
    ADD COLUMN reviewerId BIGINT NULL COMMENT '审核人 ID',
    ADD COLUMN reviewTime DATETIME NULL COMMENT '审核时间';

-- 创建基于 reviewStatus 列的索引
CREATE INDEX idx_reviewStatus ON picture (reviewStatus);

ALTER TABLE picture
    -- 添加新列
    ADD COLUMN thumbnailUrl varchar(512) NULL COMMENT '缩略图 url';


-- 空间表
create table if not exists space
(
    id         bigint auto_increment comment 'id' primary key,
    spaceName  varchar(128)                       null comment '空间名称',
    spaceLevel int      default 0                 null comment '空间级别：0-普通版 1-专业版 2-旗舰版',
    maxSize    bigint   default 0                 null comment '空间图片的最大总大小',
    maxCount   bigint   default 0                 null comment '空间图片的最大数量',
    totalSize  bigint   default 0                 null comment '当前空间下图片的总大小',
    totalCount bigint   default 0                 null comment '当前空间下的图片数量',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    editTime   datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除',
    -- 索引设计
    index idx_userId (userId),        -- 提升基于用户的查询效率
    index idx_spaceName (spaceName),  -- 提升基于空间名称的查询效率
    index idx_spaceLevel (spaceLevel) -- 提升按空间级别查询的效率
) comment '空间' collate = utf8mb4_unicode_ci;

-- 添加新列
ALTER TABLE picture
    ADD COLUMN spaceId  bigint  null comment '空间 id（为空表示公共空间）';

-- 创建索引
CREATE INDEX idx_spaceId ON picture (spaceId);

-- 添加新列
ALTER TABLE picture
    ADD COLUMN picColor varchar(16) null comment '图片主色调';

-- 添加新列
ALTER TABLE space
    ADD COLUMN spaceType int default 0 not null comment '空间类型：0-私有 1-团队';
CREATE INDEX idx_spaceType ON space (spaceType);


-- 空间成员表
create table if not exists space_user
(
    id         bigint auto_increment comment 'id' primary key,
    spaceId    bigint                                 not null comment '空间 id',
    userId     bigint                                 not null comment '用户 id',
    spaceRole  varchar(128) default 'viewer'          null comment '空间角色：viewer/editor/admin',
    createTime datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    -- 索引设计
    UNIQUE KEY uk_spaceId_userId (spaceId, userId), -- 唯一索引，用户在一个空间中只能有一个角色
    INDEX idx_spaceId (spaceId),                    -- 提升按空间查询的性能
    INDEX idx_userId (userId)                       -- 提升按用户查询的性能
) comment '空间用户关联' collate = utf8mb4_unicode_ci;


-- 空间邀请表
create table if not exists space_invite
(
    id            bigint auto_increment comment 'id' primary key,
    spaceId       bigint                                 not null comment '空间 id',
    inviterId     bigint                                 not null comment '邀请人 id',
    inviteeId     bigint                                 not null comment '被邀请人 id',
    spaceRole     varchar(128) default 'viewer'          not null comment '邀请角色：viewer/editor',
    inviteMessage varchar(512)                           null comment '邀请说明',
    inviteStatus  tinyint      default 0                 not null comment '邀请状态：0-待处理 1-已接受 2-已拒绝 3-已取消',
    handleTime    datetime                               null comment '处理时间',
    createTime    datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime    datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    INDEX idx_spaceId (spaceId),
    INDEX idx_inviteeId (inviteeId),
    INDEX idx_inviteStatus (inviteStatus)
) comment '空间邀请' collate = utf8mb4_unicode_ci;


-- 图片 AI 任务表
create table if not exists picture_ai_task
(
    id             bigint auto_increment comment 'id' primary key,
    pictureId      bigint                                 not null comment '图片 id',
    userId         bigint                                 not null comment '用户 id',
    taskType       varchar(64)  default 'out_painting'    not null comment '任务类型',
    externalTaskId varchar(128)                           not null comment '外部任务 id',
    taskStatus     tinyint      default 1                 not null comment '任务状态：0-待处理 1-处理中 2-成功 3-失败',
    requestParams  varchar(1024)                          null comment '任务参数',
    resultUrl      varchar(512)                           null comment '结果图地址',
    errorMessage   varchar(512)                           null comment '失败信息',
    finishTime     datetime                               null comment '完成时间',
    createTime     datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime     datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    UNIQUE KEY uk_externalTaskId (externalTaskId),
    INDEX idx_pictureId (pictureId),
    INDEX idx_userId (userId),
    INDEX idx_taskStatus (taskStatus)
) comment '图片 AI 任务' collate = utf8mb4_unicode_ci;


-- 图片举报表
create table if not exists picture_report
(
    id               bigint auto_increment comment 'id' primary key,
    pictureId        bigint                                 not null comment '图片 id',
    reporterId       bigint                                 not null comment '举报人 id',
    reportReasonType varchar(64)                            not null comment '举报类型',
    reportReasonText varchar(512)                           null comment '举报说明',
    reportStatus     tinyint      default 0                 not null comment '处理状态：0-待处理 1-举报成立 2-举报驳回',
    processorId      bigint                                 null comment '处理人 id',
    processResult    varchar(512)                           null comment '处理结果',
    processTime      datetime                               null comment '处理时间',
    createTime       datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime       datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    INDEX idx_pictureId (pictureId),
    INDEX idx_reporterId (reporterId),
    INDEX idx_reportStatus (reportStatus)
) comment '图片举报' collate = utf8mb4_unicode_ci;


-- 图片标签表
create table if not exists picture_tag
(
    id         bigint auto_increment comment 'id' primary key,
    tagName    varchar(64)                            not null comment '标签名称',
    createTime datetime default CURRENT_TIMESTAMP     not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP     not null on update CURRENT_TIMESTAMP comment '更新时间',
    UNIQUE KEY uk_tagName (tagName)
) comment '图片标签字典' collate = utf8mb4_unicode_ci;


-- 图片分类表
create table if not exists picture_category
(
    id           bigint auto_increment comment 'id' primary key,
    categoryName varchar(64)                            not null comment '分类名称',
    createTime   datetime default CURRENT_TIMESTAMP     not null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP     not null on update CURRENT_TIMESTAMP comment '更新时间',
    UNIQUE KEY uk_categoryName (categoryName)
) comment '图片分类字典' collate = utf8mb4_unicode_ci;


-- 空间公告表
create table if not exists space_notice
(
    id         bigint auto_increment comment 'id' primary key,
    spaceId    bigint                                 not null comment '空间 id',
    userId     bigint                                 not null comment '发布人 id',
    title      varchar(128)                           not null comment '公告标题',
    content    varchar(1024)                          not null comment '公告内容',
    isPinned   tinyint      default 0                 not null comment '是否置顶：0-否 1-是',
    createTime datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    INDEX idx_spaceId (spaceId),
    INDEX idx_isPinned (isPinned)
) comment '空间公告' collate = utf8mb4_unicode_ci;


-- 初始化默认标签
insert ignore into picture_tag (tagName)
values ('风光'),
       ('人文'),
       ('城市'),
       ('艺术'),
       ('游戏'),
       ('动物'),
       ('植物'),
       ('抽象'),
       ('明星'),
       ('动漫感');


-- 初始化默认分类
insert ignore into picture_category (categoryName)
values ('静物'),
       ('动态'),
       ('特别'),
       ('极简'),
       ('复古'),
       ('特写'),
       ('航拍'),
       ('天气'),
       ('光影'),
       ('夜色'),
       ('色彩');

