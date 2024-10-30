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

create table payment
(
    id             int auto_increment
        primary key,
    application_id int not null
);

create table period
(
    id                     int auto_increment
        primary key,
    application_start      datetime not null,
    announcement           datetime not null,
    payment_deadline       datetime not null,
    document_deadline      datetime not null,
    additional_application datetime not null
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

create table application
(
    id             int auto_increment
        primary key,
    student_id     int                      not null,
    status         varchar(50) default '대기' not null comment '''대기'' ''검토'' ''승인'' ''거부''',
    priority_score int                      not null,
    created_at     datetime                 not null,
    update_at      datetime                 null,
    constraint application_students_id_fk
        foreign key (student_id) references students (id)
            on update cascade on delete cascade
);

