import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.*;

public class Receptionist {
    final static int PORT = 1313;                                                                   // used for the communication with customers
    final static int MAX_TABLES = 20;                                                               // number of restaurant's tables
    static Random rand = new Random();                                                              // used to generate random numbers
    static int availableSeats = 100;                                                                // number of available seats for customers
    static int availableTables = 20;                                                                // number of available tables for customers
    static int requiredSeats;                                                                       // number of customer's required seats
    static int [] tables = new int[MAX_TABLES];                                                     // 0 in a cell means free table, 1 means occupied table
    static int tableNumber;                                                                         // customer's table number
    static BufferedReader readSeatsNumber;                                                          // used to read customer requested seats
    static PrintWriter giveTableNumber;                                                             // used to assign a table to the customer
    public void run() {
        // creates a socket to communicate with customers
        try (ServerSocket receptionSocket = new ServerSocket(PORT)) {
            while (true) {
                // waits for a customer
                System.out.println("(Reception) In attesa di prenotazioni da clienti");
                Socket acceptedClient = receptionSocket.accept();

                // reads customer's required seats
                readSeatsNumber = new BufferedReader(new InputStreamReader(acceptedClient.getInputStream()));
                giveTableNumber = new PrintWriter(acceptedClient.getOutputStream(), true);
                requiredSeats = Integer.parseInt(readSeatsNumber.readLine());

                // checks if there are enough available tables and seats
                if (availableTables > 0 && availableSeats >= requiredSeats) {

                    //assigns a table to the customer and updates number of available tables and seats
                    assignSeat();

                    // creates a scheduler to plan the periodic releasing of tables
                    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
                    scheduler.schedule(() -> releaseTable(requiredSeats, tableNumber), 5, TimeUnit.SECONDS);
                }
                else {
                    System.out.println("(Reception) Non ci sono abbastanza posti");
                    giveTableNumber.println(-1);

                    // creare una seconda connessione per comunicare il tempo di attesa
                    try (Socket waitingTimeSocket = receptionSocket.accept()) {
                        PrintWriter waitingTimeWriter = new PrintWriter(waitingTimeSocket.getOutputStream(), true);
                        int waitingTime = randomWaitingTime();
                        System.out.println("(Reception) Il tempo finché un tavolo si liberi è " + waitingTime + " minuti");
                        waitingTimeWriter.println(waitingTime);
                    }
                    catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static int randomWaitingTime() {
        Random random = new Random();
        return random.nextInt(30) + 1;
    }

    // allows receptionist to assign a seat to the customer and to update available seats and tables
    public static void assignSeat() {
        availableTables--;
        availableSeats -= requiredSeats;
        do {
            tableNumber = rand.nextInt(MAX_TABLES);
        }
        while (tables[tableNumber] == 1);
        tables[tableNumber] = 1;
        giveTableNumber.println(tableNumber);
        System.out.println("(Reception) Il cliente prende posto");
        System.out.println("(Reception) Numero di posti e tavoli disponibili: " + availableSeats + " " + availableTables);
    }

    // allows the receptionist to release a table and to update available seats and tables
    public static void releaseTable(int requiredSeats, int tableNumber) {
        availableTables++;
        availableSeats += requiredSeats;
        tables[tableNumber] = 0;
        System.out.println("(Receptionist) Ho liberato un tavolo");
        System.out.println("(Reception) Numero di posti e tavoli disponibili: " + availableSeats + " " + availableTables);
    }

    public static void main(String[] args) {
        Receptionist receptionist = new Receptionist();
        receptionist.run();
    }
}
