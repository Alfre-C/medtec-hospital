-- Crear la base de datos
CREATE DATABASE medtec_db;

-- Conectar la base de datos 'medtec_db' antes de ejecutar lo siguiente:
CREATE TABLE IF NOT EXISTS medicamentos (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    stock INT NOT NULL
);

-- Insertar datos iniciales (los mismos que tenías en duro)
INSERT INTO medicamentos (nombre, stock) VALUES ('Paracetamol', 50);
INSERT INTO medicamentos (nombre, stock) VALUES ('Ibuprofeno', 20);