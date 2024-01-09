import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class PolibiuszServerClient {

    public static void main(String[] args) {
        Thread serverThread = new Thread(() -> {
            try {
                PolibiuszServer.startServer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        Thread clientThread = new Thread(() -> {
            try {
                PolibiuszClient.startClient();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        serverThread.start();
        clientThread.start();
    }
}

class PolibiuszServer {

    public static void startServer() throws IOException {
        DatagramSocket serverSocket = new DatagramSocket(9876);

        System.out.println("Serwer nasłuchuje na porcie 9876...");

        while (true) {
            byte[] receiveData = new byte[1024];

            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);

            String encryptedMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());
            System.out.println("Odebrano zaszyfrowaną wiadomość: " + encryptedMessage);

            String decryptedMessage = decryptPolibiusz(encryptedMessage);

            System.out.println("Odszyfrowana wiadomość: " + decryptedMessage);

            InetAddress clientAddress = receivePacket.getAddress();
            int clientPort = receivePacket.getPort();

            String response = "Odebrano zaszyfrowaną wiadomość. Dziękujemy!";
            byte[] sendData = response.getBytes();

            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
            serverSocket.send(sendPacket);
        }
    }

    private static String decryptPolibiusz(String encryptedMessage) {
        StringBuilder decryptedMessage = new StringBuilder();

        String[] pairs = encryptedMessage.split(" ");
        for (String pair : pairs) {
            if (pair.equals(" ")) {
                decryptedMessage.append(" ");
            } else {
                int row = Integer.parseInt(pair.substring(0, 1));
                int col = Integer.parseInt(pair.substring(1, 2));

                char decryptedChar = ' ';

                if (row == 1) {
                    decryptedChar = (char) ('A' + col - 1);
                } else if (row == 2) {
                    decryptedChar = (char) ('F' + col - 1);
                } else if (row == 3) {
                    decryptedChar = (char) ('L' + col - 1);
                } else if (row == 4) {
                    decryptedChar = (char) ('Q' + col - 1);
                } else if (row == 5) {
                    decryptedChar = (char) ('V' + col - 1);
                }

                decryptedMessage.append(decryptedChar);
            }
        }

        return decryptedMessage.toString();
    }

}

class PolibiuszClient {

    public static void startClient() throws IOException {
        DatagramSocket clientSocket = new DatagramSocket();

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Wprowadź wiadomość do zaszyfrowania: ");
        String message = reader.readLine();

        String encryptedMessage = encryptPolibiusz(message);

        byte[] sendData = encryptedMessage.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getLocalHost(), 9876);
        clientSocket.send(sendPacket);

        byte[] receiveData = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        clientSocket.receive(receivePacket);

        String serverResponse = new String(receivePacket.getData(), 0, receivePacket.getLength());
        System.out.println("Odpowiedź od serwera: " + serverResponse);

        clientSocket.close();
    }

    private static String encryptPolibiusz(String message) {
        message = message.toUpperCase();

        StringBuilder encryptedMessage = new StringBuilder();

        for (char ch : message.toCharArray()) {
            if (ch == ' ') {
                encryptedMessage.append(" ");
            } else {
                int row = -1, col = -1;

                if (ch >= 'A' && ch <= 'E') {
                    row = 1;
                    col = ch - 'A' + 1;
                } else if (ch >= 'F' && ch <= 'K') {
                    row = 2;
                    col = ch - 'F' + 1;
                } else if (ch >= 'L' && ch <= 'P') {
                    row = 3;
                    col = ch - 'L' + 1;
                } else if (ch >= 'Q' && ch <= 'U') {
                    row = 4;
                    col = ch - 'Q' + 1;
                } else if (ch >= 'V' && ch <= 'Z') {
                    row = 5;
                    col = ch - 'V' + 1;
                }

                if (row != -1 && col != -1) {
                    encryptedMessage.append(row).append(col).append(" ");
                }
            }
        }

        return encryptedMessage.toString().trim();
    }

}
