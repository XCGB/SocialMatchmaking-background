-- auto-generated definition
create table user
(
    id            bigint auto_increment comment 'id'
        primary key,
    username      varchar(256)                       null comment '用户昵称',
    user_account  varchar(256)                       null comment '用户账号',
    user_password varchar(512)                       not null comment '用户密码',
    avatar_url    varchar(1024)                      null comment '用户头像',
    profile       varchar(512)                       null comment '个人简介',
    gender        tinyint  default 0                 null comment '性别',
    phone         varchar(128)                       null comment '电话',
    email         varchar(256)                       null comment '邮箱',
    tags          varchar(1024)                      null comment '标签列表',
    user_status   int      default 0                 not null comment '用户状态 0-正常',
    user_role     int      default 0                 not null comment '用户鉴权 0-默认用户  1-管理员',
    is_delete     tinyint  default 0                 not null comment '逻辑删除 0- 1',
    gmt_create    datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    gmt_modified  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '修改时间'
)
    comment '用户表';

-- auto-generated definition
create table team
(
    id           bigint auto_increment comment 'id'
        primary key,
    name         varchar(256)                       not null comment '队伍名称',
    description  varchar(1024)                      null comment '描述',
    max_num      int      default 1                 not null comment '最大人数',
    user_id      bigint                             not null comment '用户id',
    password     varchar(512)                       null comment '密码',
    status       int      default 0                 not null comment '0 - 公开，1 - 私有，2 - 加密',
    expire_time  datetime                           null comment '过期时间',
    is_delete    tinyint  default 0                 not null comment '是否删除',
    gmt_create   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    gmt_modified datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP
)
    comment '队伍';

-- auto-generated definition
create table user_team
(
    id           bigint auto_increment comment 'id'
        primary key,
    user_id      bigint                             null comment '用户id',
    team_id      bigint                             null comment '队伍id',
    join_time    datetime                           null comment '加入时间',
    is_delete    tinyint  default 0                 not null comment '是否删除',
    gmt_create   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    gmt_modified datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP
)
    comment '用户队伍关系';