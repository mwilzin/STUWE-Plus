package de.mwilzinDario.stuwe_advanced.scanner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;

import de.mwilzinDario.stuwe_advanced.auth.StudentManager;

/**
 * !!! The used scanner must be an PC/SC compliant reader and support ISO/IEC 7816-4 commands. !!!
**/

public class NFCScanner {
    private CardTerminal terminal;
    private Card card;
    private CardChannel channel;
    private final ExecutorService executor;
    private final AtomicBoolean isRunning;
    private Runnable onCardDetected;
    
    // DESFire constants
    private static final byte[] APP_ID = new byte[]{(byte)0x5F, (byte)0x84, 0x15};
    private static final byte CLA = (byte) 0x90;
    private static final byte FILE_ID = 1;

    // Constructor for the NFCScanner class
    public NFCScanner() {
        this.executor = Executors.newSingleThreadExecutor();
        // AtomicBoolean is a thread-safe boolean value to avoid race conditions
        this.isRunning = new AtomicBoolean(false);
    }

    // Sets the callback to be executed when a valid card is detected
    public void setOnCardDetected(Runnable callback) {
        this.onCardDetected = callback;
    }

    // Starts the NFC scanner in a background thread
    public void startScanning() {
        if (isRunning.get()) {
            return;
        }

        isRunning.set(true);
        executor.submit(() -> {
            try {
                List<CardTerminal> terminals = findDevices();
                if (terminals.isEmpty()) {
                    System.err.println("No card terminals found");
                    return;
                }

                terminal = terminals.get(0);
                System.out.println("Starting NFC scanner on: " + terminal.getName());

                while (isRunning.get()) {
                    try {
                        // Detect if card is present
                        if (terminal.waitForCardPresent(1000)) {
                            // Get Card UID = StudentID
                            String uid = getCardUID();
                            if (uid != null) {
                                System.out.println("Card detected: " + uid);
                                
                                // Try to read balance
                                double balance = readBalance();
                                if (balance >= 0) {
                                    System.out.println("Balance: " + balance + " EUR");
                                    // Handle authentication with the card data
                                    StudentManager.handleCardAuthentication(uid, balance, onCardDetected);
                                }
                            }
                            
                            // Wait for card to be removed to prevent multiple reads
                            terminal.waitForCardAbsent(1000);
                        }
                    } catch (CardException e) {
                        System.err.println("Error during card operation: " + e.getMessage());
                    }
                }
            } catch (Exception e) {
                System.err.println("Error in NFC scanner thread: " + e.getMessage());
            }
        });
    }

     // Stops the NFC scanner
    public void stopScanning() {
        isRunning.set(false);
        executor.shutdown();
    }

    // Reads the balance from the card
    private double readBalance() {
        try {
            // Connect to the card with any (*) protocol available
            card = terminal.connect("*");
            channel = card.getBasicChannel();
            
            // 1. Select application AID 0x5F8415
            CommandAPDU selectApp = new CommandAPDU(new byte[]{
                CLA, 0x5A, 0x00, 0x00, 0x03,  // SELECT APPLICATION
                APP_ID[0], APP_ID[1], APP_ID[2], 0x00
            });
            ResponseAPDU response = channel.transmit(selectApp);
            if (!checkResponse(response, "Select App")) {
                // Failed to select Application
                return -1;
            }

            // 2. Read Value from File ID 1
            CommandAPDU readValue = new CommandAPDU(new byte[]{
                CLA, (byte)0x6C,  // READ VALUE
                0x00, 0x00,       // P1, P2
                0x01,             // Lc: Length of data
                FILE_ID,          // File ID
                0x00              // Le: Get maximum response length
            });
            ResponseAPDU valueResp = channel.transmit(readValue);
            if (!checkResponse(valueResp, "Read Value")) {
                // Failed to read File
                return -1;
            }

            byte[] valueData = valueResp.getData();
            if (valueData.length >= 4) {
                // Remove last two bytes and reverse the remaining bytes
                byte[] balanceBytes = new byte[valueData.length - 2];
                for (int i = 0; i < balanceBytes.length; i++) {
                    balanceBytes[i] = valueData[balanceBytes.length - 1 - i];
                }
                int value = bytesToInt(balanceBytes);
                // Get decimal balance
                return (value / 1000.0);
            }
        } catch (CardException e) {
            System.err.println("Error reading balance: " + e.getMessage());
        } finally {
            disconnect();
        }
        return -1;
    }

    // Gets the card's UID using CCID commands
    private String getCardUID() {
        try {
            // Connect to the card with any (*) protocol available
            card = terminal.connect("*");
            channel = card.getBasicChannel();
            
            // Get and print the ATR (Inital Message which contains config data)
            byte[] atr = card.getATR().getBytes();
            System.out.println("Card ATR: " + bytesToHex(atr));
            
            // Send the Get UID command
            CommandAPDU command = new CommandAPDU(new byte[]{
                (byte) 0xFF,  // Class: Proprietary
                (byte) 0xCA,  // Instruction: Get UID
                0x00,         // P1
                0x00,         // P2
                0x00          // Le: Get maximum response length
            });
            ResponseAPDU response = channel.transmit(command);
            // Check if the response was successful
            if (checkResponse(response, "Get UID")) {
                return bytesToHex(response.getData());
            }
        } catch (CardException e) {
            System.err.println("Error getting card UID: " + e.getMessage());
        } finally {
            disconnect();
        }
        return null;
    }

    // Checks the response status and prints the result
    private boolean checkResponse(ResponseAPDU response, String label) {
        int sw = response.getSW();
        if (sw == 0x9100 || sw == 0x9000) {
            System.out.println(label + ": OK");
            return true;
        } else {
            System.err.println(label + " failed: SW=" + Integer.toHexString(sw));
            return false;
        }
    }

    // Disconnects from the current card
    private void disconnect() {
        try {
            if (card != null) {
                card.disconnect(false);
                card = null;
                channel = null;
            }
        } catch (CardException e) {
            System.err.println("Error disconnecting from card: " + e.getMessage());
        }
    }

    // Converts a byte array to an integer
    private int bytesToInt(byte[] bytes) {
        int val = 0;
        for (byte b : bytes) {
            val = (val << 8) | (b & 0xFF);
        }
        return val;
    }

    // Converts a byte array to a hex string
    private String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            hex.append(String.format("%02X", b));
        }
        return hex.toString();
    }

    // Searches for available card terminals
    public List<CardTerminal> findDevices() {
        List<CardTerminal> terminals = new ArrayList<>();
        try {
            TerminalFactory factory = TerminalFactory.getDefault();
            CardTerminals cardTerminals = factory.terminals();
            terminals.addAll(cardTerminals.list());
        } catch (CardException e) {
            System.err.println("Error finding card terminals: " + e.getMessage());
        }
        return terminals;
    }
} 