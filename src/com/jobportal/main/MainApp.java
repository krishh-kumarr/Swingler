package com.jobportal.main;

import com.jobportal.seeker.JobSeekerWindow;
import com.jobportal.provider.JobProviderWindow;
import com.jobportal.utils.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MainApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            JFrame frame = new JFrame("Job Portal - Registration");
            frame.setSize(400, 350);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setContentPane(createContentPane());
            frame.setVisible(true);
        });
    }

    private static JPanel createContentPane() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(new Color(240, 248, 255)); 

        JLabel titleLabel = new JLabel("Welcome to Job Portal", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(30, 144, 255)); 
        panel.add(titleLabel, BorderLayout.NORTH);

        panel.add(createFormPanel(), BorderLayout.CENTER);

        return panel;
    }

    private static JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JTextField userText = new JTextField(20);
        JPasswordField passwordText = new JPasswordField(20);
        String[] roles = {"seeker", "provider"};
        JComboBox<String> roleList = new JComboBox<>(roles);

        addFormField(formPanel, gbc, "Username:", userText, 0);
        addFormField(formPanel, gbc, "Password:", passwordText, 1);
        addFormField(formPanel, gbc, "Role:", roleList, 2);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setOpaque(false);

        JButton registerButton = createStyledButton("Register");
        JButton loginButton = createStyledButton("Login");

        buttonPanel.add(registerButton);
        buttonPanel.add(loginButton);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);

        setupButtonActions(registerButton, loginButton, userText, passwordText, roleList);

        return formPanel;
    }

    private static void addFormField(JPanel panel, GridBagConstraints gbc, String labelText, JComponent component, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.gridy = row;
        panel.add(component, gbc);
    }

    private static JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));  
        button.setForeground(new Color(0, 0, 255));  
        button.setBackground(new Color(240, 248, 255));  
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private static void setupButtonActions(JButton registerButton, JButton loginButton,
                                           JTextField userText, JPasswordField passwordText, JComboBox<String> roleList) {
        registerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                registerUser(userText.getText(), new String(passwordText.getPassword()), (String) roleList.getSelectedItem());
            }
        });

        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loginUser(userText.getText(), new String(passwordText.getPassword()), (String) roleList.getSelectedItem());
            }
        });
    }

    private static void registerUser(String username, String password, String role) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO Users (username, password, role) VALUES (?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, role);

            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(null, "Registration successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Registration failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void loginUser(String username, String password, String role) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM Users WHERE username = ? AND password = ? AND role = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, role);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                JOptionPane.showMessageDialog(null, "Login successful!", "Success", JOptionPane.INFORMATION_MESSAGE);

                if (role.equals("seeker")) {
                    new JobSeekerWindow().showWindow();
                } else {
                    new JobProviderWindow().showWindow();
                }
            } else {
                JOptionPane.showMessageDialog(null, "Invalid credentials!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Login failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
