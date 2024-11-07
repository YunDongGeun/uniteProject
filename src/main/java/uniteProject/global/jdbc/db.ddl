create table dormitory
(
    id        int auto_increment
        primary key,
    dorm_name varchar(50) not null,
    gender    char        not null,
    constraint unique_dorm_name
        unique (dorm_name)
);

create table fee_management
(
    id        int auto_increment
        primary key,
    dorm_name varchar(50) not null,
    fee_type  varchar(20) not null,
    amount    int         not null,
    constraint fk_dorm_name
        foreign key (dorm_name) references dormitory (dorm_name),
    check (`fee_type` in ('ROOM_2', 'ROOM_4', 'MEAL_0', 'MEAL_5', 'MEAL_7'))
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

create table payment
(
    id             int auto_increment
        primary key,
    application_id int         not null,
    amount         int         not null,
    payment_status varchar(20) not null,
    payment_date   datetime    null
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
    id           int auto_increment
        primary key,
    dormitory_id int         not null,
    room_number  int         not null,
    room_type    smallint    not null check(`room_type` in (2, 4)),
    constraint unique_room
        unique (dormitory_id, room_number),
    constraint room_dormitory_id_fk
        foreign key (dormitory_id) references dormitory (id)
            on update cascade on delete cascade
);

create table room_status
(
    id           int auto_increment
        primary key,
    room_id      int         not null,
    student_id   int         null, -- 아직 배정되지 않은 경우 null
    bed_number   varchar(10) not null,
    constraint room_status_room_id_fk
        foreign key (room_id) references room (id)
            on update cascade on delete cascade,
    constraint room_status_student_id_fk
        foreign key (student_id) references students (id)
            on update cascade on delete set null
);

create table account
(
    id             int auto_increment
        primary key,
    student_id     int          not null,
    account_number varchar(256) not null,
    bank_name      varchar(128) not null,
    constraint account_students_id_fk
        foreign key (student_id) references students (id)
            on update cascade on delete cascade
);

-- auto-generated definition
create table application
(
    id             int auto_increment
        primary key,
    student_id     int                      not null,
    status         varchar(50) default '대기' not null comment '''대기'' ''검토'' ''승인'' ''거부''',
    priority_score int                      not null,
    is_paid        tinyint(1)  default 0    not null comment '결제 여부 (0: 미결제, 1: 결제 완료)',
    preference     tinyint(1)               null comment '지원 우선순위 (1: 1지망, 2: 2지망)',
    created_at     datetime                 not null,
    update_at      datetime                 null,
    constraint application_students_id_fk
        foreign key (student_id) references unitedb.students (id)
            on update cascade on delete cascade,
    check (`preference` in (1, 2))
);

create table tb_certificate
(
    id               int auto_increment
        primary key,
    application_id   int                      not null,
    image            blob                     not null comment '결핵 진단서 이미지 파일',
    uploaded_at      datetime                 not null default current_timestamp,
    constraint tb_certificate_application_id_fk
        foreign key (application_id) references application (id)
            on update cascade on delete cascade
);

create table withdrawal
(
    id             int auto_increment
        primary key,
    student_id     int          not null,
    leave_date     datetime     not null,
    status         varchar(50) default '대기' not null comment '''대기'' ''검토'' ''승인'' ''거부''',
    refund_amount  int          not null,
    constraint withdrawal_students_id_fk
        foreign key (student_id) references students (id)
            on update cascade on delete cascade
);
