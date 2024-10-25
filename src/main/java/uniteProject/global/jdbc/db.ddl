create table board
(
    id       bigint auto_increment
        primary key,
    title    varchar(45) null,
    writer   varchar(45) null,
    contents varchar(45) null,
    regdate  datetime    null,
    hit      int         null
);

create table dormitory
(
    id        int auto_increment
        primary key,
    dorm_name varchar(50) not null,
    gender    char        not null
);

create table meal_plan
(
    id           int auto_increment
        primary key,
    dormitory_id int  not null,
    plan_type    char not null,
    meal_fee     int  not null,
    constraint meal_plan_dormitory_id_fk
        foreign key (dormitory_id) references dormitory (id)
            on update cascade on delete cascade
);

create table members
(
    id         int auto_increment
        primary key,
    username   varchar(255) not null,
    password   varchar(255) not null,
    role       varchar(128) not null,
    created_at datetime     not null
);

create table room
(
    room_id      int auto_increment
        primary key,
    dormitory_id int      not null,
    room_number  int      not null,
    room_type    smallint not null,
    room_fee     int      not null,
    constraint room_dormitory_id_fk
        foreign key (dormitory_id) references dormitory (id)
            on update cascade on delete cascade
);

create table students
(
    id                   int auto_increment
        primary key,
    member_id            int          not null,
    name                 varchar(256) not null,
    student_number       varchar(256) not null,
    student_type         varchar(128) not null,
    major                varchar(256) not null,
    gpa                  double       not null,
    distance_from_school double       not null,
    submit_document      tinyint(1)   not null,
    constraint students_members_id_fk
        foreign key (member_id) references members (id)
            on update cascade on delete cascade
);

