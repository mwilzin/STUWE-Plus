package de.mwilzinDario.stuwe_advanced.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

import de.mwilzinDario.stuwe_advanced.Main;
import de.mwilzinDario.stuwe_advanced.models.MealItem;

public class MenuListView extends JPanel {
    private boolean veganFilterActive = false;
    private boolean vegetarischFilterActive = false;
    private final JPanel menuPanel;
    private JToggleButton veganBtn;
    private JToggleButton vegetarischBtn;

    public MenuListView() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.WHITE);
        // Filter and date row
        JPanel filterDatePanel = new JPanel();
        filterDatePanel.setLayout(new BoxLayout(filterDatePanel, BoxLayout.X_AXIS));
        filterDatePanel.setOpaque(false);
        filterDatePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        JPanel datePanel = new JPanel();
        datePanel.setOpaque(false);
        datePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        // Date
        LocalDate now = LocalDate.now();
        String weekday = now.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.GERMAN);
        String dateLabelStr = String.format("%d. %s", now.getDayOfMonth(), now.getMonth().getDisplayName(TextStyle.FULL, Locale.GERMAN));
        JLabel weekdayLabel = new JLabel(weekday);
        weekdayLabel.setFont(new Font("Arial", Font.BOLD, 22));
        JLabel smallDateLabel = new JLabel(" " + dateLabelStr);
        smallDateLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        datePanel.add(weekdayLabel);
        datePanel.add(smallDateLabel);
        filterDatePanel.add(datePanel);
        filterDatePanel.add(Box.createHorizontalGlue());

        JLabel filterLabel = new JLabel("Filter:");
        filterLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        filterDatePanel.add(filterLabel);
        filterDatePanel.add(Box.createRigidArea(new Dimension(10, 0)));
        // Vegan filter
        veganBtn = new JToggleButton("Vegan");
        veganBtn.setFocusPainted(false);
        veganBtn.setBackground(new Color(220, 220, 220));
        veganBtn.setForeground(Color.BLACK);
        veganBtn.setBorder(new LineBorder(new Color(200, 200, 200), 2, true));
        veganBtn.setContentAreaFilled(true);
        veganBtn.setOpaque(true);
        veganBtn.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
        filterDatePanel.add(veganBtn);
        filterDatePanel.add(Box.createRigidArea(new Dimension(10, 0)));
        // Vegetarisch filter
        vegetarischBtn = new JToggleButton("Vegetarisch");
        vegetarischBtn.setFocusPainted(false);
        vegetarischBtn.setBackground(new Color(220, 220, 220));
        vegetarischBtn.setForeground(Color.BLACK);
        vegetarischBtn.setBorder(new LineBorder(new Color(200, 200, 200), 2, true));
        vegetarischBtn.setContentAreaFilled(true);
        vegetarischBtn.setOpaque(true);
        vegetarischBtn.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
        filterDatePanel.add(vegetarischBtn);
        filterDatePanel.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 30));
        // Menu list
        add(filterDatePanel);
        menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setOpaque(false);
        menuPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        add(menuPanel);
        updateMenuList();
        // Filter state sync
        // If a user is logged in, set filter states from their profile, else disable buttons
        if (Main.currentStudent != null) {
            veganFilterActive = Main.currentStudent.isVegan;
            vegetarischFilterActive = Main.currentStudent.isVegetarian;
            veganBtn.setSelected(veganFilterActive);
            vegetarischBtn.setSelected(vegetarischFilterActive);
            veganBtn.setBackground(veganFilterActive ? new Color(91, 180, 56) : new Color(220, 220, 220));
            veganBtn.setForeground(veganFilterActive ? Color.WHITE : Color.BLACK);
            vegetarischBtn.setBackground(vegetarischFilterActive ? new Color(91, 180, 56) : new Color(220, 220, 220));
            vegetarischBtn.setForeground(vegetarischFilterActive ? Color.WHITE : Color.BLACK);
            veganBtn.setEnabled(true);
            vegetarischBtn.setEnabled(true);
        } else {
            veganBtn.setSelected(false);
            vegetarischBtn.setSelected(false);
            veganBtn.setBackground(new Color(220, 220, 220));
            veganBtn.setForeground(Color.BLACK);
            vegetarischBtn.setBackground(new Color(220, 220, 220));
            vegetarischBtn.setForeground(Color.BLACK);
            veganBtn.setEnabled(false);
            vegetarischBtn.setEnabled(false);
        }

        // Toggle logic and appearance
        veganBtn.addActionListener(e -> {
            if (Main.currentStudent != null) {
                veganFilterActive = veganBtn.isSelected();
                veganBtn.setBackground(veganFilterActive ? new Color(91, 180, 56) : new Color(220, 220, 220));
                veganBtn.setForeground(veganFilterActive ? Color.WHITE : Color.BLACK);
                Main.currentStudent.isVegan = veganFilterActive;
                Main.saveStudents();
                updateMenuList();
            } else {
                veganBtn.setSelected(false);
            }
        });
        vegetarischBtn.addActionListener(e -> {
            if (Main.currentStudent != null) {
                vegetarischFilterActive = vegetarischBtn.isSelected();
                vegetarischBtn.setBackground(vegetarischFilterActive ? new Color(91, 180, 56) : new Color(220, 220, 220));
                vegetarischBtn.setForeground(vegetarischFilterActive ? Color.WHITE : Color.BLACK);
                Main.currentStudent.isVegetarian = vegetarischFilterActive;
                Main.saveStudents();
                updateMenuList();
            } else {
                vegetarischBtn.setSelected(false);
            }
        });
    }

    // Registers this view with the main application. Should be called after construction.
    public void registerWithMain() {
        Main.registerMenuListView(this);
    }

    private void updateMenuList() {
        menuPanel.removeAll();
        String[] excludedMenuLines = {"Salat-/ Gemüsebuffet 100g", "Beilagen SB", "Dessert vorport.", "Dessert SB"};
        LocalDate now = LocalDate.now();
        String today = now.toString();
        List<MealItem> matching = new ArrayList<>();
        List<MealItem> nonMatching = new ArrayList<>();
        boolean anyToday = false;
        for (MealItem item : Main.fetchedMealItems) {
            if (today.equals(item.menuDate)) {
                boolean exclude = false;
                // Exclude standard menu lines
                for (String excl : excludedMenuLines) {
                    if (excl.equals(item.menuLine)) {
                        exclude = true;
                        break;
                    }
                }
                if (exclude) continue;
                anyToday = true;
                // Check if item is vegan or vegetarian
                boolean isVegan = false, isVegetarian = false;
                if (item.icons != null) {
                    for (String icon : item.icons) {
                        if (icon.equalsIgnoreCase("VEG") || icon.equalsIgnoreCase("Vegan")) isVegan = true;
                        if (icon.equalsIgnoreCase("V")) isVegetarian = true;
                    }
                }
                // Vegetarian filter includes vegan
                boolean matches = false;
                if (veganFilterActive && isVegan) matches = true;
                if (vegetarischFilterActive && (isVegan || isVegetarian)) matches = true;
                if ((veganFilterActive || vegetarischFilterActive)) {
                    if (matches) matching.add(item); else nonMatching.add(item);
                } else {
                    matching.add(item);
                }
            }
        }
        List<MealItem> toShow = new ArrayList<>();
        toShow.addAll(matching);
        toShow.addAll(nonMatching);
        if (anyToday && !toShow.isEmpty()) {
            for (int i = 0; i < toShow.size(); i++) {
                MealItem item = toShow.get(i);
                StringBuilder desc = new StringBuilder();
                // Join ingredients into one string
                if (item.menu != null) {
                    for (String m : item.menu) {
                        desc.append(m).append(" ");
                    }
                }
                // Set color based on matching/non-matching
                Color cardColor = (i < matching.size()) ? new Color(91, 180, 56) : new Color(143, 23, 46);
                menuPanel.add(createMenuPanel(
                        item.menuLine,
                        desc.toString().trim(),
                        item.studentPrice + "€",
                        cardColor
                ));
                menuPanel.add(Box.createRigidArea(new Dimension(0, 20)));
            }
        } else {
            // No meals found
            JLabel noMeals = new JLabel("Keine Menüs für heute gefunden.");
            noMeals.setFont(new Font("Arial", Font.BOLD, 22));
            menuPanel.add(noMeals);
        }
        menuPanel.revalidate();
        menuPanel.repaint();
    }

    // Syncs the filter state with the current student
    public void syncFilterStateWithCurrentStudent() {
        if (Main.currentStudent != null) {
            // Set filter states from current student
            veganFilterActive = Main.currentStudent.isVegan;
            vegetarischFilterActive = Main.currentStudent.isVegetarian;
            veganBtn.setSelected(veganFilterActive);
            vegetarischBtn.setSelected(vegetarischFilterActive);
            veganBtn.setBackground(veganFilterActive ? new Color(91, 180, 56) : new Color(220, 220, 220));
            veganBtn.setForeground(veganFilterActive ? Color.WHITE : Color.BLACK);
            vegetarischBtn.setBackground(vegetarischFilterActive ? new Color(91, 180, 56) : new Color(220, 220, 220));
            vegetarischBtn.setForeground(vegetarischFilterActive ? Color.WHITE : Color.BLACK);
            // Update menu list
            updateMenuList();
            veganBtn.setEnabled(true);
            vegetarischBtn.setEnabled(true);
        } else {
            // No student logged in, disable filters
            veganBtn.setSelected(false);
            vegetarischBtn.setSelected(false);
            veganBtn.setBackground(new Color(220, 220, 220));
            veganBtn.setForeground(Color.BLACK);
            vegetarischBtn.setBackground(new Color(220, 220, 220));
            vegetarischBtn.setForeground(Color.BLACK);
            veganBtn.setEnabled(false);
            vegetarischBtn.setEnabled(false);
        }
    }

    // Resets the filters to default
    public void resetFilters() {
        veganFilterActive = false;
        vegetarischFilterActive = false;
        veganBtn.setSelected(false);
        vegetarischBtn.setSelected(false);
        veganBtn.setBackground(new Color(220, 220, 220));
        veganBtn.setForeground(Color.BLACK);
        vegetarischBtn.setBackground(new Color(220, 220, 220));
        vegetarischBtn.setForeground(Color.BLACK);
        updateMenuList();
    }

    // Creates a menu panel with a title, description, price and background color
    public static JPanel createMenuPanel(String title, String desc, String price, Color bg) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(bg);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        panel.setPreferredSize(new Dimension(1000, 80));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        JLabel descLabel = new JLabel(desc);
        descLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        JLabel priceLabel = new JLabel(price);
        priceLabel.setFont(new Font("Arial", Font.BOLD, 28));
        priceLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        Color fg = (bg.getRed() > 100) ? Color.WHITE : Color.BLACK;
        titleLabel.setForeground(fg);
        descLabel.setForeground(fg);
        priceLabel.setForeground(fg);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.7;
        panel.add(titleLabel, gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.7;
        panel.add(descLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.gridheight = 2; gbc.weightx = 0.3;
        panel.add(priceLabel, gbc);

        return panel;
    }
} 