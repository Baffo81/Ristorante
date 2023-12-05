import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Semaphore;

public class Cameriere extends Thread {
    final int PORT_TO_CLIENT = 1313; // Port's number used for the connection to the client
    final int PORT_TO_CHEF = 1314;   // Port's number used for the connection to the chef
    final Semaphore postiSemaforo = new Semaphore(100);

    public void run() {
        try (Socket socketToChef = new Socket(InetAddress.getLocalHost(), PORT_TO_CHEF)) {
            // Object to read chef's information
            ObjectInputStream readerToChef = new ObjectInputStream(socketToChef.getInputStream());

            while (true) {
                // Waiter waits for the menu
                Menu menu = (Menu) readerToChef.readObject();
                System.out.println("(Cameriere" + Thread.currentThread().threadId() + ")" + "Prendo il menù dallo chef");
                menu.showMenu();

                // Tries to create a socket with the specified port's number to communicate with the customer
                try (ServerSocket socketToClient = new ServerSocket(PORT_TO_CLIENT)) {
                    while (true) {
                        // Waiter waits for a customer
                        System.out.println("(Cameriere" + Thread.currentThread().threadId()+ ")" + "In attesa di clienti");

                        // Arrives a customer
                        Socket acceptedClient = socketToClient.accept();
                        System.out.println("(Cameriere" + Thread.currentThread().threadId() + ")" + "Prendo la prenotazione del cliente");

                        // Objects for reading and writing over the socket
                        BufferedReader readerToClient = new BufferedReader(new InputStreamReader(acceptedClient.getInputStream()));
                        PrintWriter writerToClient = new PrintWriter(acceptedClient.getOutputStream(), true);

                        // Gets how many seats the customer requires
                        int requiredSeats = Integer.parseInt(readerToClient.readLine());

                        // If there are enough available seats, the customer can enter the restaurant and take them
                        if (postiSemaforo.tryAcquire(requiredSeats)) {
                            // There is one available waiter in less
                            System.out.println("(Cameriere" + Thread.currentThread().threadId() + ")" + "Il cliente prende posto");
                            writerToClient.println(true);

                            System.out.println("(Cameriere" + Thread.currentThread().threadId() + ")" + "Numero dei posti aggiornati:" + postiSemaforo.availablePermits());

                            // Sends menu to the client
                            System.out.println("(Cameriere" + Thread.currentThread().threadId() + ")" + "Fornisco il menù al cliente");
                            try (ObjectOutputStream menuWriter = new ObjectOutputStream(acceptedClient.getOutputStream())) {
                                menuWriter.writeObject(menu);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            // The client exits the restaurant and its seats are now free
                            postiSemaforo.release(requiredSeats);
                        } else {
                            System.out.println("(Cameriere) Non ci sono abbastanza posti");
                            writerToClient.println(false);
                        }
                    }
                } catch (IOException exc) {
                    System.out.println("(Server) Errore creazione socket o impossibile connettersi al cliente");
                    exc.printStackTrace();
                }
            }
        } catch (IOException | ClassNotFoundException exc) {
            System.out.println("(Server) Errore durante la comunicazione con lo chef");
            exc.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Cameriere cameriere1 = new Cameriere();
        cameriere1.start();
    }
}
