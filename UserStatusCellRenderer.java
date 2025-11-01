import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class UserStatusCellRenderer extends JLabel implements ListCellRenderer<String> {

    private final ImageIcon onlineIcon;

    public UserStatusCellRenderer() {
        onlineIcon = createStatusIcon(new Color(34, 197, 94)); // A nice green color
        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends String> list, String username, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        
        setText(username);
        setIcon(onlineIcon);
        
        if (isSelected) {
            setBackground(new Color(50, 50, 50));
            setForeground(Color.WHITE);
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        return this;
    }

    private ImageIcon createStatusIcon(Color color) {
        int size = 8;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(color);
        g2d.fillOval(0, 0, size, size);
        g2d.dispose();
        return new ImageIcon(image);
    }
}