/*Version modificada para permitir comunicacion bidireccional */
package py.una.pol.sd.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.json.JSONArray;
import org.json.JSONObject;

public class HospitalDB {
    //Recordar que si no conecta es por que hay que cambiar el password
    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(
            "jdbc:postgresql://localhost:5432/db_medtec",
            "postgres",
            "postgres"
        );
    }

    // ============================================
    // MEDICAMENTOS (UDP)
    // ============================================
    public static JSONArray obtenerTodosLosMedicamentos() {
        JSONArray arr = new JSONArray();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT nombre, stock FROM medicamentos");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                JSONObject obj = new JSONObject();
                obj.put("nombre", rs.getString("nombre"));
                obj.put("stock", rs.getInt("stock"));
                arr.put(obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arr;
    }

    // ============================================
    // SERVICIOS (TCP)
    // ============================================
    public static int obtenerPrecioServicio(String nombreServicio) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT precio FROM servicios WHERE nombre = ?")) {
            stmt.setString(1, nombreServicio);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("precio");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ============================================
    // CONSULTAS DINÁMICAS PARA TURNOS INTERACTIVOS
    // ============================================
    public static JSONArray obtenerEspecialidades() {
        JSONArray arr = new JSONArray();
        try (Connection conn = getConnection();
            //horarios_de_atencion es de donde saca la informacion
            //PreparedStatement stmt = conn.prepareStatement("SELECT DISTINCT especialidad FROM turnos");
             PreparedStatement stmt = conn.prepareStatement("SELECT DISTINCT especialidad FROM horarios_de_atencion");
             ResultSet rs = stmt.executeQuery()) {
            int id = 1;
            while (rs.next()) {
                JSONObject obj = new JSONObject();
                obj.put("id", id++);
                obj.put("nombre", rs.getString("especialidad"));
                arr.put(obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arr;
    }

    public static JSONArray obtenerHorariosPorEspecialidad(String especialidad) {
        JSONArray arr = new JSONArray();
        try (Connection conn = getConnection();
            //horarios_de_atencion es de donde saca la informacion
            //PreparedStatement stmt = conn.prepareStatement("SELECT DISTINCT horario FROM turnos WHERE especialidad = ?"))
            //PreparedStatement stmt = conn.prepareStatement("SELECT DISTINCT horario FROM horarios_de_atencion WHERE especialidad = ?"))
             PreparedStatement stmt = conn.prepareStatement("SELECT DISTINCT horario FROM horarios_de_atencion WHERE especialidad = ? AND estado = 'DISPONIBLE'")) {
            stmt.setString(1, especialidad);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                arr.put(rs.getString("horario"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arr;
    }

    /*Nueva version funcional bidireccional */
    public static JSONArray obtenerMedicos(String especialidad, String horario) {
        JSONArray arr = new JSONArray();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                "SELECT DISTINCT medico " +
                "FROM horarios_de_atencion " +
                "WHERE especialidad = ? " +
                "AND horario = ? " +
                "AND estado = 'DISPONIBLE'"
            )) {

            stmt.setString(1, especialidad);
            stmt.setString(2, horario);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                arr.put(rs.getString("medico"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return arr;
    }
    /*------------------------------------- */
    /*Version funcional bidireccional anterior */
    /*public static JSONArray obtenerMedicos(String especialidad, String horario) {
        JSONArray arr = new JSONArray();
        try (Connection conn = getConnection();
            //horarios_de_atencion es de donde saca la informacion
            //PreparedStatement stmt = conn.prepareStatement("SELECT DISTINCT medico FROM turnos WHERE especialidad = ? AND horario = ?"))
            //PreparedStatement stmt = conn.prepareStatement("SELECT DISTINCT medico FROM horarios_de_atencion WHERE especialidad = ? AND horario = ?"))
             PreparedStatement stmt = conn.prepareStatement("SELECT DISTINCT medico FROM horarios_de_atencion WHERE especialidad = ? AND horario = ? AND estado = 'DISPONIBLE'")) {
            stmt.setString(1, especialidad);
            stmt.setString(2, horario);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                arr.put(rs.getString("medico"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arr;
    }*/
    /*----------------------------------------- */

    // ============================================
    // TURNOS (TCP)
    // ============================================

    /*Nueva version funcional bidireccional  */
    public static JSONObject reservarTurno(
            String cedula,
            String especialidad,
            String horario,
            String medico) {

        JSONObject turno = new JSONObject();

        Connection conn = null;

        try {

            conn = getConnection();
            conn.setAutoCommit(false);

            // 1. Intentar bloquear la disponibilidad
            PreparedStatement updateEstado = conn.prepareStatement(
                "UPDATE horarios_de_atencion " +
                "SET estado = 'OCUPADO' " +
                "WHERE especialidad = ? " +
                "AND horario = ? " +
                "AND medico = ? " +
                "AND estado = 'DISPONIBLE'"
            );

            updateEstado.setString(1, especialidad);
            updateEstado.setString(2, horario);
            updateEstado.setString(3, medico);

            int filasActualizadas = updateEstado.executeUpdate();

            // Si no se actualizó ninguna fila,
            // alguien ya reservó ese horario antes.
            if (filasActualizadas == 0) {

                conn.rollback();

                turno.put("status", "ERROR");
                turno.put("mensaje",
                        "El horario seleccionado ya no se encuentra disponible.");

                return turno;
            }

            // 2. Registrar el turno
            PreparedStatement insertTurno = conn.prepareStatement(
                "INSERT INTO turnos " +
                "(cedula, especialidad, horario, medico) " +
                "VALUES (?, ?, ?, ?) " +
                "RETURNING id"
            );

            insertTurno.setString(1, cedula);
            insertTurno.setString(2, especialidad);
            insertTurno.setString(3, horario);
            insertTurno.setString(4, medico);

            ResultSet rs = insertTurno.executeQuery();

            if (rs.next()) {

                conn.commit();

                turno.put("status", "CONFIRMADO");
                turno.put("turno_id", rs.getInt("id"));
                turno.put("cedula", cedula);
                turno.put("especialidad", especialidad);
                turno.put("horario", horario);
                turno.put("medico", medico);

            } else {

                conn.rollback();

                turno.put("status", "ERROR");
                turno.put("mensaje",
                        "No fue posible registrar el turno.");
            }

        } catch (Exception e) {

            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            e.printStackTrace();

            turno.put("status", "ERROR");
            turno.put("mensaje", e.getMessage());

        } finally {

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return turno;
    }
    /*-------------------------------------- */

    /*Version funcional bidireccional anterior */
    /*public static JSONObject reservarTurno(String cedula, String especialidad, String horario, String medico) {
        JSONObject turno = new JSONObject();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "INSERT INTO turnos (cedula, especialidad, horario, medico) VALUES (?, ?, ?, ?) RETURNING id, medico"
             )) {
            stmt.setString(1, cedula);
            stmt.setString(2, especialidad);
            stmt.setString(3, horario);
            stmt.setString(4, medico);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                turno.put("status", "CONFIRMADO");
                turno.put("cedula", cedula);
                turno.put("especialidad", especialidad);
                turno.put("horario", horario);
                turno.put("medico", rs.getString("medico"));
                turno.put("turno_id", rs.getInt("id"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return turno;
    }*/
}
/*------------------------------------------- */
/*------------------------------------------------------------------------------------------------- */






/* Version original funcional */
/*package py.una.pol.sd.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.json.JSONArray;
import org.json.JSONObject;

public class HospitalDB {

    // ============================================
    // CONEXION A LA BASE DE DATOS
    // ============================================
    //Colocar la url de postgres, el usuario y el password para poder utilizar
    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(
            "jdbc:postgresql://localhost:5432/db_medtec",
            "postgres",
            "postgres"
        );
    }

    // ============================================
    // MEDICAMENTOS (UDP)
    // ============================================
    public static JSONArray obtenerTodosLosMedicamentos() {
        JSONArray arr = new JSONArray();

        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT nombre, stock FROM medicamentos"
            );

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                JSONObject obj = new JSONObject();
                obj.put("nombre", rs.getString("nombre"));
                obj.put("stock", rs.getInt("stock"));
                arr.put(obj);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return arr;
    }

    // ============================================
    // SERVICIOS (TCP)
    // ============================================
    public static int obtenerPrecioServicio(String nombreServicio) {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT precio FROM servicios WHERE nombre = ?"
            );
            stmt.setString(1, nombreServicio);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("precio");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0; // si no existe
    }

    // ============================================
    // TURNOS (TCP)
    // ============================================
    public static JSONObject reservarTurno(String cedula, String especialidad, String horario, String medico) {
        JSONObject turno = new JSONObject();

        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO turnos (cedula, especialidad, horario, medico) VALUES (?, ?, ?, ?) RETURNING id, medico"
            );

            stmt.setString(1, cedula);
            stmt.setString(2, especialidad);
            stmt.setString(3, horario);
            stmt.setString(4, medico);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                turno.put("status", "CONFIRMADO");
                turno.put("cedula", cedula);
                turno.put("especialidad", especialidad);
                turno.put("horario", horario);
                turno.put("medico", medico);
                turno.put("turno_id", rs.getInt("id"));
                turno.put("medico", rs.getString("medico"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return turno;
    }
}*/