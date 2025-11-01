import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ChatClientGUI {
    // --- (Fields are mostly the same) ---
    private JFrame frame;
    private JList<ChatMessage> messageList;
    private DefaultListModel<ChatMessage> messageModel;
    private JTextField inputField;
    private JButton sendButton;
    private JList<String> userList;
    private DefaultListModel<String> userListModel;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String name;

    // --- NEW FIELDS for Typing Indicator ---
    private JLabel typingStatusLabel;
    private Timer typingTimer;
    private boolean isTyping = false;
    private final Set<String> usersTyping = new HashSet<>();

    // ... (Color constants are the same)
    private static final Color INSTA_DARK_BG = new Color(18, 18, 18);
    private static final Color INSTA_PINK = new Color(225, 48, 108);
    private static final Color TEXT_COLOR_LIGHT = Color.WHITE;

    public ChatClientGUI(String serverAddress, int serverPort) {
        name = JOptionPane.showInputDialog(frame, "Enter your name:", "InstaChat Login", JOptionPane.PLAIN_MESSAGE);
        if (name == null || name.trim().isEmpty()) System.exit(0);

        try {
            socket = new Socket(serverAddress, serverPort);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.println(name);

            frame = new JFrame("InstaChat - " + name);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
            mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
            mainPanel.setBackground(INSTA_DARK_BG);
            
            // ... (Header, Message List, User List, Split Pane are the same)
            JLabel headerLabel = new JLabel("Chatting with Everyone", SwingConstants.CENTER);
            headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            headerLabel.setForeground(TEXT_COLOR_LIGHT);
            mainPanel.add(headerLabel, BorderLayout.NORTH);

            messageModel = new DefaultListModel<>();
            messageList = new JList<>(messageModel);
            messageList.setCellRenderer(new MessageCellRenderer());
            messageList.setBackground(INSTA_DARK_BG);
            JScrollPane messageScrollPane = new JScrollPane(messageList);
            messageScrollPane.setBorder(BorderFactory.createEmptyBorder());

            userListModel = new DefaultListModel<>();
            userList = new JList<>(userListModel);
            userList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            userList.setBackground(new Color(30, 30, 30));
            userList.setForeground(TEXT_COLOR_LIGHT);
            userList.setCellRenderer(new UserStatusCellRenderer());
            
            JScrollPane userScrollPane = new JScrollPane(userList);
            userScrollPane.setBorder(BorderFactory.createTitledBorder("Online Users"));
            ((javax.swing.border.TitledBorder) userScrollPane.getBorder()).setTitleColor(TEXT_COLOR_LIGHT);
            userScrollPane.setPreferredSize(new Dimension(150, 0));

            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, messageScrollPane, userScrollPane);
            splitPane.setDividerLocation(350);
            splitPane.setBorder(null);
            splitPane.setUI(new javax.swing.plaf.basic.BasicSplitPaneUI());
            mainPanel.add(splitPane, BorderLayout.CENTER);

            // --- Bottom Panel now includes the typing status label ---
            JPanel bottomPanel = new JPanel(new BorderLayout(10, 5));
            bottomPanel.setBackground(INSTA_DARK_BG);
            
            typingStatusLabel = new JLabel(" ");
            typingStatusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            typingStatusLabel.setForeground(new Color(170, 170, 170));
            bottomPanel.add(typingStatusLabel, BorderLayout.NORTH);

            JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
            inputPanel.setBackground(INSTA_DARK_BG);
            inputField = new JTextField();
            inputField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            inputField.setBorder(new RoundedBorder(15));
            inputField.setBackground(new Color(36, 36, 36));
            inputField.setForeground(TEXT_COLOR_LIGHT);
            inputField.setCaretColor(TEXT_COLOR_LIGHT);
            sendButton = new JButton("Send");
            sendButton.setBackground(INSTA_PINK);
            sendButton.setForeground(TEXT_COLOR_LIGHT);
            sendButton.setFocusPainted(false);
            sendButton.setBorderPainted(false);
            sendButton.setOpaque(true);
            inputPanel.add(inputField, BorderLayout.CENTER);
            inputPanel.add(sendButton, BorderLayout.EAST);
            bottomPanel.add(inputPanel, BorderLayout.CENTER);
            
            mainPanel.add(bottomPanel, BorderLayout.SOUTH);

            frame.setContentPane(mainPanel);
            frame.setSize(650, 600);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            // --- Setup Listeners and Timers for Typing Indicator ---
            setupTypingIndicator();
            sendButton.addActionListener(e -> sendMessage());
            inputField.addActionListener(e -> sendMessage());

            new Thread(new IncomingReader()).start();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Could not connect to the server. Is it running?", "Connection Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }
    
    private void setupTypingIndicator() {
        // Timer to send "stopped typing" message after 1.5 seconds of inactivity
        typingTimer = new Timer(1500, e -> {
            out.println("[STOPPED_TYPING]" + name);
            isTyping = false;
        });
        typingTimer.setRepeats(false);

        // Listener to detect when user is typing in the input field
        inputField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { handleTyping(); }
            @Override
            public void removeUpdate(DocumentEvent e) { handleTyping(); }
            @Override
            public void changedUpdate(DocumentEvent e) { handleTyping(); }
        });
    }

    private void handleTyping() {
        if (!isTyping) {
            out.println("[TYPING]" + name);
            isTyping = true;
        }
        typingTimer.restart(); // Restart timer on every keystroke
    }

    private void sendMessage() {
        String message = inputField.getText();
        if (!message.isEmpty()) {
            typingTimer.stop(); // Stop the timer when message is sent
            out.println("[STOPPED_TYPING]" + name);
            isTyping = false;
            
            try {
                String timestamp = new java.text.SimpleDateFormat("h:mm a").format(new java.util.Date());
                messageModel.addElement(new ChatMessage(name + ": " + message, timestamp, true));
                out.println(CryptoUtils.encrypt(message));
            } catch (Exception e) {
                e.printStackTrace();
                messageModel.addElement(new ChatMessage("Error sending message.", "", true));
            }
            inputField.setText("");
            messageList.ensureIndexIsVisible(messageModel.getSize() - 1);
        }
    }

    private void updateTypingStatus() {
        if (usersTyping.isEmpty()) {
            typingStatusLabel.setText(" ");
        } else {
            String typingText = usersTyping.stream().collect(Collectors.joining(", "))
                                + (usersTyping.size() > 1 ? " are typing..." : " is typing...");
            typingStatusLabel.setText(typingText);
        }
    }

    private class IncomingReader implements Runnable {
        public void run() {
            try {
                String serverMessage;
                while ((serverMessage = in.readLine()) != null) {
                    final String finalMessage = serverMessage;
                    SwingUtilities.invokeLater(() -> {
                        if (finalMessage.startsWith("[USERLIST]")) {
                            String userNames = finalMessage.substring(10);
                            userListModel.clear();
                            Arrays.stream(userNames.split(",")).forEach(userListModel::addElement);
                        } else if (finalMessage.startsWith("[TYPING]")) {
                            String typingUser = finalMessage.substring(8);
                            usersTyping.add(typingUser);
                            updateTypingStatus();
                        } else if (finalMessage.startsWith("[STOPPED_TYPING]")) {
                            String stoppedTypingUser = finalMessage.substring(16);
                            usersTyping.remove(stoppedTypingUser);
                            updateTypingStatus();
                        }
                        else if (finalMessage.contains("joined the chat") || finalMessage.contains("left the chat")) {
                            messageModel.addElement(new ChatMessage(finalMessage, "", false));
                        } else {
                            try {
                                String[] parts = finalMessage.split("\\|", 3);
                                String senderName = parts[0];
                                String timestamp = parts[1];
                                String encryptedPayload = parts[2];
                                
                                String decryptedMsg = senderName + ": " + CryptoUtils.decrypt(encryptedPayload);
                                messageModel.addElement(new ChatMessage(decryptedMsg, timestamp, false));
                            } catch (Exception e) {
                                messageModel.addElement(new ChatMessage("Could not decrypt message.", "", false));
                            }
                        }
                        messageList.ensureIndexIsVisible(messageModel.getSize() - 1);
                    });
                }
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> {
                    messageModel.addElement(new ChatMessage("Connection closed.", "", false));
                });
            }
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> new ChatClientGUI("localhost", 1234));
    }
}