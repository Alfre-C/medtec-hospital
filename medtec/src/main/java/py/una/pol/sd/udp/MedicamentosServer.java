package py.una.pol.sd.udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.json.JSONObject;

public class MedicamentosServer {
    
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/medtec_db";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "postgres";

    public static void main(String[] args) {
        int port = 7000;
        System.out.println("Iniciando Servidor UDP de Consulta de Medicamentos MedTec en el puerto " + port + "...");

        try (DatagramSocket socket = new DatagramSocket(port)) {
            byte[] buffer = new byte[1024];

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String mensaje = new String(packet.getData(), 0, packet.getLength()).trim();
                System.out.println("Solicitud UDP recibida de " + packet.getAddress() + ":" + packet.getPort() + " -> " + mensaje);

                if ("Bye".equalsIgnoreCase(mensaje)) {
                    System.out.println("El cliente indicó desconexión (Bye). Esperando nuevas solicitudes...");
                    continue; // Vuelve a esperar al siguiente paquete de cualquier cliente
                }

                if ("GET_MEDICAMENTOS".equals(mensaje)) {
                    JSONObject jsonResp = new JSONObject();

                    // Consultar la base de datos real de PostgreSQL
                    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                         PreparedStatement stmt = conn.prepareStatement("SELECT nombre, stock FROM medicamentos");
                         ResultSet rs = stmt.executeQuery()) {

                        while (rs.next()) {
                            String nombre = rs.getString("nombre");
                            int stock = rs.getInt("stock");
                            jsonResp.put(nombre, stock);
                        }

                    } catch (Exception dbEx) {
                        System.err.println("Error al conectar o consultar la base de datos: " + dbEx.getMessage());
                        jsonResp.put("error", "No se pudo obtener el stock de la base de datos");
                    }

                    byte[] respData = jsonResp.toString().getBytes();
                    DatagramPacket responsePacket = new DatagramPacket(
                        respData,
                        respData.length,
                        packet.getAddress(),
                        packet.getPort()
                    );
                    socket.send(responsePacket);
                    System.out.println("Stock obtenido desde PostgreSQL y enviado al cliente.");
                } else {
                    // Responder si el comando no es reconocido
                    String errorMsg = "Comando no reconocido. Use GET_MEDICAMENTOS o Bye.";
                    byte[] respData = errorMsg.getBytes();
                    DatagramPacket responsePacket = new DatagramPacket(
                        respData,
                        respData.length,
                        packet.getAddress(),
                        packet.getPort()
                    );
                    socket.send(responsePacket);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}