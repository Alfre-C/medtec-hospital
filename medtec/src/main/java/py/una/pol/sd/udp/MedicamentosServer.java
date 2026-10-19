package py.una.pol.sd.udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.json.JSONArray;
import org.json.JSONObject;
import py.una.pol.sd.db.HospitalDB;

public class MedicamentosServer {

    public static void main(String[] args) {
        try {
            DatagramSocket socket = new DatagramSocket(7000);
            System.out.println("Servidor UDP de Medicamentos iniciado en puerto 7000...");

            while (true) {
                byte[] buffer = new byte[4096];
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                socket.receive(request);

                String mensaje = new String(request.getData(), 0, request.getLength());
                System.out.println("Mensaje recibido: " + mensaje);

                if (mensaje.equals("GET_MEDICAMENTOS")) {

                    JSONArray lista = HospitalDB.obtenerTodosLosMedicamentos();
                    JSONObject respuesta = new JSONObject();
                    respuesta.put("medicamentos", lista);

                    byte[] respBytes = respuesta.toString().getBytes();

                    DatagramPacket respPacket = new DatagramPacket(
                            respBytes,
                            respBytes.length,
                            request.getAddress(),
                            request.getPort()
                    );

                    socket.send(respPacket);
                    System.out.println("JSON enviado al cliente.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}