package com.jobportal.provider;

import com.jobportal.utils.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class JobProviderWindow {
    private JTextField titleText, companyText, qualificationsText, linkText;

    public void showWindow() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        JFrame frame = new JFrame("Job Provider - Post a Job");
        frame.setSize(500, 400);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setContentPane(createContentPane());
        frame.setVisible(true);
    }

    private JPanel createContentPane() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(240, 248, 255)); // Light blue background

        JLabel titleLabel = new JLabel("Post a New Job", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(30, 144, 255)); // Dodger blue color
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel formPanel = createFormPanel();
        mainPanel.add(formPanel, BorderLayout.CENTER);

        JButton postButton = createStyledButton("Post Job");
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        buttonPanel.add(postButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        postButton.addActionListener(e -> postJob());

        return mainPanel;
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        titleText = new JTextField(20);
        companyText = new JTextField(20);
        qualificationsText = new JTextField(20);
        linkText = new JTextField(20);

        addFormField(formPanel, gbc, "Job Title:", titleText, 0);
        addFormField(formPanel, gbc, "Company:", companyText, 1);
        addFormField(formPanel, gbc, "Qualifications:", qualificationsText, 2);
        addFormField(formPanel, gbc, "Job Link:", linkText, 3);

        return formPanel;
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, String labelText, JComponent component, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.gridy = row;
        panel.add(component, gbc);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(new Color(30, 144, 255)); // Blue text
        button.setBackground(Color.WHITE); // White background
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(new Color(30, 144, 255), 2)); // Blue border
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void postJob() {
        String title = titleText.getText();
        String company = companyText.getText();
        String qualifications = qualificationsText.getText();
        String link = linkText.getText();

        if (title.isEmpty() || company.isEmpty() || qualifications.isEmpty() || link.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please fill in all fields!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO Jobs (title, company, qualifications, link) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, title);
            pstmt.setString(2, company);
            pstmt.setString(3, qualifications);
            pstmt.setString(4, link);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(null, "Job posted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearFields();
            } else {
                JOptionPane.showMessageDialog(null, "Failed to post job. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error posting job: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {
        titleText.setText("");
        companyText.setText("");
        qualificationsText.setText("");
        linkText.setText("");
    }
}
