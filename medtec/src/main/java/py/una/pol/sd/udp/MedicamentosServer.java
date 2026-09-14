package py.una.pol.sd.udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import org.json.JSONObject;

public class MedicamentosServer {
    public static void main(String[] args) {
        int port = 7000;
        System.out.println("Iniciando Servidor UDP de Consulta de Medicamentos MedTec en el puerto " + port + "...");

        try (DatagramSocket socket = new DatagramSocket(port)) {
            byte[] buffer = new byte[1024];

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String mensaje = new String(packet.getData(), 0, packet.getLength()).trim();
                System.out.println("Solicitud UDP recibida: " + mensaje);

                if ("GET_MEDICAMENTOS".equals(mensaje)) {
                    JSONObject jsonResp = new JSONObject();
                    jsonResp.put("Paracetamol", 50);
                    jsonResp.put("Ibuprofeno", 20);

                    byte[] respData = jsonResp.toString().getBytes();
                    DatagramPacket responsePacket = new DatagramPacket(
                        respData,
                        respData.length,
                        packet.getAddress(),
                        packet.getPort()
                    );
                    socket.send(responsePacket);
                    System.out.println("Stock de medicamentos enviado al cliente.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}