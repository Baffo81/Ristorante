import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Waiter {
    static final int PORT_TO_CUSTOMER = 1316;

    public static void main(String[] args) {
        Waiter waiter = new Waiter();
        waiter.waiter();
    }

    public void waiter() {
        try (ServerSocket serverSocket = new ServerSocket(PORT_TO_CUSTOMER)) {
            while (true) {
                System.out.println("(CapoSala) Attendo l'ordine del cliente");
                Socket acceptedCustomer = serverSocket.accept();
                System.out.println("(CapoSala) Comando allo sguattero di inviare l'ordine allo chef");
                startWaiterSonProcess(acceptedCustomer);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void startWaiterSonProcess(Socket customerSocket) throws IOException {
        try {
            // Ottieni gli stream di input/output del processo figlio
            ProcessBuilder processBuilder = new ProcessBuilder("java", "WaiterSon");
            Process waiterSonProcess = processBuilder.start();

            // Invia le informazioni della socket al processo figlio
            OutputStream outputStream = waiterSonProcess.getOutputStream();
            outputStream.write((customerSocket.getInetAddress() + "\n").getBytes());
            outputStream.write((customerSocket.getPort() + "\n").getBytes());
            outputStream.flush();

            // Attendere che il processo figlio sia pronto per ricevere l'ordine
            InputStream inputStream = waiterSonProcess.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            // Leggi il messaggio di prontezza dal processo figlio
            String readyMessage = reader.readLine();
            if (!"READY".equals(readyMessage)) {
                throw new RuntimeException("Il processo figlio non è pronto");
            }

            // Chiudi l'output stream per indicare al processo figlio che tutte le informazioni sono state inviate
            outputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
