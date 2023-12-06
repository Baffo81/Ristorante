import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Cuoco
{
    public static void main(String[] args)
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
        try (ServerSocket serverSocket = new ServerSocket(PORT))
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
        }
    }

    public static void writeMenu()
    {
        //tries to open the file in read mode
        try (FileWriter fileWriter = new FileWriter("menu.txt"))
        {
            String order,                                       //order's name
                   status;                                      //"S" if the chef wants to add another order into the menu and "N" otherwise
            float price;                                        //order's price
            Scanner scanner = new Scanner(System.in);           //object to read from the stdin
            PrintWriter writer = new PrintWriter(fileWriter);   //object to write into the file

            //the chef writes the menu
            do
            {
                //reads order's name
                do
                {
                    System.out.println("Quale ordine desideri scrivere nel menù?");
                    order = scanner.nextLine();
                }
                while(order.isEmpty());

                //reads order's price
                do
                {
                    System.out.println("Inserisci il prezzo dell'ordine");
                    price = Float.parseFloat(scanner.next());
                }
                while (price < 0.50f);

                //writes order's name and order's price into the file separated by a line
                writer.println(order);
                writer.println(price);

                //checks if the chef wants to add another order into the menù
                System.out.println("Desideri inserire un altro ordine nel menù? (S/N)");
                status = scanner.next();
                scanner.nextLine();
            }
            while (status.equalsIgnoreCase("s"));
        }
        catch (IOException exc)
        {
            System.out.println("(Cuoco) Impossibile connettersi al file");
        }
    }
}