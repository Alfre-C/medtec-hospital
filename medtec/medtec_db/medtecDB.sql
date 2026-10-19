create table medicamentos(
	id serial primary key,
	nombre varchar(50) not null,
	stock int not null
);

insert into medicamentos(nombre, stock) values 
('Paracetamol', 50),
('Ibuprofeno', 20),
('Amoxicilina', 15);

create table servicios(
	id serial primary key,
	nombre varchar(50) not null,
	precio int not null
);

insert into servicios(nombre, precio) values 
('Consulta', 120000),
('Hemograma', 80000),
('Ecografía', 150000);

/*No usar esta version anterior de la tabla turnos*/
create table turnos(
	id serial primary key,
	cedula varchar(20) not null,
	especialidad varchar(50) not null,
	horario varchar(20) not null,
	medico varchar(50) not null
);
/*-------------------------------------*/

/*Agregar mas espacio a la columna de horarios*/
alter table turnos alter column horario type varchar(50);

/*Ignorar este insert anterior*/
/*Cargar nuevos valores para probar la comunicacion bidireccional y el registro de turnos*/
insert into turnos(cedula, especialidad, horario, medico) values 
('DISPONIBLE', 'Cirujano', 'Lunes: 08:00 a 08:20', 'Dr. Perez'),
('DISPONIBLE', 'Cirujano', 'Lunes: 08:20 a 08:40', 'Dr. Perez'),
('DISPONIBLE', 'Oftalmologo', 'Lunes: 08:40 a 09:00', 'Dr. Costa'),
('DISPONIBLE', 'Pediatra', 'Martes: 08:00 a 08:20', 'Dr. Pavon'),
('DISPONIBLE', 'Cardiologo', 'Martes: 08:20 a 08:40', 'Dr. Corazon'),
('DISPONIBLE', 'Traumatologo', 'Miercoles: 09:00 a 09:20', 'Dr. Chopper');

/*-----------------------------------------------------------------------------------------*/
/*-------------------Aca abajo esta lo importante----------------------------------------------*/

/*Tabla final de turnos*/
create table turnos(
	id serial primary key,
	cedula varchar(20) not null,
	especialidad varchar(50) not null,
	horario varchar(50) not null,
	medico varchar(50) not null
);

/*Agregar una tabla para insertar los medicos y sus horarios disponibles*/
create table horarios_de_atencion(
	id serial primary key,
	estado varchar(20) not null,
	especialidad varchar(50) not null,
	horario varchar(50) not null,
	medico varchar(50) not null
);

/*-------------------Usar para probar el programa de turnos--------------------------------*/

/*Cargar nuevos valores para probar la comunicacion bidireccional y el registro de turnos*/
insert into horarios_de_atencion(estado, especialidad, horario, medico) values 
('DISPONIBLE', 'Cirujano', 'Lunes: 08:00 a 08:20', 'Dr. Perez'),
('DISPONIBLE', 'Cirujano', 'Lunes: 08:20 a 08:40', 'Dr. Perez'),
('DISPONIBLE', 'Oftalmologo', 'Lunes: 08:40 a 09:00', 'Dr. Costa'),
('DISPONIBLE', 'Pediatra', 'Martes: 08:00 a 08:20', 'Dr. Pavon'),
('DISPONIBLE', 'Cardiologo', 'Martes: 08:20 a 08:40', 'Dr. Corazon'),
('DISPONIBLE', 'Traumatologo', 'Miercoles: 09:00 a 09:20', 'Dr. Chopper');

/*Para borrar la tabla turnos y resetear el id serial*/
truncate table turnos restart identity;

/*Para borrar la tabla horarios_de_atencion y resetear el id serial*/
truncate table horarios_de_atencion restart identity;

/*---------------------------------------------------------------------------------------*/
/*---------------------------------------------------------------------------------------------*/
