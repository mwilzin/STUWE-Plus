package de.mwilzinDario.stuwe_advanced.auth;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.SwingUtilities;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.mwilzinDario.stuwe_advanced.Main;
import de.mwilzinDario.stuwe_advanced.models.Balances;
import de.mwilzinDario.stuwe_advanced.models.SavedStudent;

public class StudentManager {
    private static final String STUDENTS_FILE = "students.json";
    private static final ObjectMapper mapper = new ObjectMapper();
    private static SavedStudent[] students;

    // Loads students from the JSON file
    public static void loadStudents() {
        File file = new File(STUDENTS_FILE);
        if (!file.exists()) {
            students = new SavedStudent[0];
            return;
        }
        try {
            students = mapper.readValue(file, SavedStudent[].class);
        } catch (IOException e) {
            System.err.println("Error loading students: " + e.getMessage());
            students = new SavedStudent[0];
        }
    }

    // Saves students to the JSON file
    public static void saveStudents() {
        try (FileWriter fw = new FileWriter(STUDENTS_FILE)) {
            mapper.writerWithDefaultPrettyPrinter().writeValue(fw, students);
        } catch (IOException e) {
            System.err.println("Error saving students: " + e.getMessage());
        }
    }

    // Creates a new student with the given ID
    public static SavedStudent createStudent(String studentId) {
        SavedStudent newStudent = new SavedStudent();
        newStudent.studentID = studentId;
        newStudent.isVegan = false;
        newStudent.isVegetarian = false;
        newStudent.balances = new Balances[0];

        // Add to students array
        SavedStudent[] newStudents = new SavedStudent[students.length + 1];
        System.arraycopy(students, 0, newStudents, 0, students.length);
        newStudents[students.length] = newStudent;
        students = newStudents;

        // Save to file
        saveStudents();
        return newStudent;
    }

    // Gets a student by their ID
    public static SavedStudent getStudent(String studentId) {
        for (SavedStudent student : students) {
            if (student.studentID.equals(studentId)) {
                return student;
            }
        }
        return null;
    }

    // Adds a new balance to a student's history
    public static boolean addBalance(String studentId, double amount) {
        SavedStudent student = getStudent(studentId);
        if (student == null) {
            return false;
        }

        Balances newBalance = new Balances();
        newBalance.amount = amount;
        newBalance.date = new java.util.Date();

        Balances[] newBalances = new Balances[student.balances.length + 1];
        System.arraycopy(student.balances, 0, newBalances, 0, student.balances.length);
        newBalances[student.balances.length] = newBalance;
        student.balances = newBalances;

        saveStudents();
        return true;
    }

    // Gets a student's current balance
    public static double getCurrentBalance(String studentId) {
        SavedStudent student = getStudent(studentId);
        if (student == null || student.balances.length == 0) {
            return 0.0;
        }
        return student.balances[student.balances.length - 1].amount;
    }

    // Gets all balances for a student
    public static Balances[] getBalances(String studentId) {
        SavedStudent student = getStudent(studentId);
        return student != null ? student.balances : new Balances[0];
    }

    // Handles the authentication process when a card is detected
    public static void handleCardAuthentication(String cardUid, double cardBalance, Runnable onSuccess) {
        if (cardUid == null || cardBalance < 0) {
            return;
        }
        
        // Get or create student
        SavedStudent student = getStudent(cardUid);
        if (student == null) {
            System.out.println("Creating new student with ID: " + cardUid);
            createStudent(cardUid);
        }
        
        // Only add balance if it's different from the last one
        double lastBalance = getCurrentBalance(cardUid);
        if (Math.abs(lastBalance - cardBalance) > 0.001) { // Use small epsilon for float comparison
            System.out.println("Balance changed from " + lastBalance + " to " + cardBalance + " EUR");
            addBalance(cardUid, cardBalance);
        } else {
            System.out.println("Balance unchanged: " + cardBalance + " EUR");
        }
        
        // Set as current student and trigger login
        Main.setCurrentStudentById(cardUid);
        if (Main.currentStudent != null) {
            // Trigger the callback on the EDT to update the UI
            SwingUtilities.invokeLater(() -> {
                if (onSuccess != null) {
                    onSuccess.run();
                }
                // Update the view and reset the timer
                if (Main.mainView != null) {
                    Main.mainView.updateView();
                    Main.mainView.resetLoggedInTimer();
                }
            });
        }
    }
} 