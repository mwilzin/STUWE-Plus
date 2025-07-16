package de.mwilzinDario.stuwe_advanced.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import de.mwilzinDario.stuwe_advanced.Main;

public final class MainView extends JPanel {
    private final JPanel topContainer;
    private final MenuListView menuListView;
    private final JSplitPane splitPane;
    private final JLabel iconLabel;
    private final JLayeredPane layeredPane;
    private LoggedInTopView currentLoggedInView;

    public MainView() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // Initialize all components first
        topContainer = new JPanel(new BorderLayout());
        topContainer.setBackground(Color.WHITE);
        
        menuListView = new MenuListView();
        menuListView.registerWithMain();

        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topContainer, menuListView);
        splitPane.setResizeWeight(0.5);
        splitPane.setDividerLocation(0.5);
        splitPane.setDividerSize(0);
        splitPane.setBorder(null);

        ImageIcon logoIcon = new ImageIcon(Main.class.getResource("/logo.jpg"));
        Image logoImg = Main.getScaledImagePreserveRatio(logoIcon.getImage(), 250, 80);
        iconLabel = new JLabel(new ImageIcon(logoImg));
        iconLabel.setOpaque(false);
        iconLabel.setBounds(0, 0, 250, 80);

        layeredPane = new JLayeredPane();
        layeredPane.setLayout(null);
        layeredPane.add(splitPane, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(iconLabel, JLayeredPane.PALETTE_LAYER);
        add(layeredPane, BorderLayout.CENTER);

        // Now that all components are initialized, set up the initial view
        updateView();
    }

    @Override
    public void doLayout() {
        super.doLayout();
        int w = getWidth();
        int h = getHeight();
        layeredPane.setBounds(0, 0, w, h);
        splitPane.setBounds(0, 0, w, h);
        iconLabel.setBounds(w - 250, 0, 250, 80);
    }

    // Updates the view based on whether there is a current user
    public void updateView() {
        if (Main.currentStudent != null) {
            showLoggedInTopView();
        } else {
            showPresentIDView();
        }
    }

    // Shows the logged-in view
    public void showLoggedInTopView() {
        // Clean up previous view if it exists
        if (currentLoggedInView != null) {
            currentLoggedInView.cleanup();
        }
        
        topContainer.removeAll();
        currentLoggedInView = new LoggedInTopView(this::showPresentIDView);
        topContainer.add(currentLoggedInView, BorderLayout.CENTER);
        topContainer.revalidate();
        topContainer.repaint();
    }

    // Shows the present ID view
    public void showPresentIDView() {
        // Clean up current view
        if (currentLoggedInView != null) {
            currentLoggedInView.cleanup();
            currentLoggedInView = null;
        }
        // Reset UI filters on logout
        menuListView.resetFilters();
        topContainer.removeAll();
        PresentIDView presentIDView = new PresentIDView();
        topContainer.add(presentIDView, BorderLayout.CENTER);
        topContainer.revalidate();
        topContainer.repaint();
    }

    public void resetLoggedInTimer() {
        if (currentLoggedInView != null) {
            currentLoggedInView.resetTimer();
        }
    }

    // Sets this instance as the main view. Should be called after construction.
    public void setAsMainView() {
        Main.mainView = this;
    }
} 