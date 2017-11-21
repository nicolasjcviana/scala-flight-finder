create table city (
	code varchar(3)  not null,
	name varchar(50) not null,
	
	constraint city_pk primary key (code)
);

create table airport (
	code varchar(3)  not null,
	name varchar(50) not null,
	city varchar(3)  not null,
	
	constraint airport_pk primary key (code),
	constraint airport_city_fk foreign key (city) references city (code)
);
create index airport_city_idx on airport (city);

create table aircraft (
	code varchar(3)  not null,
	name varchar(50) not null,
	
	constraint aircraft_pk primary key (code)
);

create table tax_kind (
	code varchar(10) not null,
	name varchar(100) not null,
	
	constraint tax_kind_pk primary key (code)
);

create table company (
	code varchar(2)  not null,
	name varchar(50) not null,
	
	constraint company_pk primary key (code)
);

create table request (
    id          varchar(100) not null,
    begun_at    timestamp    not null,
    finished_at timestamp    null,
    
    constraint request_pk primary key (id)
);

create table trip_option (
    request_id varchar(100) not null,
	trip_id    varchar(50)  not null,
	sale_total varchar(20)  not null,
	slice      jsonb        null,
	pricing    jsonb        null,
	
	constraint trip_option_pk primary key (request_id, trip_id),
	constraint trip_option_request_fk foreign key (request_id) references request (id)
);
create index trip_option_request_idx on trip_option (request_id);