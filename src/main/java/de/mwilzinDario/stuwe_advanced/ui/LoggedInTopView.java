package de.mwilzinDario.stuwe_advanced.ui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

import de.mwilzinDario.stuwe_advanced.Main;
import de.mwilzinDario.stuwe_advanced.models.Balances;

public class LoggedInTopView extends JPanel {
    private JLabel timerLabel;
    private Timer countdownTimer;
    private int timeLeft = 60;
    
    public LoggedInTopView(Runnable onLogout) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Left: Balance
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(Color.WHITE);
        
        // Get latest balance
        String balanceText = "0,00€";
        if (Main.currentStudent != null && Main.currentStudent.balances != null && Main.currentStudent.balances.length > 0) {
            // Sort balances by date to get the latest
            Balances[] sortedBalances = Arrays.copyOf(Main.currentStudent.balances, Main.currentStudent.balances.length);
            Arrays.sort(sortedBalances, Comparator.comparing(b -> b.date));
            double latestBalance = sortedBalances[sortedBalances.length - 1].amount;
            balanceText = String.format("%.2f€", latestBalance).replace(".", ",");
        }
        
        JLabel balanceLabel = new JLabel(balanceText);
        balanceLabel.setFont(new Font("Arial", Font.BOLD, 64));
        balanceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel balanceTextLabel = new JLabel("Dein Kontostand");
        balanceTextLabel.setFont(new Font("Arial", Font.PLAIN, 22));
        balanceTextLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftPanel.add(Box.createVerticalGlue());
        leftPanel.add(balanceLabel);
        leftPanel.add(balanceTextLabel);
        leftPanel.add(Box.createVerticalGlue());
        leftPanel.setBorder(BorderFactory.createEmptyBorder(0, 40, 0, 0));
        add(leftPanel, BorderLayout.WEST);

        // Center: Graph and timer label in a horizontal row
        JPanel centerRowPanel = new JPanel();
        centerRowPanel.setLayout(new BoxLayout(centerRowPanel, BoxLayout.X_AXIS));
        centerRowPanel.setBackground(Color.WHITE);

        // Graph panel
        JPanel graphPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                int leftPad = 100;
                int rightPad = 30;
                int topPad = 30;
                int bottomPad = 40;
                int w = getWidth();
                int h = getHeight();
                int graphW = w - leftPad - rightPad;
                int graphH = h - topPad - bottomPad;

                // Draw graph border
                g2.setColor(Color.LIGHT_GRAY);
                g2.drawRect(leftPad, topPad, graphW, graphH);

                // Draw axes labels
                g2.setColor(Color.BLACK);
                g2.setFont(new Font("Arial", Font.PLAIN, 14));
                g2.drawString("€", leftPad - 25, topPad + 10);

                if (Main.currentStudent != null && Main.currentStudent.balances != null && Main.currentStudent.balances.length > 0) {
                    // Sort balances by date
                    Balances[] sortedBalances = Arrays.copyOf(Main.currentStudent.balances, Main.currentStudent.balances.length);
                    Arrays.sort(sortedBalances, Comparator.comparing(b -> b.date));

                    // Find min and max values for scaling
                    double minAmount = Arrays.stream(sortedBalances).mapToDouble(b -> b.amount).min().orElse(0);
                    double maxAmount = Arrays.stream(sortedBalances).mapToDouble(b -> b.amount).max().orElse(50);
                    double range = Math.max(maxAmount - minAmount, 1); // Ensure range is at least 1

                    // Draw Y-axis ticks and labels (rounded, fewer labels)
                    int yTicks = 2; // 3 labels: min, mid, max
                    for (int i = 0; i <= yTicks; i++) {
                        int y = topPad + (int) (graphH * i / (double) yTicks);
                        double value = maxAmount - (range * i / (double) yTicks);
                        int roundedValue = (int)Math.round(value);
                        g2.setColor(Color.GRAY);
                        g2.drawLine(leftPad - 5, y, leftPad, y);
                        g2.setColor(Color.BLACK);
                        g2.drawString(String.format("%d €", roundedValue), leftPad - 55, y + 5);
                    }

                    // Draw X-axis ticks and labels (first, middle, last)
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy");
                    int nPoints = sortedBalances.length;
                    int[] xTickIndices = {0, nPoints/2, nPoints-1};
                    for (int idx : xTickIndices) {
                        if (idx < 0 || idx >= nPoints) continue;
                        int x = leftPad + (int) (graphW * idx / (double) (nPoints - 1));
                        g2.setColor(Color.GRAY);
                        g2.drawLine(x, topPad + graphH, x, topPad + graphH + 5);
                        g2.setColor(Color.BLACK);
                        g2.drawString(dateFormat.format(sortedBalances[idx].date), x - 20, topPad + graphH + 20);
                    }

                    // Draw line graph
                    g2.setColor(new Color(0, 120, 255));
                    int[] px = new int[nPoints];
                    int[] py = new int[nPoints];
                    for (int i = 0; i < nPoints; i++) {
                        px[i] = leftPad + (int) (graphW * i / (double) (nPoints - 1));
                        py[i] = topPad + (int) (graphH - (graphH * (sortedBalances[i].amount - minAmount) / range));
                    }
                    g2.setStroke(new BasicStroke(3f));
                    g2.drawPolyline(px, py, nPoints);
                }
            }
        };
        graphPanel.setPreferredSize(new Dimension(700, 200));
        graphPanel.setBackground(Color.WHITE);
        centerRowPanel.add(graphPanel);

        // Timer panel (right of graph)
        JPanel timerPanel = new JPanel();
        timerPanel.setLayout(new BoxLayout(timerPanel, BoxLayout.X_AXIS));
        timerPanel.setBackground(Color.WHITE);
        timerPanel.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 40));
        
        JLabel logoutText = new JLabel("Abmelden in: ");
        logoutText.setFont(new Font("Arial", Font.PLAIN, 22));
        logoutText.setForeground(Color.BLACK);
        
        timerLabel = new JLabel("60");
        timerLabel.setFont(new Font("Arial", Font.BOLD, 22));
        timerLabel.setForeground(new Color(143, 23, 46)); // Red color for urgency
        
        timerPanel.add(logoutText);
        timerPanel.add(timerLabel);
        centerRowPanel.add(Box.createHorizontalGlue());
        centerRowPanel.add(timerPanel);

        add(centerRowPanel, BorderLayout.CENTER);

        // Start countdown timer
        countdownTimer = new Timer(1000, e -> {
            timeLeft--;
            timerLabel.setText(String.valueOf(timeLeft));
            if (timeLeft <= 0) {
                countdownTimer.stop();
                Main.currentStudent = null; // Log out
                if (onLogout != null) {
                    onLogout.run();
                }
            }
        });
        countdownTimer.start();
    }

    // Clean up timer when view is removed
    public void cleanup() {
        if (countdownTimer != null && countdownTimer.isRunning()) {
            countdownTimer.stop();
        }
    }

    public void resetTimer() {
        if (countdownTimer != null) {
            countdownTimer.stop();
        }
        timeLeft = 60;
        timerLabel.setText(String.valueOf(timeLeft));
        countdownTimer.start();
    }
} 