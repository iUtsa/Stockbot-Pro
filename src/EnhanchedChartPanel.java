import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

/**
 * Custom chart panel for drawing financial charts.
 * This is a simplified implementation that could be extended with more features.
 */
public class EnhancedChartPanel extends JPanel {
    
    private ArrayList<Double> prices;
    private ArrayList<Double> indicators;
    private ArrayList<String> labels;
    private double minPrice, maxPrice;
    private int mouseX = -1;
    private String chartTitle = "Stock Price Chart";
    
    // Chart rendering parameters
    private int padding = 50;
    private int labelPadding = 25;
    private int pointWidth = 4;
    private Color gridColor = new Color(200, 200, 200, 200);
    private Color priceLineColor = new Color(44, 102, 230, 180);
    private Color indicatorLineColor = new Color(230, 44, 44, 180);
    private Color pointColor = new Color(100, 100, 100, 180);
    private Color crosshairColor = new Color(100, 100, 100, 100);
    
    /**
     * Constructor for the chart panel
     */
    public EnhancedChartPanel() {
        this.prices = new ArrayList<>();
        this.indicators = new ArrayList<>();
        this.labels = new ArrayList<>();
        
        // Add mouse listeners for crosshair
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mouseX = e.getX();
                repaint();
            }
        });
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                mouseX = -1;
                repaint();
            }
        });
    }
    
    /**
     * Set data for the chart
     * @param prices Price data
     * @param indicators Indicator data (MA, RSI, etc.)
     * @param labels Date labels
     */
    public void setData(ArrayList<Double> prices, ArrayList<Double> indicators, ArrayList<String> labels) {
        this.prices = prices;
        this.indicators = indicators;
        this.labels = labels;
        
        // Calculate min and max values for scaling
        minPrice = Double.MAX_VALUE;
        maxPrice = Double.MIN_VALUE;
        
        for (Double price : prices) {
            minPrice = Math.min(minPrice, price);
            maxPrice = Math.max(maxPrice, price);
        }
        
        // Add some padding to min/max
        double padding = (maxPrice - minPrice) * 0.1;
        minPrice -= padding;
        maxPrice += padding;
        
        repaint();
    }
    
    /**
     * Set the chart title
     * @param title The chart title
     */
    public void setChartTitle(String title) {
        this.chartTitle = title;
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Get dimensions
        int width = getWidth();
        int height = getHeight();
        
        // Draw chart background
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, width, height);
        
        // If no data, show message and return
        if (prices.isEmpty()) {
            g2.setColor(Color.BLACK);
            g2.setFont(new Font("Arial", Font.PLAIN, 18));
            String message = "No data available";
            int messageWidth = g2.getFontMetrics().stringWidth(message);
            g2.drawString(message, (width - messageWidth) / 2, height / 2);
            return;
        }
        
        // Calculate chart dimensions
        int chartWidth = width - 2 * padding;
        int chartHeight = height - 2 * padding;
        
        // Draw chart title
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.BOLD, 18));
        int titleWidth = g2.getFontMetrics().stringWidth(chartTitle);
        g2.drawString(chartTitle, (width - titleWidth) / 2, 30);
        
        // Draw Y-axis
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.PLAIN, 12));
        int ySteps = 5;
        for (int i = 0; i <= ySteps; i++) {
            double value = minPrice + (maxPrice - minPrice) * i / ySteps;
            String label = String.format("%.2f", value);
            int labelWidth = g2.getFontMetrics().stringWidth(label);
            int y = height - padding - (int) (chartHeight * i / ySteps);
            
            // Draw grid line
            g2.setColor(gridColor);
            g2.drawLine(padding, y, width - padding, y);
            
            // Draw label
            g2.setColor(Color.BLACK);
            g2.drawString(label, padding - labelWidth - 5, y + 5);
        }
        
        // Draw X-axis
        int numLabels = Math.min(10, labels.size());
        int step = labels.size() / numLabels;
        if (step == 0) step = 1;
        
        for (int i = 0; i < labels.size(); i += step) {
            int x = padding + (int) (chartWidth * i / (prices.size() - 1));
            
            // Draw grid line
            g2.setColor(gridColor);
            g2.drawLine(x, padding, x, height - padding);
            
            // Draw label (only if we have them)
            if (!labels.isEmpty()) {
                g2.setColor(Color.BLACK);
                String label = labels.get(i);
                int labelWidth = g2.getFontMetrics().stringWidth(label);
                g2.drawString(label, x - labelWidth / 2, height - padding + 20);
            }
        }
        
        // Draw price line
        g2.setColor(priceLineColor);
        g2.setStroke(new java.awt.BasicStroke(2f));
        
        for (int i = 0; i < prices.size() - 1; i++) {
            int x1 = padding + (int) (chartWidth * i / (prices.size() - 1));
            int y1 = height - padding - (int) (chartHeight * (prices.get(i) - minPrice) / (maxPrice - minPrice));
            int x2 = padding + (int) (chartWidth * (i + 1) / (prices.size() - 1));
            int y2 = height - padding - (int) (chartHeight * (prices.get(i + 1) - minPrice) / (maxPrice - minPrice));
            
            g2.draw(new Line2D.Double(x1, y1, x2, y2));
        }
        
        // Draw indicator line if available
        if (!indicators.isEmpty()) {
            g2.setColor(indicatorLineColor);
            
            for (int i = 0; i < Math.min(indicators.size() - 1, prices.size() - 1); i++) {
                int x1 = padding + (int) (chartWidth * i / (prices.size() - 1));
                int y1 = height - padding - (int) (chartHeight * (indicators.get(i) - minPrice) / (maxPrice - minPrice));
                int x2 = padding + (int) (chartWidth * (i + 1) / (prices.size() - 1));
                int y2 = height - padding - (int) (chartHeight * (indicators.get(i + 1) - minPrice) / (maxPrice - minPrice));
                
                g2.draw(new Line2D.Double(x1, y1, x2, y2));
            }
        }
        
        // Draw crosshair if mouse is over chart
        if (mouseX >= padding && mouseX <= width - padding) {
            g2.setColor(crosshairColor);
            g2.drawLine(mouseX, padding, mouseX, height - padding);
            
            // Find closest data point and highlight it
            int dataIndex = (int) ((mouseX - padding) * (prices.size() - 1) / chartWidth);
            if (dataIndex >= 0 && dataIndex < prices.size()) {
                int x = padding + (int) (chartWidth * dataIndex / (prices.size() - 1));
                int y = height - padding - (int) (chartHeight * (prices.get(dataIndex) - minPrice) / (maxPrice - minPrice));
                
                // Draw point
                g2.setColor(pointColor);
                g2.fill(new Rectangle2D.Double(x - pointWidth / 2, y - pointWidth / 2, pointWidth, pointWidth));
                
                // Draw tooltip
                String tooltip = String.format("Price: %.2f", prices.get(dataIndex));
                if (!labels.isEmpty() && dataIndex < labels.size()) {
                    tooltip = labels.get(dataIndex) + " - " + tooltip;
                }
                
                int tooltipWidth = g2.getFontMetrics().stringWidth(tooltip) + 10;
                int tooltipHeight = 20;
                int tooltipX = Math.min(x + 10, width - tooltipWidth - 10);
                int tooltipY = Math.max(y - 30, tooltipHeight + 10);
                
                g2.setColor(new Color(255, 255, 225));
                g2.fillRect(tooltipX, tooltipY, tooltipWidth, tooltipHeight);
                g2.setColor(Color.BLACK);
                g2.drawRect(tooltipX, tooltipY, tooltipWidth, tooltipHeight);
                g2.drawString(tooltip, tooltipX + 5, tooltipY + 15);
            }
        }
        
        // Draw legend
        int legendX = width - 150;
        int legendY = 60;
        
        g2.setColor(priceLineColor);
        g2.fillRect(legendX, legendY, 20, 10);
        g2.setColor(Color.BLACK);
        g2.drawString("Price", legendX + 25, legendY + 10);
        
        if (!indicators.isEmpty()) {
            g2.setColor(indicatorLineColor);
            g2.fillRect(legendX, legendY + 20, 20, 10);
            g2.setColor(Color.BLACK);
            g2.drawString("Indicator", legendX + 25, legendY + 30);
        }
    }
}