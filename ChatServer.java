import java.io.*;
import java.net.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ChatServer {
    private static final int PORT = 1234;
    private static Set<ClientHandler> clientHandlers = new HashSet<>();
    private static final String DATABASE_URL = "jdbc:sqlite:chat_history.db";

    public static void main(String[] args) {
        setupDatabase();
        System.out.println("Chat Server started on port " + PORT);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                new ClientHandler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void broadcastUserList() {
        synchronized (clientHandlers) {
            String userList = clientHandlers.stream()
                                            .map(client -> client.name)
                                            .filter(name -> name != null && !name.isEmpty())
                                            .collect(Collectors.joining(","));
            String message = "[USERLIST]" + userList;
            for (ClientHandler client : clientHandlers) {
                client.sendMessage(message);
            }
        }
    }

    private static void broadcast(String message, ClientHandler excludeClient) {
        // Log the message to the database only if it's a chat message
        if (message.contains("|")) {
             try {
                String[] parts = message.split("\\|", 3);
                String senderName = parts[0];
                String actualEncryptedMessage = parts[2];
                logMessage(senderName, actualEncryptedMessage);
            } catch(Exception e) {
                System.out.println("Could not log message: " + e.getMessage());
            }
        }
        
        synchronized (clientHandlers) {
            for (ClientHandler client : clientHandlers) {
                if (client != excludeClient) {
                    client.sendMessage(message);
                }
            }
        }
    }

    private static void setupDatabase() {
        String sql = "CREATE TABLE IF NOT EXISTS messages ("
                   + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                   + "username TEXT NOT NULL,"
                   + "message TEXT NOT NULL,"
                   + "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP"
                   + ");";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Database setup complete. Table 'messages' is ready.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
    private static void logMessage(String username, String message) {
         String sql = "INSERT INTO messages(username, message) VALUES(?,?)";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, message);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // --- This inner class MUST be inside the ChatServer class ---
    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String name;

        public ClientHandler(Socket socket) { this.socket = socket; }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                name = in.readLine();
                if (name == null) return;
                
                synchronized (clientHandlers) { clientHandlers.add(this); }

                System.out.println(name + " joined the chat.");
                broadcast(name + " has joined the chat!", this);
                broadcastUserList();

                String line;
                while ((line = in.readLine()) != null) {
                    if (line.equalsIgnoreCase("exit")) break;

                    if (line.startsWith("[TYPING]") || line.startsWith("[STOPPED_TYPING]")) {
                        broadcast(line, this);
                    } else {
                        String timestamp = new SimpleDateFormat("h:mm a").format(new Date());
                        String messageToSend = name + "|" + timestamp + "|" + line;
                        broadcast(messageToSend, this);
                    }
                }
            } catch (IOException e) {
                System.out.println("Client " + name + " error: " + e.getMessage());
            } finally {
                if (name != null) {
                    System.out.println(name + " left the chat.");
                    synchronized (clientHandlers) { clientHandlers.remove(this); }
                    broadcast(name + " has left the chat!", this);
                    broadcastUserList();
                }
                try { socket.close(); } catch (IOException e) {}
            }
        }
        
        public void sendMessage(String message) { out.println(message); }
    }
}