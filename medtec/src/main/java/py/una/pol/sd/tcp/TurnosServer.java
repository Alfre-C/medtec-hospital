/* Version modificada para permitir la comunicacion bidireccional */
package py.una.pol.sd.tcp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import org.json.JSONArray;
import org.json.JSONObject;
import py.una.pol.sd.db.HospitalDB;

public class TurnosServer {
    public static void main(String[] args) throws Exception {
        ServerSocket server = new ServerSocket(5000);
        System.out.println("Servidor de turnos interactivo activo en puerto 5000...");

        while (true) {
            Socket socket = server.accept();
            new Thread(() -> handle(socket)).start();
        }
    }

    private static void handle(Socket socket) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            String especialidad = "";
            String horario = "";
            String medico = "";
            String cedula = "";

            int estado = 1; // 1: Especialidad, 2: Horario, 3: Medico, 4: Cedula

            while (!socket.isClosed()) {
                switch (estado) {
                    case 1:
                        JSONArray especialidades = HospitalDB.obtenerEspecialidades();
                        
                        out.println("MENU");
                        out.println("Seleccione el número de la especialidad:");
                        for (int i = 0; i < especialidades.length(); i++) {
                            JSONObject obj = especialidades.getJSONObject(i);
                            out.println(obj.getInt("id") + "- " + obj.getString("nombre"));
                        }
                        out.println("FIN_MENU");
                        
                        String espInput = in.readLine();
                        if (espInput == null) return;
                        
                        if (espInput.equalsIgnoreCase("VOLVER")) {
                            out.println("INFO: Ya se encuentra en el primer paso.");
                            break;
                        }

                        especialidad = traducirEspecialidadPorId(especialidades, espInput);
                        if (especialidad != null) {
                            estado = 2;
                        } else {
                            out.println("ERROR: Opción inválida.");
                        }
                        break;

                    case 2:
                        JSONArray horarios = HospitalDB.obtenerHorariosPorEspecialidad(especialidad);
                        
                        out.println("MENU");
                        out.println("Horarios disponibles para " + especialidad + ":");
                        for (int i = 0; i < horarios.length(); i++) {
                            out.println("- " + horarios.getString(i));
                        }
                        out.println("Ingrese el Día y la hora exacta (ej: Lunes: 08:00 a 08:20) o escriba VOLVER:");
                        out.println("FIN_MENU");
                        
                        String horInput = in.readLine();
                        if (horInput == null) return;

                        if (horInput.equalsIgnoreCase("VOLVER")) {
                            estado = 1;
                        } else {
                            horario = horInput;
                            estado = 3;
                        }
                        break;

                    case 3:
                        JSONArray medicos = HospitalDB.obtenerMedicos(especialidad, horario);
                        
                        out.println("MENU");
                        out.println("Médicos asignados:");
                        for (int i = 0; i < medicos.length(); i++) {
                            out.println("- " + medicos.getString(i));
                        }
                        out.println("Ingrese el nombre del médico (ej: Dr. Perez) o escriba VOLVER:");
                        out.println("FIN_MENU");
                        
                        String medInput = in.readLine();
                        if (medInput == null) return;

                        if (medInput.equalsIgnoreCase("VOLVER")) {
                            estado = 2;
                        } else {
                            medico = medInput;
                            estado = 4;
                        }
                        break;

                    case 4:
                        out.println("MENU");
                        out.println("Ingrese su cédula para registrar el turno (o escriba VOLVER):");
                        out.println("FIN_MENU");
                        
                        String cedInput = in.readLine();
                        if (cedInput == null) return;

                        if (cedInput.equalsIgnoreCase("VOLVER")) {
                            estado = 3;
                        } else {
                            cedula = cedInput;
                            
                            JSONObject turno = HospitalDB.reservarTurno(cedula, especialidad, horario, medico);
                            
                            out.println("TURNO_CONFIRMADO");
                            out.println(turno.toString());
                            socket.close();
                        }
                        break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String traducirEspecialidadPorId(JSONArray lista, String opcion) {
        try {
            int idBuscado = Integer.parseInt(opcion.trim());
            for (int i = 0; i < lista.length(); i++) {
                JSONObject obj = lista.getJSONObject(i);
                if (obj.getInt("id") == idBuscado) {
                    return obj.getString("nombre");
                }
            }
        } catch (Exception e) {
            // Error de parseo
        }
        return null;
    }


    /*public static void main(String[] args) throws Exception {
        ServerSocket server = new ServerSocket(5000);
        System.out.println("Servidor de turnos interactivo activo en puerto 5000...");

        while (true) {
            Socket socket = server.accept();
            new Thread(() -> handle(socket)).start();
        }
    }

    private static void handle(Socket socket) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            String especialidad = "";
            String horario = "";
            String medico = "";
            String cedula = "";

            int estado = 1; // 1: Especialidad, 2: Horario, 3: Medico, 4: Cedula

            while (!socket.isClosed()) {
                switch (estado) {
                    case 1:
                        JSONArray especialidades = HospitalDB.obtenerEspecialidades();
                        StringBuilder sbEsp = new StringBuilder("Seleccione el número de la especialidad:\n");
                        for (int i = 0; i < especialidades.length(); i++) {
                            JSONObject obj = especialidades.getJSONObject(i);
                            sbEsp.append(obj.getInt("id")).append("- ").append(obj.getString("nombre")).append("\n");
                        }

                        out.println("MENU");
                        out.println(sbEsp.toString().trim());
                        
                        String espInput = in.readLine();
                        if (espInput == null) return;
                        
                        if (espInput.equalsIgnoreCase("VOLVER")) {
                            out.println("INFO: Ya se encuentra en el primer paso.");
                            break;
                        }

                        especialidad = traducirEspecialidadPorId(especialidades, espInput);
                        if (especialidad != null) {
                            estado = 2;
                        } else {
                            out.println("ERROR: Opción inválida.");
                        }
                        break;

                    case 2:
                        JSONArray horarios = HospitalDB.obtenerHorariosPorEspecialidad(especialidad);
                        StringBuilder sbHor = new StringBuilder("Horarios disponibles para " + especialidad + ":\n");
                        for (int i = 0; i < horarios.length(); i++) {
                            sbHor.append("- ").append(horarios.getString(i)).append("\n");
                        }
                        sbHor.append("Ingrese el Día y la hora exacta (ej: Lunes: 08:00 a 08:20) o escriba VOLVER:");

                        out.println("MENU");
                        out.println(sbHor.toString().trim());
                        
                        String horInput = in.readLine();
                        if (horInput == null) return;

                        if (horInput.equalsIgnoreCase("VOLVER")) {
                            estado = 1;
                        } else {
                            horario = horInput;
                            estado = 3;
                        }
                        break;

                    case 3:
                        JSONArray medicos = HospitalDB.obtenerMedicos(especialidad, horario);
                        StringBuilder sbMed = new StringBuilder("Médicos asignados:\n");
                        for (int i = 0; i < medicos.length(); i++) {
                            sbMed.append("- ").append(medicos.getString(i)).append("\n");
                        }
                        sbMed.append("Ingrese el nombre del médico (ej: Dr. Perez) o escriba VOLVER:");

                        out.println("MENU");
                        out.println(sbMed.toString().trim());
                        
                        String medInput = in.readLine();
                        if (medInput == null) return;

                        if (medInput.equalsIgnoreCase("VOLVER")) {
                            estado = 2;
                        } else {
                            medico = medInput;
                            estado = 4;
                        }
                        break;

                    case 4:
                        out.println("MENU");
                        out.println("Ingrese su cédula para registrar el turno (o escriba VOLVER):");
                        
                        String cedInput = in.readLine();
                        if (cedInput == null) return;

                        if (cedInput.equalsIgnoreCase("VOLVER")) {
                            estado = 3;
                        } else {
                            cedula = cedInput;
                            
                            JSONObject turno = HospitalDB.reservarTurno(cedula, especialidad, horario, medico);
                            
                            out.println("TURNO_CONFIRMADO");
                            out.println(turno.toString());
                            socket.close();
                        }
                        break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String traducirEspecialidadPorId(JSONArray lista, String opcion) {
        try {
            int idBuscado = Integer.parseInt(opcion.trim());
            for (int i = 0; i < lista.length(); i++) {
                JSONObject obj = lista.getJSONObject(i);
                if (obj.getInt("id") == idBuscado) {
                    return obj.getString("nombre");
                }
            }
        } catch (Exception e) {
            // Error de parseo
        }
        return null;
    }*/
}
/*-------------------------------------------------------------------------------------------- */








/* Version original funcional */
/*package py.una.pol.sd.tcp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.json.JSONObject;
import py.una.pol.sd.db.HospitalDB;

public class TurnosServer {

    public static void main(String[] args) throws Exception {
        ServerSocket server = new ServerSocket(5000);
        System.out.println("Servidor de turnos activo en puerto 5000...");

        while (true) {
            Socket socket = server.accept();
            new Thread(() -> handle(socket)).start();
        }
    }

    private static void handle(Socket socket) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // Recibir solicitud
            JSONObject req = new JSONObject(in.readLine());
            String cedula = req.getString("cedula");
            String especialidad = req.getString("especialidad");
            String horario = req.getString("horario");
            String medico = req.getString("medico");

            // Procesar reserva en la base de datos
            JSONObject turno = HospitalDB.reservarTurno(cedula, especialidad, horario, medico);

            // Enviar respuesta
            out.println(turno.toString());

            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}*/