import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class WaiterSon {

    static final int PORT_TO_CHEF = 1315;
    static BufferedReader takeOrder;
    static PrintWriter sendOrder;
    static String order;

    public static void main(String[] args) {
        try {
            System.out.println("(Sguattero) Ciao sono lo sguattero e sono pronto ad inviare l'ordine allo chef");

            // Invia un messaggio di prontezza al processo padre
            System.out.println("READY");

            // Leggi il socket dal processo padre
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String clientAddress = reader.readLine().trim();
            int clientPort = Integer.parseInt(reader.readLine().trim());
            System.out.println("porta:" + clientAddress + clientPort);

            // Ricevi le informazioni sulla socket dal processo padre
            try (Socket acceptedCustomer = new Socket(clientAddress, clientPort)) {
                takeOrder = new BufferedReader(new InputStreamReader(acceptedCustomer.getInputStream()));
                sendOrder = new PrintWriter(acceptedCustomer.getOutputStream(), true);
                System.out.println("(Sguattero) prendo l'ordine del cliente ");
                order = takeOrder.readLine();

                // Comunica con lo chef
                try (Socket socketChef = new Socket(InetAddress.getLocalHost(), PORT_TO_CHEF)) {
                    sendOrder = new PrintWriter(socketChef.getOutputStream(), true);
                    System.out.println("(Sguattero) Mando l'ordine del cliente allo chef");
                    sendOrder.println(order);
                    System.out.println("(Sguattero) Attendo che lo chef prepari il piatto");
                    order = takeOrder.readLine();
                    System.out.println("(Sguattero) il piatto e' pronto:" + order);
                }

                System.out.println("(Sguattero) Consegno il piatto al cliente");
                sendOrder.println(order);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
