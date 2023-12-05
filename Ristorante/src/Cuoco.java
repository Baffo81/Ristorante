import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Cuoco
{
    public static void main(String [] args)
    {
        final int PORT = 1314;              //number of the port used to communicate with waiters
        Socket accepted;                    //specifies which waiter is communicating with the chef
        BufferedReader orderReader;         //used to receive request of preparing an order by waiters
        PrintWriter orderWriter;            //used to send prepared orders to waiters
        String order;                       //requested order by a waiter

        //writes the menù
        System.out.println("(Cuoco) Scrivo il menù");
        writeMenu();

        //creates a server socket with the specified port to communicate with waiters
        try(ServerSocket serverSocket = new ServerSocket(PORT))
        {
            //keeps cooking clients' orders and sending them to waiters
            while (true)
            {
                //waits for an order request by a waiter
                System.out.println("(Cuoco) Attendo un ordine");
                accepted = serverSocket.accept();

                //declares objects to use to read and write an order through the socket
                orderReader = new BufferedReader(new InputStreamReader(accepted.getInputStream()));
                orderWriter = new PrintWriter(accepted.getOutputStream(), true);

                //reads an order, prepares it and sends it back to the waiter
                order = orderReader.readLine();
                System.out.println("(Cuoco) Preparo l'ordine e lo invio al cameriere");
                orderWriter.write(order);
                accepted.close();
            }
        }
        catch (IOException exc)
        {
            System.out.println("(Server) Errore creazione socket o impossibile connettersi al cameriere");
            exc.printStackTrace();
        }
    }

    public static void writeMenu()
    {
        //name of the file that contains the menu
        String fileName = "src/menu.txt";

        //tries to open the file in read mode
        try (FileWriter fileWriter = new FileWriter(fileName))
        {
            //reads each menu order and prints them on the screen
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write("Pasta al sugo");
            bufferedWriter.newLine();

            //close the connection to the file
            bufferedWriter.close();
        }
        catch (Exception exc)
        {
            System.out.println("Errore connessione al file");
            exc.printStackTrace();
        }
    }
}
