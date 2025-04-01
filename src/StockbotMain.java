import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class StockBotMain {
    public static void main(String[] args) {
        try {
            // Set system look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Launch the application on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            StockBotGUI gui = new StockBotGUI();
            gui.setVisible(true);
        });
    }
}