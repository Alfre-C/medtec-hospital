package py.una.pol.sd.tcp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.json.JSONArray;
import org.json.JSONObject;

public class CopagosClient {

    /**
     * Envía al servidor de AlfaMed la solicitud de cálculo de copagos.
     * @param cedula Número de cédula del paciente.
     * @param servicios Lista de servicios realizados.
     * @return JSONObject con la respuesta del servidor.
     */
    public static JSONObject solicitarCopago(String cedula, JSONArray servicios) {
        JSONObject respuesta = new JSONObject();

        try {
            Socket socket = new Socket("localhost", 6000);

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            JSONObject req = new JSONObject();
            req.put("cedula", cedula);
            req.put("servicios", servicios);

            out.println(req.toString());

            String respStr = in.readLine();
            respuesta = new JSONObject(respStr);

            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return respuesta;
    }
}