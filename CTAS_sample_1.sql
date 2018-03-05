-- init 
create table public.test1 (
    id int primary key,
    name varchar(255)
);
insert into test1 (id, name) values (1, ‘name 1′);
insert into test1 (id, name) values (2, ‘name 2′);


-- test 1 
create table public.test2   as select * from public.test1  where id = 1;

-- test 2
create table public.test3  as 
select test1.* from public.test1  
inner join public.test2 on test1.id= test2.id
;