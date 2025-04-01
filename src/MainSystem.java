import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class MainSystem {
    
    private Scanner scanner;
    
    public MainSystem() {
        scanner = new Scanner(System.in);
    }
    
    // Method to get and normalize a file name
    public String askName() {
        return scanner.nextLine();
    }
    
    // Method to fix file name (ensure it has .csv extension)
    public String fixName(String name) {
        if (!name.endsWith(".csv")) {
            name += ".csv";
        }
        return name;
    }
    
    // Method to read from a file
    public BufferedReader readFile(String fileName) {
        try {
            return new BufferedReader(new FileReader(fileName));
        } catch (IOException e) {
            System.out.println("Error opening file: " + fileName);
            e.printStackTrace();
            return null;
        }
    }
    
    // Method to write to a file
    public BufferedWriter writeFile(String fileName) {
        try {
            return new BufferedWriter(new FileWriter(fileName));
        } catch (IOException e) {
            System.out.println("Error creating file: " + fileName);
            e.printStackTrace();
            return null;
        }
    }
    
    // Method to calculate simple moving average
    public ArrayList<Double> smoothy(ArrayList<Double> data) {
        ArrayList<Double> result = new ArrayList<>();
        int period = 14; // Moving Average period
        
        // First n-1 elements don't have a full window, so we'll use whatever data we have
        for (int i = 0; i < data.size(); i++) {
            double sum = 0;
            int count = 0;
            
            // Sum the values in the window
            for (int j = Math.max(0, i - period + 1); j <= i; j++) {
                sum += data.get(j);
                count++;
            }
            
            // Add the average to the result
            result.add(sum / count);
        }
        
        return result;
    }
    
    // Method to format double values to 2 decimal places
    public double format(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}