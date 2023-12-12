import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Waiter {
    public static void main(String[] args) {
        Waiter waiter = new Waiter();
        waiter.waiter();
    }

    public void waiter() {
        final int PORT_TO_CUSTOMER = 1316,
                PORT_TO_CHEF = 1315;

        try (ServerSocket serverSocket = new ServerSocket(PORT_TO_CUSTOMER)) {
            try (Socket chefSocket = new Socket(InetAddress.getLocalHost(), PORT_TO_CHEF)) {
                while (true) {
                    System.out.println("(Cameriere) Attendo l'ordine del cliente");
                    Socket acceptedCustomer = serverSocket.accept();

                    // crea un nuovo thread per gestire una richiesta
                    new Thread(() -> handleCustomer(acceptedCustomer, chefSocket)).start();
                }
            } catch (IOException exc) {
                System.out.println("(Cameriere) Impossibile comunicare con il cuoco");
                throw new RuntimeException(exc);
            }
        } catch (IOException exc) {
            System.out.println("(Cameriere) Impossibile comunicare con il cliente");
            throw new RuntimeException(exc);
        }
    }

    public static void handleCustomer(Socket customerSocket, Socket chefSocket) {
        try (
                BufferedReader readOrderFromCustomer = new BufferedReader(new InputStreamReader(customerSocket.getInputStream()));
                PrintWriter sendOrderToChef = new PrintWriter(chefSocket.getOutputStream());
                BufferedReader readReadyOrderFromChef = new BufferedReader(new InputStreamReader(chefSocket.getInputStream()));
                PrintWriter sendReadyOrderToCustomer = new PrintWriter(customerSocket.getOutputStream(), true)
        ) {
            readOrder(readOrderFromCustomer, sendOrderToChef, readReadyOrderFromChef, sendReadyOrderToCustomer);
        } catch (IOException exc) {
            System.out.println("(Cameriere) Impossibile gestire l'ordine del cliente");
            // Puoi gestire la situazione in cui il socket è chiuso qui
        } finally {
            try {
                // Chiudi il socket del cliente solo dopo aver gestito l'ordine
                customerSocket.close();
            } catch (IOException exc) {
                System.out.println("(Cameriere) Impossibile chiudere la comunicazione con il cliente");
                exc.printStackTrace();
            }
        }
    }

    public static void readOrder(BufferedReader readOrderFromCustomer, PrintWriter sendOrderToChef, BufferedReader readReadyOrderFromChef, PrintWriter sendReadyOrderToCustomer) throws IOException {
        String order;
        order = readOrderFromCustomer.readLine();
        System.out.println("(Cameriere) Il cliente ordina " + order + ", mando l'ordine allo chef per prepararlo e attendo");
        sendOrderToChef.println(order);
        sendOrderToChef.flush();
        String confirmation = readReadyOrderFromChef.readLine();
        if (confirmation != null && confirmation.equals("Pronto")) {
            System.out.println("(Cameriere) Ricevuta conferma dal cuoco. I piatti sono pronti.");
            sendOrder(sendReadyOrderToCustomer, order);
        } else {
            System.out.println("(Cameriere) Il cuoco non ha confermato la preparazione dell'ordine.");
        }
        try {
            readOrderFromCustomer.close();
            sendOrderToChef.close();
            readReadyOrderFromChef.close();
            sendReadyOrderToCustomer.close();
        } catch (IOException e) {
            System.out.println("(Cameriere) Impossibile chiudere la comunicazione con il cliente");
            throw new RuntimeException(e);
        }
    }

    public static void sendOrder(PrintWriter sendReadyOrderToCustomer, String order) {
        System.out.println("(Cameriere) I piatti: " + order + " sono pronti");
        sendReadyOrderToCustomer.println(order);
        sendReadyOrderToCustomer.flush();
    }
}
