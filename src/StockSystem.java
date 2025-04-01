import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

public class StockSystem {

    // Date formats
    private String format1 = "M/dd/yyyy", format2 = "MM/dd/yyyy", format3 = "M/d/yyyy", 
                   format4 = "M/dd/yyyy", format5 = "yyyy-MM-dd";
    private DateTimeFormatter formDate = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    
    private double balance;
    private int stock;
    private static final int N = 14;

    private StringBuilder str = new StringBuilder();
    private BufferedWriter log;

    private ArrayList<Double> rsiValues = new ArrayList<>();
    private ArrayList<Double> open = new ArrayList<>();
    private ArrayList<Double> high = new ArrayList<>();
    private ArrayList<Double> low = new ArrayList<>();
    private ArrayList<Double> close = new ArrayList<>();
    private ArrayList<Double> adj_close = new ArrayList<>();
    private ArrayList<Double> vol = new ArrayList<>();
    private ArrayList<LocalDate> date = new ArrayList<>();
    private ArrayList<Double> ma = new ArrayList<>();
    
    // Performance tracking for the chart
    private ArrayList<Double> performanceData = new ArrayList<>();

    private MainSystem mainSystem;
    
    public StockSystem() {
        this.mainSystem = new MainSystem();
    }
    
    public void setMainSystem(MainSystem mainSystem) {
        this.mainSystem = mainSystem;
    }
    
    public void setBalance(double balance) {
        this.balance = balance;
    }
    
    public void loadData(String inputFile, String outputFile) throws IOException, ParseException {
        // Clear previous data
        clearData();
        
        // Read the CSV file
        BufferedReader br = mainSystem.readFile(inputFile);
        BufferedWriter write = mainSystem.writeFile(outputFile);
        boolean header = false;

        String line;
        // Store respective values
        while ((line = br.readLine()) != null) {
            String[] list = line.split(",");
            if (!header) {
                header = true;
                continue;
            }

            date.add(add_Date(list[0]));
            open.add(Double.parseDouble(list[1].trim()));
            high.add(Double.parseDouble(list[2].trim()));
            low.add(Double.parseDouble(list[3].trim()));
            close.add(Double.parseDouble(list[4].trim()));
            adj_close.add(Double.parseDouble(list[5].trim()));
            vol.add(Double.parseDouble(list[6].trim()));
        }

        // Generate MA and RSI values
        ma = mainSystem.smoothy(open);
        rsiValues = getRSI(open, N);

        // Write results to output file
        StringBuilder head_line = new StringBuilder();
        head_line.append("Date").append(",").append("RSI values").append(",").append("Open price").append(",")
                .append("Moving Average");

        str.append(head_line).append("\n");
        for (int i = 0; i < rsiValues.size(); i++) {
            Double rsi_val = mainSystem.format(rsiValues.get(i));
            Double ma_val = mainSystem.format(ma.get(i));
            str.append(date.get(i)).append(",").append(rsi_val).append(",").append(open.get(i)).append(",")
                    .append(ma_val).append("\n");
        }

        write.write(str.toString());
        write.close();
        br.close();
    }
    
    private void clearData() {
        rsiValues.clear();
        open.clear();
        high.clear();
        low.clear();
        close.clear();
        adj_close.clear();
        vol.clear();
        date.clear();
        ma.clear();
        performanceData.clear();
        str = new StringBuilder();
    }
    
