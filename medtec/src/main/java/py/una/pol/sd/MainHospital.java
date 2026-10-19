package py.una.pol.sd;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

import py.una.pol.sd.tcp.CopagosClient;
import py.una.pol.sd.udp.MedicamentosServer;
import py.una.pol.sd.tcp.TurnosServer;

public class MainHospital {
    public static void main(String[] args) throws Exception {

        Scanner sc = new Scanner(System.in);

        System.out.println("=== MEDTEC - HOSPITAL ===");

        while (true) {
            System.out.println("\nSeleccione una opción:");
            System.out.println("1. Iniciar servidor de Turnos (TCP)");
            System.out.println("2. Iniciar servidor de Medicamentos (UDP)");
            System.out.println("3. Solicitar cálculo de copago (TCP → AlfaMed)");
            System.out.println("4. Salir");

            System.out.print("Opción: ");
            int op = Integer.parseInt(sc.nextLine());

            switch (op) {

                case 1:
                    System.out.println("Servidor de turnos iniciado en puerto 5000...");
                    new Thread(() -> {
                        try {
                            TurnosServer.main(null);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();
                    break;

                case 2:
                    System.out.println("Servidor de medicamentos iniciado en puerto 7000...");
                    new Thread(() -> {
                        try {
                            MedicamentosServer.main(null);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();
                    break;

                case 3:
                    System.out.print("Ingrese cédula: ");
                    String cedula = sc.nextLine();

                    JSONArray servicios = new JSONArray();
                    System.out.println("Ingrese servicios (escriba FIN para terminar):");
                    while (true) {
                        String s = sc.nextLine();
                        if (s.equalsIgnoreCase("FIN")) break;
                        servicios.put(s);
                    }

                    JSONObject resp = CopagosClient.solicitarCopago(cedula, servicios);
                    System.out.println("Respuesta del seguro:");
                    System.out.println(resp.toString(2));
                    break;

                case 4:
                    System.out.println("Saliendo...");
                    sc.close();
                    return;

                default:
                    System.out.println("Opción inválida.");
            }
        }
    }
}
