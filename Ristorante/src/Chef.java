import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Chef {
    public static void main(String[] args) {

        // used for communication with waiters
        final int PORT = 1315;

        // writes the menù
        writeMenu();

        // creates a server socket with the specified port to communicate with waiters
        try (ServerSocket chefSocket = new ServerSocket(PORT)) {

            // keeps cooking clients' orders and sending them to waiters
            while (true) {

                // reads an order, prepares it and sends it back to the waiter
                prepareOrder(chefSocket);
            }
        }
        catch (IOException exc) {
            System.out.println("(Cuoco) Impossibile comunicare con il cameriere");
            throw new RuntimeException(exc);
        }
    }

    public static void writeMenu() {

        // tries to open the file in read mode
        try (FileWriter menuWriter = new FileWriter("menu.txt")) {
            String order,                                       // order's name
                   status;                                      // "S" if the chef wants to add another order into the menu and "N" otherwise
            float price;                                        // order's price
            Scanner scanner = new Scanner(System.in);           // object to read from the stdin
            PrintWriter writer = new PrintWriter(menuWriter);   // object to write into the file

            // writes the menu
            do {

                // writes customer's order
                do {
                    System.out.println("Quale ordine desideri scrivere nel menù?");
                    order = scanner.nextLine();
                }
                while(order.isEmpty());

                // writes order's price
                do {
                    System.out.println("Inserisci il prezzo dell'ordine");
                    price = Float.parseFloat(scanner.next());
                }
                while (price < 0.50f);

                // writes order's name and order's price into the file separated by a line
                writer.println(order);
                writer.println(price);

                // checks if the chef wants to add another order into the menù
                System.out.println("Desideri inserire un altro ordine nel menù? (S/N)");
                status = scanner.next();
                scanner.nextLine();
            }
            while (status.equalsIgnoreCase("s"));
        }
        catch (IOException exc) {
            System.out.println("(Cuoco) Errore scrittura menù");
            throw new RuntimeException(exc);
        }
    }

    //simulates the preparation of an order by the chef
    public static void prepareOrder(ServerSocket chefSocket) throws IOException {

        // waits for an order request by a waiter
        System.out.println("(Cuoco) Attendo un ordine");
        Socket acceptedOrder = chefSocket.accept();

        // used to get an order by a waiter and to send it back to him
        BufferedReader takeOrder = new BufferedReader(new InputStreamReader(acceptedOrder.getInputStream()));
        PrintWriter sendOrder = new PrintWriter(acceptedOrder.getOutputStream());

        // reads an order
        String order = takeOrder.readLine();

        // prepares the order
        System.out.println("(Cuoco) Preparo: " + order);
        try {
            Thread.sleep(3000);
        }
        catch(InterruptedException exc) {
            System.out.println("(Cuoco) Errore utilizzo sleep");
            throw new RuntimeException(exc);
        }

        // sends the order to a waiter
        System.out.println("(Cuoco) Ordine pronto e consegnato al cameriere");
        sendOrder.println(order);
        sendOrder.flush();

        // closes the communication
        acceptedOrder.close();
    }
}