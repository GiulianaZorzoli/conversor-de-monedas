package Conversor;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class ConversorMonedas {
    private static final String API_KEY = "api_key"; //Lo puse de esta manera para no exponer mi api key
    private static final String BASE_URL = "https://v6.exchangerate-api.com/v6/";

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();

    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        System.out.println("CONVERSOR DE MONEDAS");

        boolean continuar = true;
        Scanner scanner = new Scanner(System.in);

        while (continuar) {
            System.out.println("Menu:\n1-Convertir moneda\n2-Mostrar codigos disponibles\n3-Salir");
            int opcion = scanner.nextInt();
            scanner.nextLine();

            switch (opcion) {
                case 1:
                    System.out.println("Ingresa la moneda base (por ejemplo, USD): ");
                    String base = scanner.nextLine().toUpperCase();
                    System.out.println("Ingresa la moneda objetivo (por ejemplo, EUR): ");
                    String objetivo = scanner.nextLine().toUpperCase();
                    System.out.println("Ingresa la cantidad a convertir: ");
                    double cantidad = scanner.nextDouble();
                    scanner.nextLine();  // Consume the newline character left after nextDouble

                    convertirMoneda(base, objetivo, cantidad);
                    break;
                case 2:
                    mostrarCodigosDeMoneda();
                    break;
                case 3:
                    continuar = false;
                    break;
                default:
                    System.out.println("Opción no válida. Por favor, elige una opción del menú.");
            }
        }

        scanner.close();
    }

    private static void convertirMoneda(String base, String objetivo, double cantidad) {
        try {
            String endpoint = BASE_URL + API_KEY + "/latest/" + base;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(endpoint))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);
                if (jsonResponse.get("result").getAsString().equals("success")) {
                    JsonObject conversionRates = jsonResponse.getAsJsonObject("conversion_rates");
                    if (conversionRates.has(objetivo)) {
                        double tasaConversion = conversionRates.get(objetivo).getAsDouble();
                        double cantidadConvertida = cantidad * tasaConversion;
                        System.out.println(cantidad + " " + base + " son " + cantidadConvertida + " " + objetivo);
                    } else {
                        System.out.println("Moneda objetivo no encontrada en las tasas de conversión.");
                    }
                } else {
                    System.out.println("Error en la respuesta de la API.");
                }
            } else {
                System.err.println("Fallo de Request. cod: " + response.statusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void mostrarCodigosDeMoneda() {
        try {
            String endpoint = BASE_URL + API_KEY + "/codes";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(endpoint))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);
                if (jsonResponse.get("result").getAsString().equals("success")) {
                    JsonArray supportedCodes = jsonResponse.getAsJsonArray("supported_codes");
                    System.out.println("Códigos de moneda disponibles:");
                    for (int i = 0; i < supportedCodes.size(); i++) {
                        JsonArray codeInfo = supportedCodes.get(i).getAsJsonArray();
                        System.out.println(codeInfo.get(0).getAsString() + " - " + codeInfo.get(1).getAsString());
                    }
                } else {
                    System.out.println("Error en la respuesta de la API.");
                }
            } else {
                System.err.println("Request failed with status code: " + response.statusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
