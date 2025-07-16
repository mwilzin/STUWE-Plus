package de.mwilzinDario.stuwe_advanced;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import javax.smartcardio.CardTerminal;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import de.mwilzinDario.stuwe_advanced.api.MealPlanAPI;
import de.mwilzinDario.stuwe_advanced.auth.StudentManager;
import de.mwilzinDario.stuwe_advanced.models.MealItem;
import de.mwilzinDario.stuwe_advanced.models.SavedStudent;
import de.mwilzinDario.stuwe_advanced.scanner.CommandLineScanner;
import de.mwilzinDario.stuwe_advanced.scanner.NFCScanner;
import de.mwilzinDario.stuwe_advanced.ui.MainView;
import de.mwilzinDario.stuwe_advanced.ui.MenuListView;

/**
 * @author marc-aurelwilzin
 * @author dario
 */
public class Main {
    public static List<MealItem> fetchedMealItems = new ArrayList<>();
    public static SavedStudent[] savedStudents;
    public static SavedStudent currentStudent;
    private static NFCScanner nfcScanner;
    private static CommandLineScanner commandLineScanner;
    public static MainView mainView;

    public static void main(String[] args) {
        // Load students
        loadOrCreateStudents();
        // Fetch meal plan
        fetchMealPlan();
        // Initialize NFC scanner
        initScanner();
        // Create and show GUI
        SwingUtilities.invokeLater(Main::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Stuwe Advanced");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(800, 600));
        frame.setSize(1200, 800);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(Color.WHITE);

        MainView view = new MainView();
        view.setAsMainView();
        frame.add(view, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    public static Image getScaledImagePreserveRatio(Image srcImg, int maxWidth, int maxHeight) {
        int srcWidth = srcImg.getWidth(null);
        int srcHeight = srcImg.getHeight(null);
        double widthRatio = (double) maxWidth / srcWidth;
        double heightRatio = (double) maxHeight / srcHeight;
        double scale = Math.min(widthRatio, heightRatio);
        int newWidth = (int) (srcWidth * scale);
        int newHeight = (int) (srcHeight * scale);
        return srcImg.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
    }

    public static void initScanner() {
        nfcScanner = new NFCScanner();
        List<CardTerminal> terminals = nfcScanner.findDevices();
        if (!terminals.isEmpty()) {
            System.out.println("Found " + terminals.size() + " card terminal(s):");
            for (CardTerminal terminal : terminals) {
                System.out.println("- " + terminal.getName());
            }
            // Start scanning with the first available terminal
            nfcScanner.startScanning();
            System.out.println("Started NFC scanner on: " + terminals.get(0).getName());
        } else {
            System.out.println("No card terminals found - starting command line scanner");
            // Fallback to command line scanner
            commandLineScanner = new CommandLineScanner();
            commandLineScanner.startScanning();
        }
    }

    public static void fetchMealPlan() {
        fetchedMealItems = MealPlanAPI.fetchMealPlan();
    }

    public static void loadOrCreateStudents() {
        StudentManager.loadStudents();
        currentStudent = null;
    }

    public static void saveStudents() {
        StudentManager.saveStudents();
    }

    // For notifying views to update filter state (simple observer pattern)
    private static final List<MenuListView> menuListViews = new ArrayList<>();
    public static void registerMenuListView(MenuListView view) {
        menuListViews.add(view);
    }
    public static void notifyMenuListViewsStudentChanged() {
        for (MenuListView v : menuListViews) {
            v.syncFilterStateWithCurrentStudent();
        }
    }

    public static void setCurrentStudentById(String studentId) {
        SavedStudent student = StudentManager.getStudent(studentId);
        if (student != null) {
            currentStudent = student;
            System.out.println("Current student set to: " + currentStudent.studentID);
            notifyMenuListViewsStudentChanged();
            // Update the view if mainView exists
            if (mainView != null) {
                mainView.updateView();
            }
        } else {
            System.out.println("Student ID not found: " + studentId);
        }
    }
}

