package de.mwilzinDario.stuwe_advanced.scanner;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import de.mwilzinDario.stuwe_advanced.auth.StudentManager;

/**
 * Command-line fallback scanner for when no NFC hardware is available
 * Prompts user to manually enter student ID and balance
 */
public class CommandLineScanner {
    private final ExecutorService executor;
    private final AtomicBoolean isRunning;
    private final Scanner scanner;
    private Runnable onCardDetected;
    
    public CommandLineScanner() {
        this.executor = Executors.newSingleThreadExecutor();
        this.isRunning = new AtomicBoolean(false);
        this.scanner = new Scanner(System.in);
    }
    
    public void setOnCardDetected(Runnable callback) {
        this.onCardDetected = callback;
    }
    
    public void startScanning() {
        if (isRunning.get()) {
            return;
        }
        
        isRunning.set(true);
        System.out.println("=== Command Line Scanner Started ===");
        System.out.println();
        
        executor.submit(() -> {
            while (isRunning.get()) {
                performManualScan();
            }
        });
    }
    
    private void performManualScan() {
        try {
            
            // Prompt for Student ID
            System.out.print("Enter Student ID: ");
            String studentId = scanner.nextLine().trim();
            
            if (studentId.isEmpty()) {
                System.out.println("Student ID cannot be empty. Scan cancelled.");
                return;
            }
            
            // Prompt for Balance
            System.out.print("Enter current balance (EUR): ");
            String balanceInput = scanner.nextLine().trim();
            
            double balance;
            try {
                balance = Double.parseDouble(balanceInput);
                if (balance < 0) {
                    System.out.println("Balance cannot be negative. Scan cancelled.");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid balance format. Please enter a valid number. Scan cancelled.");
                return;
            }
            
            // Process the scan
            System.out.println("Processing scan for Student ID: " + studentId + ", Balance: " + balance + " EUR");
            StudentManager.handleCardAuthentication(studentId, balance, onCardDetected);
            
            System.out.println("Scan completed successfully!\n");
            
        } catch (Exception e) {
            System.err.println("Error during manual scan: " + e.getMessage());
        }
    }
    
    public boolean isRunning() {
        return isRunning.get();
    }
} 