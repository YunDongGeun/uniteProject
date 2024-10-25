-- auto-generated definition
create table member
(
    id         int auto_increment
        primary key,
    username   varchar(255) not null,
    password   varchar(255) not null,
    role       varchar(128) not null,
    created_at datetime     not null
);
-- auto-generated definition
create table meal_plan
(
    meal_plan_id int auto_increment
        primary key,
    dorm_name    varchar(50) not null,
    plan_type    char        not null,
    meal_fee     int         not null
);
-- auto-generated definition
create table room
(
    room_id     int auto_increment
        primary key,
    dorm_name   varchar(50) not null,
    gender      char        not null,
    room_number int         not null,
    room_type   smallint    not null,
    room_fee    int         not null
);
-- auto-generated definition
create table student
(
    student_id           int auto_increment
        primary key,
    member_id            int          not null,
    name                 varchar(256) not null,
    student_number       varchar(256) not null,
    student_type         varchar(128) not null,
    major                varchar(256) not null,
    gpa                  double       not null,
    distance_from_school double       not null,
    submit_document      tinyint(1)   not null,
    constraint student_member_id_fk
        foreign key (member_id) references member (id)
            on update cascade on delete cascade
);

