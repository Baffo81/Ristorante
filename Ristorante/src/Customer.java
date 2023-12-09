import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.*;

public class Customer {
    final Scanner scanner = new Scanner(System.in);

    public void run() {
        // ------------------------------------- ports for the communication -------------------------------------------
        final int PORT_TO_RECEPTION = 1313,    // used for the connection with the receptionist
                  PORT_TO_WAITER    = 1316;    // used for the connection with waiters
        // -------------------------------------------------------------------------------------------------------------

        // ------------------------------------- objects to read and write through the socket --------------------------
        BufferedReader checkSeats,          // used to get receptionist answer about if there are available seats
                       eatOrder;            // used to take end eat a ready order
        PrintWriter sendSeats,              // used to say to the receptionist how many seats he requires
                    takeOrder;              // used to take end eat a ready order
        // -------------------------------------------------------------------------------------------------------------

        // ----------------------------------------- variables ---------------------------------------------------------
        int requiredSeats,                  // number of required seats
            tableNumber,                    // number of customer's table
            waitingTime;                    // time the customer has to wait to enter
        String order,                       // requested order by the customer
               answerWaitingTime;           // used to check if the user wants waiting
        // -------------------------------------------------------------------------------------------------------------

        // tries to create a socket with specified server's address and port's number to communicate with the waiter
        try (Socket receptionSocket = new Socket(InetAddress.getLocalHost(), PORT_TO_RECEPTION)) {

            // says how many seats he needs to the receptionist and gets a table
            checkSeats = new BufferedReader(new InputStreamReader(receptionSocket.getInputStream()));
            sendSeats = new PrintWriter(receptionSocket.getOutputStream(), true);
            requiredSeats = getRequiredSeats();
            sendSeats.println(requiredSeats);
            tableNumber = Integer.parseInt(checkSeats.readLine());
            receptionSocket.close();

            // if there are available seats, the customer takes them
            if (tableNumber >= 0) {

                // gets the menù
                System.out.println("(Cliente) Prendo posto al tavolo " + tableNumber + " e scannerizzo il menù");

                // creates a socket to take orders
                try (Socket waiterSocket = new Socket(InetAddress.getLocalHost(), PORT_TO_WAITER)){

                    // shows the menu
                    getMenu();

                    // simulates customer's orders
                    for (int i = 0; i < 4; i++) {

                        // orders, waits for the order and eats it
                        eatOrder = new BufferedReader(new InputStreamReader(waiterSocket.getInputStream()));
                        takeOrder = new PrintWriter(waiterSocket.getOutputStream(), true);
                        order = getOrder();
                        takeOrder.println(order);
                        order = eatOrder.readLine();
                        System.out.println("(Cliente) Mangio " + order);
                        try {
                            Thread.sleep(1000);
                        }
                        catch(InterruptedException exc) {
                            System.out.println("(Cliente) Errore utilizzo sleep");
                            throw new RuntimeException(exc);
                        }

                    }
                }
                catch (IOException exc) {
                    System.out.println("(Cliente) Impossibile comunicare con il cameriere");
                    throw new RuntimeException(exc);
                }
            }

            // otherwise, he waits
            else {
                try (Socket receptionSocket2 = new Socket(InetAddress.getLocalHost(), PORT_TO_RECEPTION)) {

                    // objects for reading and writing through the socket
                    BufferedReader checkSeats2 = new BufferedReader(new InputStreamReader(receptionSocket2.getInputStream()));

                    // decides if waiting or not
                    waitingTime = Integer.parseInt(checkSeats2.readLine());

                    System.out.println("(Reception) Vuoi attendere " + waitingTime + " minuti ?");
                    answerWaitingTime = scanner.next();

                    if (answerWaitingTime.equalsIgnoreCase("si")) {

                        // creates a scheduler to plan the periodic execution of tasks
                        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

                        // plans which task execute after a waiting time, and specifies the time unity
                        ScheduledFuture<?> waitTask = scheduler.schedule(this::onWaitComplete, waitingTime, TimeUnit.SECONDS);

                        // waits for the task to complete (the estimated wait time)
                        try {
                            waitTask.get(); // restituisce la fine della task (è bloccante)
                        }
                        catch (InterruptedException | ExecutionException exc) {
                            System.out.println("(Cliente) Errore utilizzo scheduler");
                            throw new RuntimeException(exc);
                        }

                        // deallocates used resources
                        finally {
                            scheduler.shutdown(); // rilascio le risorse
                        }
                    }
                    else {
                        System.out.println("(Cliente) Me ne vado!");
                    }
                }
            }
        }
        catch (IOException exc) {
            System.out.println("(Cliente) Impossibile comunicare con il receptionist");
            throw new RuntimeException(exc);
        }
    }

    private void onWaitComplete() {
        System.out.println("(Cliente) Attesa completata. Riprovo a prendere posto.");
        run();
    }

    // allows customer to say how many seats he needs
    public int getRequiredSeats() {
        // reads customer requested seats
        System.out.println("Benvenuto, di quanti posti hai bisogno?");
        int requiredSeats = scanner.nextInt();
        System.out.println("(Cliente) Mi servono " + requiredSeats + " posti");
        return requiredSeats;
    }

    public String getOrder() {
        // customer's order
        String order;

        // gets customer's order
        do {
            System.out.println("Scegli un ordine da effettuare");
            scanner.nextLine();
            order = scanner.nextLine();
        } while (!checkOrder(order));

        return order;
    }

    // checks if customer's requested order is in the menù
    public boolean checkOrder(String order) {
        // tries to open the file in read mode
        try (FileReader fileReader = new FileReader("menu.txt")) {
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String menuOrder;
            while ((menuOrder = bufferedReader.readLine()) != null)
                if (menuOrder.equals(order))
                    return true;

            // closes the connection to the file
            bufferedReader.close();
            return false;
        }
        catch (Exception exc) {
            System.out.println("(Cliente) Errore apertura menù");
            throw new RuntimeException(exc);
        }
    }

    // simulates menu's scanning by the customer and shows it
    public void getMenu() {
        // tries to open the file in read mode
        try (FileReader fileReader = new FileReader("menu.txt")) {
            // reads each menu order and prints them on the screen
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String order;
            float price;
            System.out.println("Questo è il menù:");
            while ((order = bufferedReader.readLine()) != null) {
                price = Float.parseFloat(bufferedReader.readLine().trim());
                System.out.println("Ordine: " + order);
                System.out.println("Prezzo: " + price);
            }

            // closes the connection to the file
            bufferedReader.close();
        }
        catch (Exception exc) {
            System.out.println("(Cliente) Errore scannerizzazione menù");
            throw new RuntimeException(exc);
        }
    }

    public static void main(String[] args) {
            Customer customer = new Customer();
            customer.run();
    }
}
