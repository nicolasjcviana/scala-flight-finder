create table city (
    code     varchar(3)  not null,
    name     varchar(50) not null
);
alter table city add constraint city_pk primary key (code);

create table airport (
    code     varchar(3)  not null,
    name     varchar(50) not null
);
alter table airport add constraint airport_pk primary key (code);

