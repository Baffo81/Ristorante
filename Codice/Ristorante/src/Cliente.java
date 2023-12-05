import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.Semaphore;

public class Cliente extends Thread
{
    private static final Object lock = new Object();
    final int PORT         = 1313;                  //port's number used for the connection to the waiter
    public void run()
    {
        int     requiredSeats  = randomNumber();     //number of required seats
        boolean answer;                              //true if there are available seats and false otherwise
        Menu menu;
        final Semaphore postiSemaforo = new Semaphore(100);

        try {

                System.out.println("(Cliente" + Thread.currentThread().getId() + ")" + "Attendo la disponibilità di posti");

                //tries to create a socket with specified server's address and port's number to communicate with the waiter
                try (Socket socket = new Socket(InetAddress.getLocalHost(), PORT)) {
                    //objects for reading and writing over the socket
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

                    //Gestisco la richiesta dei posti dei vari clienti
                    synchronized (lock) {

                        postiSemaforo.acquire(requiredSeats);
                        //says how many seats he needs
                        System.out.println("(Cliente" + Thread.currentThread().threadId() + ")" + " Mi servono " + requiredSeats + " posti");
                        writer.println(requiredSeats);

                        //gets waiter response
                        answer = Boolean.parseBoolean(reader.readLine());

                        //if there are available seats, the customer takes them
                        if (answer) {
                            System.out.println("(Cliente" + Thread.currentThread().threadId() + ")" + "Prendo posto e richiedo il menù");

                            try (ObjectInputStream readerObject = new ObjectInputStream(socket.getInputStream())) {
                                menu = (Menu) readerObject.readObject();
                                System.out.println("(Cliente" + Thread.currentThread().threadId() + ")" + "Il menù è ");
                                menu.showMenu();
                            } catch (IOException | ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                        } else {
                            System.out.println("(Cliente" + Thread.currentThread().threadId() + ")" + "Me ne vado!");
                        }

                        postiSemaforo.release(requiredSeats);
                    }
                } catch (IOException exc) {
                    System.out.println("(Cliente" + Thread.currentThread().threadId() + ")" + "Errore creazione socket o impossibile connettersi al server");
                }
        }
        catch (Exception exc)
        {
            System.out.println("(Cliente" + Thread.currentThread().threadId() + ")" + "Errore interno");
        }
    }

    //generates a random number that indicates how many seats are required by the customer
    public static int randomNumber()
    {
        Random random = new Random();
        return random.nextInt(10) + 1;
    }

    public static void main(String[] args)
    {
        Cliente cliente1 = new Cliente();
        Cliente cliente2 = new Cliente();
        cliente1.start();
        cliente2.start();
    }
}
