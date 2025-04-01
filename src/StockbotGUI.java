import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class StockBotGUI extends JFrame {
    
    private StockSystem stockSystem;
    private MainSystem mainSystem;
    private JComboBox<String> algorithmSelector;
    private JTextField balanceField;
    private JTextField stockSearchField;
    private JTextArea resultArea;
    private JPanel chartPanel;
    private File selectedFile;
    private JTextField exportFileField;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    
    public StockBotGUI() {
        // Initialize components
        stockSystem = new StockSystem();
        mainSystem = new MainSystem();
        
        // Setup the frame
        setTitle("StockBot - Stock Analysis System");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Create main panel with border layout
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Create top panel for controls
        JPanel controlPanel = createControlPanel();
        mainPanel.add(controlPanel, BorderLayout.NORTH);
        
        // Create center panel for chart and results
        JSplitPane centerPanel = createCenterPanel();
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        // Add the main panel to the frame
        add(mainPanel);
    }
    
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Controls"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // File selection row
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("CSV File:"), gbc);
        
        JTextField filePathField = new JTextField(20);
        filePathField.setEditable(false);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(filePathField, gbc);
        
        JButton browseButton = new JButton("Browse");
        browseButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                selectedFile = fileChooser.getSelectedFile();
                filePathField.setText(selectedFile.getAbsolutePath());
            }
        });
        gbc.gridx = 2;
        gbc.weightx = 0.0;
        panel.add(browseButton, gbc);
        
        // Export file row
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Export File:"), gbc);
        
        exportFileField = new JTextField(20);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(exportFileField, gbc);
        
        JButton exportBrowseButton = new JButton("Browse");
        exportBrowseButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
            fileChooser.setDialogTitle("Specify Export File Name");
            int result = fileChooser.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                String path = file.getAbsolutePath();
                if (!path.toLowerCase().endsWith(".csv")) {
                    path += ".csv";
                }
                exportFileField.setText(path);
            }
        });
        gbc.gridx = 2;
        gbc.weightx = 0.0;
        panel.add(exportBrowseButton, gbc);
        
        // Balance row
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Initial Balance ($):"), gbc);
        
        balanceField = new JTextField("10000", 10);
        gbc.gridx = 1;
        panel.add(balanceField, gbc);
        
        // Algorithm selection row
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("Algorithm:"), gbc);
        
        algorithmSelector = new JComboBox<>(new String[]{
            "Algorithm 1: Long Game",
            "Algorithm 2: RSI & MA Method",
            "Algorithm 3: Using Module"
        });
        gbc.gridx = 1;
        panel.add(algorithmSelector, gbc);
        
        // Stock search row
        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(new JLabel("Stock Search:"), gbc);
        
        stockSearchField = new JTextField(10);
        gbc.gridx = 1;
        panel.add(stockSearchField, gbc);
        
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchStock());
        gbc.gridx = 2;
        panel.add(searchButton, gbc);
        
        // Action buttons row
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton loadButton = new JButton("Load Data");
        loadButton.addActionListener(e -> loadData());
        buttonPanel.add(loadButton);
        
        JButton analyzeButton = new JButton("Analyze");
        analyzeButton.addActionListener(e -> runAnalysis());
        buttonPanel.add(analyzeButton);
        
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 3;
        panel.add(buttonPanel, gbc);
        
        return panel;
    }
    
    private JSplitPane createCenterPanel() {
        // Create chart panel
        chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBorder(BorderFactory.createTitledBorder("Stock Chart"));
        chartPanel.add(new JLabel("No data loaded", JLabel.CENTER), BorderLayout.CENTER);
        
        // Create results panel
        JPanel resultsPanel = new JPanel(new BorderLayout());
        resultsPanel.setBorder(BorderFactory.createTitledBorder("Analysis Results"));
        
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);
        resultsPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Create split pane with chart on top and results on bottom
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, chartPanel, resultsPanel);
        splitPane.setResizeWeight(0.7); // Give more space to the chart
        
        return splitPane;
    }
    
    private void loadData() {
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(this, "Please select a CSV file first.",
                    "No File Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (exportFileField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please specify an export file name.",
                    "No Export File", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            stockSystem = new StockSystem();
            stockSystem.setMainSystem(mainSystem);
            
            // Load the data
            stockSystem.loadData(selectedFile.getAbsolutePath(), exportFileField.getText());
            
            // Update the chart
            updateChart();
            
            resultArea.setText("Data loaded successfully.\n");
            resultArea.append("Total records: " + stockSystem.getDataSize() + "\n");
            resultArea.append("Date range: " + 
                    stockSystem.getFirstDate().format(dateFormatter) + " to " + 
                    stockSystem.getLastDate().format(dateFormatter) + "\n");
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage(),
                    "Load Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void updateChart() {
        // Create dataset
        XYSeriesCollection dataset = new XYSeriesCollection();
        
        // Add price series
        XYSeries priceSeries = new XYSeries("Price");
        ArrayList<Double> openPrices = stockSystem.getOpenPrices();
        for (int i = 0; i < openPrices.size(); i++) {
            priceSeries.add(i, openPrices.get(i));
        }
        dataset.addSeries(priceSeries);
        
        // Add MA series
        XYSeries maSeries = new XYSeries("Moving Average");
        ArrayList<Double> maValues = stockSystem.getMAValues();
        for (int i = 0; i < maValues.size(); i++) {
            maSeries.add(i, maValues.get(i));
        }
        dataset.addSeries(maSeries);
        
        // Add RSI series on secondary axis if needed
        XYSeries rsiSeries = new XYSeries("RSI");
        ArrayList<Double> rsiValues = stockSystem.getRSIValues();
        for (int i = 0; i < rsiValues.size(); i++) {
            rsiSeries.add(i, rsiValues.get(i));
        }
        
        // Create chart
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Stock Price and Indicators",
                "Time",
                "Price",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
        
        // Customize the chart
        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, Color.BLUE);
        renderer.setSeriesPaint(1, Color.RED);
        plot.setRenderer(renderer);
        
        // Update the chart panel
        chartPanel.removeAll();
        ChartPanel panel = new ChartPanel(chart);
        panel.setPreferredSize(new Dimension(600, 400));
        panel.setMouseWheelEnabled(true);
        chartPanel.add(panel, BorderLayout.CENTER);
        chartPanel.revalidate();
        chartPanel.repaint();
    }
    
    private void runAnalysis() {
        if (stockSystem == null || stockSystem.getDataSize() == 0) {
            JOptionPane.showMessageDialog(this, "Please load data first.",
                    "No Data", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            double balance = Double.parseDouble(balanceField.getText());
            stockSystem.setBalance(balance);
            
            int selectedAlgo = algorithmSelector.getSelectedIndex();
            
            resultArea.setText("");
            resultArea.append("Running analysis with initial balance: $" + balance + "\n\n");
            
            double result = 0;
            switch (selectedAlgo) {
                case 0:
                    result = stockSystem.algo1();
                    break;
                case 1:
                    result = stockSystem.algo2();
                    break;
                case 2:
                    result = stockSystem.algo3();
                    break;
            }
            
            resultArea.append("Analysis complete.\n");
            resultArea.append("Final balance: $" + String.format("%.2f", result) + "\n");
            resultArea.append("Profit/Loss: $" + String.format("%.2f", result - balance) + 
                    " (" + String.format("%.2f", ((result - balance) / balance) * 100) + "%)\n");
            
            // Update the chart with performance data
            updatePerformanceChart(selectedAlgo);
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number for the balance.",
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error running analysis: " + e.getMessage(),
                    "Analysis Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void updatePerformanceChart(int algorithmIndex) {
        // Create dataset
        XYSeriesCollection dataset = new XYSeriesCollection();
        
        // Add performance series based on algorithm
        XYSeries performanceSeries = new XYSeries("Portfolio Value");
        ArrayList<Double> performanceData = stockSystem.getPerformanceData();
        for (int i = 0; i < performanceData.size(); i++) {
            performanceSeries.add(i, performanceData.get(i));
        }
        dataset.addSeries(performanceSeries);
        
        // Add initial balance reference line
        XYSeries initialBalanceSeries = new XYSeries("Initial Balance");
        double initialBalance = Double.parseDouble(balanceField.getText());
        for (int i = 0; i < performanceData.size(); i++) {
            initialBalanceSeries.add(i, initialBalance);
        }
        dataset.addSeries(initialBalanceSeries);
        
        // Create chart
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Algorithm " + (algorithmIndex + 1) + " Performance",
                "Trading Days",
                "Portfolio Value ($)",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
        
        // Customize the chart
        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, Color.GREEN);
        renderer.setSeriesPaint(1, Color.RED);
        renderer.setSeriesShapesVisible(1, false); // No shapes for reference line
        plot.setRenderer(renderer);
        
        // Update the chart panel
        chartPanel.removeAll();
        ChartPanel panel = new ChartPanel(chart);
        panel.setPreferredSize(new Dimension(600, 400));
        panel.setMouseWheelEnabled(true);
        chartPanel.add(panel, BorderLayout.CENTER);
        chartPanel.revalidate();
        chartPanel.repaint();
    }
    
    private void searchStock() {
        String symbol = stockSearchField.getText().trim().toUpperCase();
        if (symbol.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a stock symbol.",
                    "Empty Input", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Create a stock search service
        StockSearchService searchService = new StockSearchService();
        
        // In a real app, this would search an API
        if (searchService.symbolExists(symbol)) {
            String companyName = searchService.getCompanyName(symbol);
            resultArea.setText("Found stock: " + symbol + " - " + companyName + "\n\n");
            
            // Show options to download historical data
            resultArea.append("You can download historical data for " + symbol + " by:\n");
            resultArea.append("1. Selecting a date range\n");
            resultArea.append("2. Clicking 'Download Data'\n\n");
            resultArea.append("Note: This feature would connect to a financial API in a full implementation.\n");
            
            // Add download buttons
            JPanel downloadPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            downloadPanel.add(new JLabel("Start Date:"));
            JTextField startDateField = new JTextField(10);
            startDateField.setText("2020-01-01");
            downloadPanel.add(startDateField);
            
            downloadPanel.add(new JLabel("End Date:"));
            JTextField endDateField = new JTextField(10);
            endDateField.setText("2023-01-01");
            downloadPanel.add(endDateField);
            
            JButton downloadButton = new JButton("Download Data");
            downloadButton.addActionListener(e -> {
                JOptionPane.showMessageDialog(this, 
                        "In a full implementation, this would download historical data for " + symbol + "\n" +
                        "from " + startDateField.getText() + " to " + endDateField.getText(),
                        "Download Feature", JOptionPane.INFORMATION_MESSAGE);
            });
            downloadPanel.add(downloadButton);
            
            // Add the download panel to the bottom of the results panel
            JPanel resultsPanel = (JPanel) resultArea.getParent().getParent();
            resultsPanel.add(downloadPanel, BorderLayout.SOUTH);
            resultsPanel.revalidate();
            resultsPanel.repaint();
            
        } else {
            resultArea.setText("Stock symbol not found: " + symbol + "\n\n");
            resultArea.append("Please try another symbol or check your spelling.\n");
            resultArea.append("Common symbols include: AAPL (Apple), MSFT (Microsoft), GOOGL (Google), AMZN (Amazon).\n");
        }
    }
    
    // Additional functionality - shows a candlestick chart
    private void showCandlestickChart() {
        if (stockSystem == null || stockSystem.getDataSize() == 0) {
            JOptionPane.showMessageDialog(this, "Please load data first.",
                    "No Data", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Create a new frame for the candlestick chart
        JFrame candlestickFrame = new JFrame("Candlestick Chart");
        candlestickFrame.setSize(800, 600);
        candlestickFrame.setLocationRelativeTo(this);
        
        // In a full implementation, this would create a candlestick chart
        // using the high, low, open, and close data
        JPanel placeholderPanel = new JPanel(new BorderLayout());
        placeholderPanel.add(new JLabel("Candlestick chart would be displayed here in a full implementation", JLabel.CENTER));
        
        candlestickFrame.add(placeholderPanel);
        candlestickFrame.setVisible(true);
    }
    
    // For adding a menu bar with additional options
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // File menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem openItem = new JMenuItem("Open CSV...");
        openItem.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                selectedFile = fileChooser.getSelectedFile();
                // Update UI to show selected file
            }
        });
        fileMenu.add(openItem);
        
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);
        
        // Chart menu
        JMenu chartMenu = new JMenu("Charts");
        JMenuItem lineChartItem = new JMenuItem("Line Chart");
        lineChartItem.addActionListener(e -> updateChart());
        chartMenu.add(lineChartItem);
        
        JMenuItem candlestickItem = new JMenuItem("Candlestick Chart");
        candlestickItem.addActionListener(e -> showCandlestickChart());
        chartMenu.add(candlestickItem);
        
        // Help menu
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                    "StockBot - Stock Analysis System\n" +
                    "Version 1.0\n\n" +
                    "A tool for analyzing stock data and testing trading strategies.",
                    "About StockBot",
                    JOptionPane.INFORMATION_MESSAGE);
        });
        helpMenu.add(aboutItem);
        
        menuBar.add(fileMenu);
        menuBar.add(chartMenu);
        menuBar.add(helpMenu);
        
        return menuBar;
    }
    
    public static void main(String[] args) {
        try {
            // Set system look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Launch the application
        SwingUtilities.invokeLater(() -> {
            StockBotGUI gui = new StockBotGUI();
            gui.setJMenuBar(gui.createMenuBar());
            gui.setVisible(true);
        });
    }
}