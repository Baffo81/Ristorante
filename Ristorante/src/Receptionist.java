import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.Semaphore;

public class Receptionist {
    public static void main(String[] args) {
        final int PORT = 1313; // number of the port on which communicates with customers
        final Semaphore availableSeats = new Semaphore(100); // number of available seats for customers

        try (ServerSocket receptionSocket = new ServerSocket(PORT)) {
            while (true) {
                System.out.println("(Reception) In attesa di prenotazioni da clienti");

                Socket acceptedClient = receptionSocket.accept();
                BufferedReader reader = new BufferedReader(new InputStreamReader(acceptedClient.getInputStream()));
                PrintWriter writer = new PrintWriter(acceptedClient.getOutputStream(), true);

                int requiredSeats = Integer.parseInt(reader.readLine());

                if (availableSeats.tryAcquire(requiredSeats))
                {
                    System.out.println("(Reception) Il cliente prende posto");
                    writer.println(true);
                    System.out.println("(Reception) Numero dei posti disponibili:" + availableSeats.availablePermits());
                    availableSeats.release(requiredSeats);
                }
                else
                {
                    System.out.println("(Reception) Non ci sono abbastanza posti");
                    writer.println(false);
                    // Creare una seconda connessione per comunicare il tempo di attesa
                    try (Socket waitingTimeSocket = receptionSocket.accept()) {
                        PrintWriter waitingTimeWriter = new PrintWriter(waitingTimeSocket.getOutputStream(), true);
                        int waitingTime = RandomWaitingTime();
                        System.out.println("(Reception) Il tempo finché un tavolo si liberi è " + waitingTime + " minuti");
                        waitingTimeWriter.println(waitingTime);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                }
            } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static int RandomWaitingTime() {
        Random random = new Random();
        return random.nextInt(30) + 1;
    }
}
