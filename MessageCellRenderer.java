import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MessageCellRenderer extends JPanel implements ListCellRenderer<ChatMessage> {

    private final JTextArea textArea = new JTextArea();
    private final JLabel timestampLabel = new JLabel();
    private final JPanel bubblePanel = new JPanel(new BorderLayout(0, 3));
    private final JPanel wrapperPanel = new JPanel(); // Will use BoxLayout

    private final JLabel systemMessageLabel = new JLabel();

    public MessageCellRenderer() {
        super(new BorderLayout());

        // Use BoxLayout for the wrapper panel to easily align left or right
        wrapperPanel.setLayout(new BoxLayout(wrapperPanel, BoxLayout.LINE_AXIS));

        // --- Setup for Bubble Messages ---
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        timestampLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        timestampLabel.setForeground(new Color(200, 200, 200));
        bubblePanel.add(textArea, BorderLayout.CENTER);
        bubblePanel.add(timestampLabel, BorderLayout.SOUTH);
        bubblePanel.setBorder(new RoundedBorder(15));
        
        // --- Setup for System Messages ---
        systemMessageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        systemMessageLabel.setForeground(new Color(170, 170, 170));
        systemMessageLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));

        this.add(wrapperPanel, BorderLayout.CENTER);
        this.add(systemMessageLabel, BorderLayout.SOUTH);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends ChatMessage> list, ChatMessage message, int index,
                                                  boolean isSelected, boolean cellHasFocus) {

        // Set the background of the entire cell
        this.setBackground(list.getBackground());
        wrapperPanel.setBackground(list.getBackground());

        // Handle system messages (no bubble)
        if (message.getTimestamp().isEmpty()) {
            systemMessageLabel.setText("<html>" + message.getContent() + "</html>");
            systemMessageLabel.setVisible(true);
            wrapperPanel.setVisible(false); // Hide the bubble panel
        } else {
            // Handle regular messages (with bubble)
            systemMessageLabel.setVisible(false); // Hide the system message label
            wrapperPanel.setVisible(true);

            textArea.setText(message.getContent());
            timestampLabel.setText(message.getTimestamp());

            // Clear the wrapper panel before adding new components
            wrapperPanel.removeAll();

            if (message.isSentByUser()) {
                // Your messages: Align RIGHT
                bubblePanel.setBackground(new Color(0, 132, 255));
                textArea.setForeground(Color.WHITE);
                textArea.setBackground(new Color(0, 132, 255));
                timestampLabel.setHorizontalAlignment(SwingConstants.RIGHT);

                // Add "glue" to the left, pushing the bubble to the right
                wrapperPanel.add(Box.createHorizontalGlue());
                wrapperPanel.add(bubblePanel);
            } else {
                // Others' messages: Align LEFT
                bubblePanel.setBackground(new Color(50, 50, 50));
                textArea.setForeground(Color.WHITE);
                textArea.setBackground(new Color(50, 50, 50));
                timestampLabel.setHorizontalAlignment(SwingConstants.LEFT);

                // Add the bubble to the left, and "glue" to the right
                wrapperPanel.add(bubblePanel);
                wrapperPanel.add(Box.createHorizontalGlue());
            }

            // Set a max size for the bubble so it wraps nicely instead of filling the whole width
            int maxWidth = (int)(list.getWidth() * 0.7); // 70% of the list's width
            bubblePanel.setMaximumSize(new Dimension(maxWidth, Short.MAX_VALUE));
        }

        return this;
    }
}