    // Algorithm 1: Long-term holding strategy
    public double algo1() {
        log = mainSystem.writeFile("Activity_log_Algorithm_1_TheLongGame.csv");
        str = new StringBuilder();
        double bal = balance, worth;
        LocalDate buy_date = date.get(0);
        LocalDate[] sell_date = new LocalDate[4];
        double[] price = new double[4];
        price[0] = open.get(0);

        // Clear and initialize performance data
        performanceData.clear();

        // Header
        str.append("Date").append(",").append("Networth").append("\n");

        // To test for 1-4 years of patient
        for (int i = 0; i < 4; i++) {
            sell_date[i] = buy_date.plusYears(i + 1);
        }

        int buyShare = (int) (bal / price[0]);
        bal -= buyShare * open.get(0);

        for (int i = 0; i < date.size(); i++) {
            if (isWithinAWeek(sell_date[0], date.get(i))) {
                price[0] = open.get(i);
            }
            if (isWithinAWeek(sell_date[1], date.get(i))) {
                price[1] = open.get(i);
            }
            if (isWithinAWeek(sell_date[2], date.get(i))) {
                price[2] = open.get(i);
            }
            if (isWithinAWeek(sell_date[3], date.get(i))) {
                price[3] = open.get(i);
            }

            worth = bal + (buyShare * close.get(i));
            performanceData.add(worth);
            str.append(date.get(i).format(formDate)).append(",").append(mainSystem.format(worth)).append("\n");
        }

        try {
            log.write(str.toString());
            log.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // Return final worth
        return bal + (buyShare * close.get(close.size() - 1));
    }
    
    // Algorithm 2: RSI and MA based trading
    public double algo2() {
        log = mainSystem.writeFile("Activity_log_Algorithm_2_RSI_MA_Method.csv");
        str = new StringBuilder();
        double bal = balance, worth = 0;
        int buyShare = 0;
        int val;
        boolean stop = false;

        // Clear and initialize performance data
        performanceData.clear();

        // Header
        str.append("Date").append(",").append("Networth").append("\n");

        for (int i = 0; i < date.size(); i++) {
            // Buy if the rsi value is going above 30 and the MA is lower than the current price
            if (!stop) {
                if (rsiValues.get(i) > 30 && ma.get(i) < open.get(i) && rsiValues.get(i) < 70) {
                    if (bal > open.get(i)) {
                        val = tradeEvaluator(1, i, bal);
                        if (val > 0) {
                            bal -= val * open.get(i);
                            buyShare += val;
                        }
                    }
                }
                // Sell if RSI is < 70 and MA > current price
                else if (rsiValues.get(i) < 70 && (i > 0 && rsiValues.get(i - 1) > 70) && ma.get(i) > open.get(i)) {
                    if (buyShare > 0) {
                        val = tradeEvaluator(2, i, (double) buyShare);
                        if (val > 0) {
                            buyShare -= val;
                            bal += val * open.get(i);
                        }
                    }
                }
            }

            // Calculate current portfolio worth
            worth = bal + (buyShare * close.get(i));
            performanceData.add(worth);
            
            // Check if we should stop (doubled money and RSI trend is changing)
            if (balance * 2 <= worth) {
                if (i > 1 && !(rsiValues.get(i) > rsiValues.get(i - 1) && (rsiValues.get(i - 1) > rsiValues.get(i - 2)))) {
                    stop = true;
                }
            }

            str.append(date.get(i).format(formDate)).append(",").append(mainSystem.format(worth)).append("\n");
        }

        try {
            log.write(str.toString());
            log.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Calculate final worth
        worth = bal;
        if (buyShare > 0) {
            worth += buyShare * close.get(close.size() - 1);
        }
        
        return worth;
    }
    
    // Algorithm 3: Aggressive buy/sell based on price and RSI
    public double algo3() {
        log = mainSystem.writeFile("Activity_log_Algorithm_3_UsingModule.csv");
        str = new StringBuilder();
        double bal = balance, worth = 0;
        double buyPrice = open.get(0);
        int bought, buyShare = 0;

        // Clear and initialize performance data
        performanceData.clear();

        // Initial purchase
        bought = (int) (bal / buyPrice);
        buyShare += bought;
        bal -= (bought * buyPrice);

        boolean stop = false;
        
        // Header
        str.append("Date").append(",").append("Networth").append("\n");

        for (int i = 1; i < date.size(); i++) {
            // Buy strategy
            if (!stop) {
                if (open.get(i) < buyPrice || (rsiValues.get(i) > 30 && rsiValues.get(i) < 70)) {
                    bought = (int) (bal / open.get(i));
                    buyShare += bought;
                    buyPrice = open.get(i);
                    bal -= bought * buyPrice;
                }
                // Sell strategy
                else if (open.get(i) > buyPrice || rsiValues.get(i) > 70) {
                    bal += buyShare * open.get(i);
                    buyShare = 0;
                }
            }
            
            // Calculate current portfolio worth
            worth = bal + (buyShare * close.get(i));
            performanceData.add(worth);
            
            // Check if we should stop (doubled money and RSI trend is changing)
            if (balance * 2 <= worth) {
                if (i > 1 && !(rsiValues.get(i) > rsiValues.get(i - 1) && (rsiValues.get(i - 1) > rsiValues.get(i - 2)))) {
                    stop = true;
                }
            }

            str.append(date.get(i).format(formDate)).append(",").append(mainSystem.format(worth)).append("\n");
        }

        try {
            log.write(str.toString());
            log.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // Calculate final worth
        worth = bal;
        if (buyShare > 0) {
            worth += buyShare * close.get(close.size() - 1);
        }
        
        return worth;
    }
    
    // Helper methods from your original code
    private int tradeEvaluator(int input, int i, double bal) {
        Double current_price = mainSystem.format(open.get(i));
        int shares = 0;

        // buy and sell 30% at a time
        switch (input) {
            case 1: // Buy stock
                shares = (int) Math.min((bal * 0.3) / current_price, bal / current_price);
                return shares;
            case 2: // Sell stock
                return (int) (bal * 0.5);
            default: // No change
                return 0;
        }
    }
    
    private LocalDate add_Date(String listItem) throws ParseException {
        LocalDate current_date = parseDateFlexible(listItem, format1, format2, format3, format4, format5);
        return current_date;
    }
    
    private static LocalDate parseDateFlexible(String dateString, String... formats) {
        for (String format : formats) {
            try {
                return LocalDate.parse(dateString, DateTimeFormatter.ofPattern(format));
            } catch (DateTimeParseException e) {
                // Try the next format
            }
        }
        throw new IllegalArgumentException("Could not parse date: " + dateString);
    }
    
    private static boolean isWithinAWeek(LocalDate date1, LocalDate date2) {
        // Calculate the difference in days between the dates
        long daysBetween = 10;
        if (date1.getYear() == date2.getYear()) {
            if (date1.getMonth() == date2.getMonth()) {
                daysBetween = Math.abs(date1.until(date2).getDays());
            }
        }

        // Check if the absolute difference is less than or equal to 3 days
        return daysBetween <= 3;
    }
    
    // RSI calculation methods
    private ArrayList<Double> getRSI(ArrayList<Double> price, int n) {
        ArrayList<Double> U = new ArrayList<>();
        ArrayList<Double> D = new ArrayList<>();
        ArrayList<Double> rsiVal = new ArrayList<>();
        ArrayList<Double> rs = new ArrayList<>();

        double change;

        // for the first value
        // Because my code need it for formatting
        // I either do this or remove some early days
        U.add(0.0);
        D.add(0.0);
        U.add(0.0);
        D.add(0.0);

        for (int i = 1; i < price.size(); i++) {
            change = price.get(i) - price.get(i - 1);
            if (change >= 0) {
                U.add(change);
                D.add(0.0);
            } else {
                U.add(0.0);
                D.add(Math.abs(change));
            }
        }
        rs = getRS(U, D, n);

        for (double ele : rs) {
            double rsi = 0;
            if (ele == 0) {
                rsiVal.add(100.0);
            } else {
                rsi = (100 - (100 / (1 + ele)));
                rsiVal.add(rsi);
            }
        }
        return rsiVal;
    }

    // Get the rs values
    private ArrayList<Double> getRS(ArrayList<Double> up, ArrayList<Double> down, int n) {
        ArrayList<Double> rs = new ArrayList<>();
        for (int i = 1; i < up.size(); i++) {
            double avgU = 0, avgD = 0;
            for (int num = i; num > Math.max(0, i - n); num--) {
                avgU += up.get(num);
                avgD += down.get(num);
            }
            if (avgD == 0) {
                rs.add(0.0);
            } else
                rs.add(avgU / avgD);
        }
        return rs;
    }
    
    // Getter methods for GUI components to access the data
    public int getDataSize() {
        return date.size();
    }
    
    public LocalDate getFirstDate() {
        return date.isEmpty() ? null : date.get(0);
    }
    
    public LocalDate getLastDate() {
        return date.isEmpty() ? null : date.get(date.size() - 1);
    }
    
    public ArrayList<Double> getOpenPrices() {
        return open;
    }
    
    public ArrayList<Double> getMAValues() {
        return ma;
    }
    
    public ArrayList<Double> getRSIValues() {
        return rsiValues;
    }
    
    public ArrayList<Double> getPerformanceData() {
        return performanceData;
    }