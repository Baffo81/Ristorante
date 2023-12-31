import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.*;

public class Customer {

    public void run() {
        final int PORT_TO_RECEPTION = 1313,    // used for the communication with the receptionist
                  PORT_TO_WAITER    = 1316;    // used for the communication with waiters
        int tableNumber,                       // number of customer's table
                waitingTime;                   // time the customer has to wait to enter
        String answerWaitingTime;              // used to check if the user wants waiting

        Socket receptionSocket;
        Socket waiterSocket = null;
        // tries to create a socket with specified server's address and port's number to communicate with the waiter
        try {
            receptionSocket = new Socket(InetAddress.getLocalHost(), PORT_TO_RECEPTION);

            // says how many seats he needs to the receptionist and gets a table
            tableNumber = getTable(receptionSocket);

            // if there are available seats, the customer takes them
            if (tableNumber >= 0) {

                // gets the menù
                System.out.println("(Cliente) Prendo posto al tavolo " + tableNumber + " e scannerizzo il menù");

                receptionSocket.close();

                // creates a socket to take orders
                try {
                     waiterSocket = new Socket(InetAddress.getLocalHost(), PORT_TO_WAITER);

                    // shows the menu
                    getMenu();

                    // orders, waits for the order and eats it
                    getOrder(waiterSocket);

                } catch (IOException exc) {
                    System.out.println("(Cliente) Impossibile comunicare con il cameriere");
                    throw new RuntimeException(exc);
                }
            }

            // otherwise, he waits
            else {
                try (Socket receptionSocket2 = new Socket(InetAddress.getLocalHost(), PORT_TO_RECEPTION)) {

                    // used to read through the socket
                    BufferedReader checkSeats2 = new BufferedReader(new InputStreamReader(receptionSocket2.getInputStream()));

                    // used to read through the standard input
                    BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

                    // decides if waiting or not
                    waitingTime = Integer.parseInt(checkSeats2.readLine());

                    System.out.println("(Reception) Vuoi attendere " + waitingTime + " minuti ?");
                    answerWaitingTime = stdin.readLine();

                    if (answerWaitingTime.equalsIgnoreCase("si")) {

                        // creates a scheduler to plan the periodic execution of tasks
                        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

                        // plans which task execute after a waiting time, and specifies the time unity
                        ScheduledFuture<?> waitTask = scheduler.schedule(this::onWaitComplete, waitingTime, TimeUnit.SECONDS);

                        // waits for the task to complete (the estimated wait time)
                        try {
                            waitTask.get();
                        } catch (InterruptedException | ExecutionException exc) {
                            System.out.println("(Cliente) Errore utilizzo scheduler");
                            throw new RuntimeException(exc);
                        } finally {

                            // deallocates used resources
                            scheduler.shutdown();
                            checkSeats2.close();
                            stdin.close();
                            stdin.close();
                        }
                    }
                    else {
                        System.out.println("(Cliente) Me ne vado!");
                    }
                }
            }
        } catch (IOException exc) {
            System.out.println("(Cliente) Impossibile comunicare con il receptionist");
            throw new RuntimeException(exc);
        }finally {
            // chiudi il socket solo alla fine del processo del cliente
            if (waiterSocket != null && !waiterSocket.isClosed()) {
                try {
                    waiterSocket.close();
                } catch (IOException e) {
                    System.out.println("(Cliente) Errore durante la chiusura del socket del cameriere");
                    throw new RuntimeException(e);
                    // o gestisci l'eccezione in modo appropriato
                }
            }
        }
    }

    private void onWaitComplete() {
        System.out.println("(Cliente) Attesa completata. Riprovo a prendere posto.");
        run();
    }

