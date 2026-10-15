create table if not exists medicamentos (
    id serial primary key,
    nombre varchar(100) not null,
    stock int not null
);

insert into medicamentos (nombre, stock) values ('Paracetamol', 50);
insert into medicamentos (nombre, stock) values ('Ibuprofeno', 20);
insert into medicamentos (nombre, stock) values ('Omeprazol', 80);
insert into medicamentos (nombre, stock) values ('Loratadina', 15);
insert into medicamentos (nombre, stock) values ('Amoxicilina', 30);
insert into medicamentos (nombre, stock) values ('Novalgina', 40);

update medicamentos
set stock = '20'
where nombre = 'Paracetamol';

insert into medicamentos (nombre, stock) values ('Tridoval', 30);