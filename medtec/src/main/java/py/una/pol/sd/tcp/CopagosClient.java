package py.una.pol.sd.tcp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import org.json.JSONArray;
import org.json.JSONObject;

public class CopagosClient {
    /**
     * Solicita al servidor de AlfaMed la validación de copagos y deducibles.
     * @param cedula Cédula del paciente.
     * @param servicios Lista de nombres de servicios requeridos.
     * @return JSONObject con la respuesta del servidor (ej. monto total).
     */
    public static JSONObject consultarCopago(String cedula, String[] servicios) {
        JSONObject respuesta = new JSONObject();

        try (Socket socket = new Socket("localhost", 6000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Construir solicitud JSON según el requerimiento (Cliente MedTec -> Servidor AlfaMed)
            JSONObject req = new JSONObject();
            req.put("cedula", cedula);
            
            JSONArray serviciosArray = new JSONArray();
            for (String servicio : servicios) {
                serviciosArray.put(servicio);
            }
            req.put("servicios", serviciosArray);

            // Enviar solicitud
            out.println(req.toString());

            // Recibir respuesta
            String respStr = in.readLine();
            if (respStr != null) {
                respuesta = new JSONObject(respStr);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return respuesta;
    }

    // Método main opcional para pruebas unitarias de este cliente
    public static void main(String[] args) {
        System.out.println("Enviando consulta de copagos a AlfaMed...");
        String[] serviciosEjemplo = {"Consulta", "Hemograma"};
        JSONObject res = consultarCopago("5419598", serviciosEjemplo);
        System.out.println("Respuesta recibida: " + res.toString());
    }
}