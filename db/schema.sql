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
	name varchar(50) not null,
	
	constraint tax_kind_pk primary key (code)
);

create table company (
	code varchar(2)  not null,
	name varchar(50) not null,
	
	constraint company_pk primary key (code)
);

create table trip_option (
	id         varchar(50) not null,
	sale_total varchar(20) not null,
	slice      jsonb       null,
	pricing    jsonb       null,
	timestamp  timestamp   not null default current_timestamp,
	
	constraint trip_option_pk primary key (id)
);