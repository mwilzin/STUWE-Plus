package de.mwilzinDario.stuwe_advanced.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.mwilzinDario.stuwe_advanced.Main;

public class PresentIDView extends JPanel {
    public PresentIDView() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.WHITE);
        // Header row container
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        // Icon
        ImageIcon iconIcon = new ImageIcon(Main.class.getResource("/id_icon.png"));
        Image iconImg = Main.getScaledImagePreserveRatio(iconIcon.getImage(), 100, 100);
        JLabel iconLabel = new JLabel(new ImageIcon(iconImg));
        headerPanel.add(iconLabel);
        add(headerPanel);
        // Instruction Text
        JLabel scanLabel = new JLabel("Scanne deinen Studentenausweis...");
        scanLabel.setFont(new Font("Arial", Font.PLAIN, 22));
        scanLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        scanLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(scanLabel);
    }
} 