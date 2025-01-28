package com.jobportal.seeker;

import com.jobportal.utils.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

public class JobSeekerWindow {
    private List<JCheckBox> jobCheckBoxes = new ArrayList<>();
    private ButtonGroup jobButtonGroup = new ButtonGroup();
    private JPanel jobPanel;

    public void showWindow() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        JFrame frame = new JFrame("Job Seeker - Job Listings");
        frame.setSize(600, 500);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setContentPane(createContentPane());
        frame.setVisible(true);
    }

    private JPanel createContentPane() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(240, 248, 255)); // Light blue background

        JLabel titleLabel = new JLabel("Job Listings", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(new Color(30, 144, 255)); // Dodger blue color
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        jobPanel = new JPanel();
        jobPanel.setLayout(new BoxLayout(jobPanel, BoxLayout.Y_AXIS));
        jobPanel.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(jobPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(30, 144, 255), 2));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        return mainPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setOpaque(false);

        JButton refreshButton = createStyledButton("Refresh Job Listings");
        JButton goToLinkButton = createStyledButton("Go to Link");
        JButton deleteJobButton = createStyledButton("Delete Job");

        buttonPanel.add(refreshButton);
        buttonPanel.add(goToLinkButton);
        buttonPanel.add(deleteJobButton);

        setupButtonActions(refreshButton, goToLinkButton, deleteJobButton);

        return buttonPanel;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(new Color(0, 0, 255));  // Blue text
        button.setBackground(Color.WHITE);  // White background to contrast with text
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(new Color(30, 144, 255), 2)); // Dodger blue border
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void setupButtonActions(JButton refreshButton, JButton goToLinkButton, JButton deleteJobButton) {
        refreshButton.addActionListener(e -> refreshJobListings());
        goToLinkButton.addActionListener(e -> openSelectedJobLink());
        deleteJobButton.addActionListener(e -> deleteSelectedJob());
    }

    private void refreshJobListings() {
        jobPanel.removeAll();
        jobCheckBoxes.clear();
        jobButtonGroup.clearSelection();

        try (Connection conn = DBConnection.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Jobs");

            while (rs.next()) {
                String jobTitle = rs.getString("title");
                String company = rs.getString("company");
                String qualifications = rs.getString("qualifications");
                String link = rs.getString("link");

                JCheckBox jobCheckBox = createJobCheckBox(jobTitle, company, qualifications, link);
                jobPanel.add(jobCheckBox);
                jobPanel.add(Box.createRigidArea(new Dimension(0, 5))); // Add space between checkboxes
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error refreshing job listings: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        jobPanel.revalidate();
        jobPanel.repaint();
    }

    private JCheckBox createJobCheckBox(String jobTitle, String company, String qualifications, String link) {
        JCheckBox jobCheckBox = new JCheckBox(jobTitle + " - " + company);
        jobCheckBox.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        jobCheckBox.setToolTipText("<html>Qualifications: " + qualifications + "<br>Link: " + link + "</html>");
        jobCheckBox.setBackground(Color.WHITE);
        jobButtonGroup.add(jobCheckBox);
        jobCheckBoxes.add(jobCheckBox);
        return jobCheckBox;
    }

    private void openSelectedJobLink() {
        for (JCheckBox jobCheckBox : jobCheckBoxes) {
            if (jobCheckBox.isSelected()) {
                String selectedJob = jobCheckBox.getText();
                try (Connection conn = DBConnection.getConnection()) {
                    String sql = "SELECT link FROM Jobs WHERE title = ?";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, selectedJob.split(" - ")[0]);
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next()) {
                        String jobLink = rs.getString("link");
                        Desktop.getDesktop().browse(java.net.URI.create(jobLink));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Error opening job link: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
                break;
            }
        }
    }

    private void deleteSelectedJob() {
        for (JCheckBox jobCheckBox : jobCheckBoxes) {
            if (jobCheckBox.isSelected()) {
                String selectedJob = jobCheckBox.getText().split(" - ")[0];
                int confirm = JOptionPane.showConfirmDialog(null, 
                    "Are you sure you want to delete the job: " + selectedJob + "?", 
                    "Confirm Deletion", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    deleteJobFromDatabase(selectedJob);
                    refreshJobListings();
                }
                break;
            }
        }
    }

    private void deleteJobFromDatabase(String jobTitle) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "DELETE FROM Jobs WHERE title = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, jobTitle);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(null, "Job '" + jobTitle + "' deleted successfully!");
            } else {
                JOptionPane.showMessageDialog(null, "No job found with title: " + jobTitle, "Deletion Failed", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error deleting job: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
