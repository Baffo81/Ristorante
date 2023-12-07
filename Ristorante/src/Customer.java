import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.*;

public class Customer {
    private final Scanner scanner = new Scanner(System.in);
    final int PORT_TO_RECEPTION = 1313;     // used for the communication with the receptionist and to get customer's requested seats
    final int PORT_TO_EMPLOYEE = 1314;      // used for the communication with employees and to request orders
    final int PORT_TO_WAITER = 1316;        // used for the communication with waiters and to get orders

    public void run() {
        int requiredSeats,                      // number of required seats
                waitingTime;                    // number of waitingTime
        boolean answer;                         // true if there are available seats and false otherwise
        String order,                           // requested order by the customer
                answerWaitingTime;              // used to check if the user wants waiting

        // tries to create a socket with specified server's address and port's number to communicate with the waiter
        try (Socket receptionSocket = new Socket(InetAddress.getLocalHost(), PORT_TO_RECEPTION)) {
            // objects for reading and writing through the socket
            BufferedReader checkSeats = new BufferedReader(new InputStreamReader(receptionSocket.getInputStream()));
            PrintWriter seatsWriter = new PrintWriter(receptionSocket.getOutputStream(), true);

            // says how many seats he needs
            requiredSeats = getRequiredSeats();
            System.out.println("(Cliente) Mi servono " + requiredSeats + " posti");

            // gets waiter response
            seatsWriter.println(requiredSeats);
            answer = Boolean.parseBoolean(checkSeats.readLine());
            receptionSocket.close();

            // if there are available seats, the customer takes them
            if (answer) {
                int TABLENUMBER = RandomTableNumber();
                // gets the menù
                System.out.println("(Cliente) Prendo posto al tavolo " + TABLENUMBER + " e scannerizzo il menù");

                // keeps ordering and eating
                while (true) {
                    try (Socket waiterSocket = new Socket(InetAddress.getLocalHost(), PORT_TO_WAITER)) {
                        // objects for reading and writing through the socket
                        BufferedReader orderReader = new BufferedReader(new InputStreamReader(waiterSocket.getInputStream()));
                        PrintWriter orderWriter = new PrintWriter(waiterSocket.getOutputStream(), true);

                        // orders, wait for the order and eats it
                        System.out.println("(Cliente) Effettuo un'ordinazione");
                        order = getOrder();
                        System.out.println("(Cliente) Ordino " + order + " al tavolo" + TABLENUMBER);
                        orderWriter.println(order + "- Tavolo" + TABLENUMBER);
                        order = orderReader.readLine();
                        System.out.println("(Cliente) Mangio " + order);
                    }
                }
            }

            // otherwise, he goes away
            else {
                try (Socket receptionSocket2 = new Socket(InetAddress.getLocalHost(), PORT_TO_RECEPTION)) {
                    // objects for reading and writing through the socket
                    BufferedReader checkSeats2 = new BufferedReader(new InputStreamReader(receptionSocket2.getInputStream()));

                    // decides if waiting or not
                    waitingTime = Integer.parseInt(checkSeats2.readLine());

                    System.out.println("(Reception) Vuoi attendere " + waitingTime + " minuti ?");
                    answerWaitingTime = scanner.next();

                    if (answerWaitingTime.equalsIgnoreCase("si")) {
                        System.out.println("(Cliente) Attendo " + waitingTime + " minuti");

                        // creates a scheduler to plan the periodic execution of tasks
                        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

                        // plans which task execute after a waiting time, and specifies the time unity
                        ScheduledFuture<?> waitTask = scheduler.schedule(this::onWaitComplete, waitingTime, TimeUnit.SECONDS);

                        // waits for the task to complete (the estimated wait time)
                        try {
                            waitTask.get(); // restituisce la fine della task (è bloccante)
                        }
                        catch (InterruptedException | ExecutionException e) {
                            throw new RuntimeException(e);
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
            System.out.println("(Client) Errore creazione socket o impossibile connettersi al server");
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
        return scanner.nextInt();
    }

    public String getOrder() {
        // customer's order
        String order;

        // show the menu to the customer
        getMenu();

        // gets customer's order
        System.out.println("Scegli un ordine da effettuare");
        order = scanner.nextLine();

        // checks if customer's requested order is in the menù
        while (!checkOrder(order)) {
            System.out.println("Ordine non disponibile, scegline un altro");
            order = scanner.nextLine();
        }

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
            System.out.println("Errore connessione al file");
            return false;
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
            System.out.println("Errore scannerizzazione menù");
        }
    }

    // generates table number
    public int RandomTableNumber() {
        Random random = new Random();
        return random.nextInt(50) + 1;
    }

    public static void main(String[] args) {
            Customer client = new Customer();
            client.run();
    }
}
