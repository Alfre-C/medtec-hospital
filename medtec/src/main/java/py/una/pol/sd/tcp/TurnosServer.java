package py.una.pol.sd.tcp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import org.json.JSONObject;

public class TurnosServer {
    public static void main(String[] args) {
        int port = 5000;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Iniciando Servidor MedTec - Hospital en el puerto " + port + "...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket socket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            String inputLine = in.readLine();
            if (inputLine != null) {
                JSONObject req = new JSONObject(inputLine);
                String cedula = req.optString("cedula", "");
                String especialidad = req.optString("especialidad", "");
                String horario = req.optString("horario", "");

                System.out.println("Solicitud de turno recibida - Cédula: " + cedula + ", Especialidad: " + especialidad);

                // Respuesta simulada según el requerimiento
                JSONObject resp = new JSONObject();
                resp.put("status", "CONFIRMADO");
                resp.put("medico", "Dr. Pérez");
                resp.put("turno_id", 102);

                out.println(resp.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}