    // allows customer to say how many seats he needs and to get a table if there are one available and there are enough seats
    public int getTable(Socket receptionSocket) throws IOException {

        // used to gets customer's required seats and to say it to the receptionist
        BufferedReader checkSeats = new BufferedReader(new InputStreamReader(receptionSocket.getInputStream()));
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter sendSeats = new PrintWriter(receptionSocket.getOutputStream(), true);

        // reads customer requested seats
        System.out.println("Benvenuto, di quanti posti hai bisogno?");
        int requiredSeats = Integer.parseInt(stdin.readLine());

        // says how many seats he requires to the receptionist
        sendSeats.println(requiredSeats);

        // gets the table number by the receptionist if it's possible
        int tableNumber = Integer.parseInt(checkSeats.readLine());

        // closes used resources
        checkSeats.close();
        sendSeats.close();
        receptionSocket.close();
        return tableNumber;
    }

    // simulates menu's scanning by the customer and shows it
    public void getMenu() {

        // opens the files that contains the menu in read mode
        try (FileReader fileReader = new FileReader("menu.txt")) {

            // used to get each order and its price
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String order;
            float price;
            System.out.println("Questo è il menù:");

            // shows each menu order and its price on the screen
            while ((order = bufferedReader.readLine()) != null) {
                price = Float.parseFloat(bufferedReader.readLine());
                System.out.println("Ordine: " + order);
                System.out.println("Prezzo: " + price);
            }

            // closes the connection to the file
            bufferedReader.close();
        } catch (Exception exc) {
            System.out.println("(Cliente) Errore scannerizzazione menù");
            throw new RuntimeException(exc);
        }
    }

    // simulates a customer order
    public void getOrder(Socket waiterSocket) throws IOException {

        // used to get a customer's order and to send it to a waiter
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        BufferedReader eatOrder = new BufferedReader(new InputStreamReader(waiterSocket.getInputStream()));
        PrintWriter takeOrder = new PrintWriter(waiterSocket.getOutputStream(), true);

        StringBuilder totalOrdered = new StringBuilder();
        String order;
        float  bill = 0.0f;

        do {

            // gets customer's order
            System.out.println("Scegli un ordine da effettuare oppure digita 'fine' per chiedere il conto");
            order = stdin.readLine();

            // if the customer stops eating
            if (order.equalsIgnoreCase("fine")) {
                takeOrder.println("fine");
                break;
            }
            // if the requested order isn't in the menù
            if(checkOrder(order) < 0.50f) {
                System.out.println("Ordine non presente nel menù");
            } else {

                // sends the order to the waiter
                System.out.println("(Cliente) Attendo che " + order + " sia pronto");
                takeOrder.println(order);

                // waits for the order and eats it
                order = eatOrder.readLine();

                // adds the order to the customer's list and its price to the bill
                totalOrdered.append(order).append("\n");
                bill += checkOrder(order);

                // eats the order
                System.out.println("(Cliente) Mangio " + order);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException exc) {
                    throw new RuntimeException(exc);
                }
            }
        } while (true);

        System.out.println("Lista degli ordini: " + "\n" + totalOrdered);
        System.out.println("(Cliente) Conto: " + bill + '€');

        // closes used resources
        stdin.close();

    }

    // checks if customer's requested order is in the menù and returns true if the order is available and false otherwise
    public float checkOrder(String order) {

        // opens the file that contains the menu in read mode
        try (FileReader fileReader = new FileReader("menu.txt")) {

            // used to read an order from the file
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String menuOrder;
            float price;

            // reads each order stored into the menu while it finds the customer's requested one or until it realizes that it isn't available
            while ((menuOrder = bufferedReader.readLine()) != null){
                price = Float.parseFloat(bufferedReader.readLine());
                if (menuOrder.equals(order)) {
                    return price;
                }
            }

            // closes the connection to the file
            bufferedReader.close();
            return -1;
        } catch (Exception exc) {
            System.out.println("(Cliente) Errore apertura menù");
            throw new RuntimeException(exc);
        }
    }

    public static void main(String[] args) {
        Customer customer = new Customer();
        customer.run();
    }
}
