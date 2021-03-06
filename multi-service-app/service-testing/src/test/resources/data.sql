drop table if exists address;
drop table if exists payment;
drop table if exists user;

create table user
(
    id        bigint auto_increment
        primary key,
    birthday  date         null,
    email     varchar(255) null,
    last_name varchar(255) null,
    name      varchar(255) null
);

create table address
(
    id       bigint auto_increment
        primary key,
    city     varchar(255) null,
    line_one varchar(255) null,
    line_two varchar(255) null,
    state    varchar(255) null,
    user_id  bigint       null,
    zip      varchar(255) null
);

create table payment
(
    id           bigint auto_increment
        primary key,
    card_number  varchar(255) null,
    cardholder   varchar(255) null,
    cvv          varchar(255) null,
    expiry_month int          null,
    expiry_year  int          null,
    token        varchar(255) null,
    user_id      bigint       null
);

insert into user (id, birthday, email, last_name, name)
values (1, '1980-04-24', 'example-email1@gmail.com', 'Hofstadter', 'Leonard'),
       (2, '1981-05-14', 'example-email2@gmail.com', 'Wollowitz', 'Howard'),
       (3, '1983-01-02', 'example-email3@gmail.com', 'Cooper', 'Sheldon'),
       (4, '1982-03-14', 'example-email4@gmail.com', 'Kudhrapali', 'Rajesh');
insert into address (id, city, line_one, line_two, state, user_id, zip)
values (1, 'Pasadena', 'West Boulevard', '123', 'CA', 1, '88040'),
       (2, 'San Francisco', 'Bumpy Road', '1024', 'CA', 2, '89356'),
       (3, 'San Diego', 'Mexico Way', '34', 'CA', 3, '11876');
insert into payment (id, card_number, cardholder, cvv, expiry_month, expiry_year, token, user_id)
values (1, '1111222233334444', 'Leonard Hofstadter', '927', 4, 2023, 'token123', 1),
       (2, '2222333344445555', 'Howard Wollowitz', '279', 6, 2022, 'token456', 2),
       (3, '3333444455556666', 'Sheldon Cooper', '792', 11, 2024, 'token789', 3);