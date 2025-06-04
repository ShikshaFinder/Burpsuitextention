package com.burp.xss;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.http.HttpService;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import burp.api.montoya.scanner.Scanner;
import burp.api.montoya.ui.editor.extension.EditorMode;
import burp.api.montoya.ui.editor.extension.ExtensionProvidedHttpRequestEditor;
import burp.api.montoya.ui.editor.extension.ExtensionProvidedHttpResponseEditor;
import burp.api.montoya.ui.editor.extension.HttpRequestEditorProvider;
import burp.api.montoya.ui.editor.extension.HttpResponseEditorProvider;
import burp.api.montoya.ui.menu.Menu;
import burp.api.montoya.ui.menu.MenuItem;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BlindXssInjector implements BurpExtension {
    private MontoyaApi api;
    private JPanel mainPanel;
    private JTextArea payloadArea;
    private JTextField telegramTokenField;
    private JTextField telegramChatIdField;
    private JToggleButton enableButton;
    private JTextArea logArea;
    private List<String> payloads;
    private ExecutorService executorService;
    private TelegramNotifier telegramNotifier;

    @Override
    public void initialize(MontoyaApi api) {
        this.api = api;
        this.executorService = Executors.newFixedThreadPool(5);
        this.payloads = new ArrayList<>();
        this.telegramNotifier = new TelegramNotifier();

        // Set extension name
        api.extension().setName("Blind XSS Injector");

        // Create UI
        createUI();

        // Register HTTP request handler
        api.http().registerHttpHandler(new BlindXssHttpHandler(this));

        // Register context menu items
        api.userInterface().menuBar().registerMenu(Menu.caption("Blind XSS Injector")
                .withMenuItem(MenuItem.caption("Load Payloads")
                        .withAction(this::loadPayloads))
                .withMenuItem(MenuItem.caption("Save Payloads")
                        .withAction(this::savePayloads)));
    }

    private void createUI() {
        mainPanel = new JPanel(new BorderLayout());

        // Control Panel
        JPanel controlPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Enable/Disable Toggle
        enableButton = new JToggleButton("Enable Injector");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        controlPanel.add(enableButton, gbc);

        // Telegram Settings
        gbc.gridy = 1;
        controlPanel.add(new JLabel("Telegram Bot Token:"), gbc);
        telegramTokenField = new JTextField(30);
        gbc.gridy = 2;
        controlPanel.add(telegramTokenField, gbc);

        gbc.gridy = 3;
        controlPanel.add(new JLabel("Telegram Chat ID:"), gbc);
        telegramChatIdField = new JTextField(30);
        gbc.gridy = 4;
        controlPanel.add(telegramChatIdField, gbc);

        // Payload Configuration
        gbc.gridy = 5;
        controlPanel.add(new JLabel("XSS Payloads:"), gbc);
        payloadArea = new JTextArea(10, 30);
        payloadArea.setLineWrap(true);
        JScrollPane payloadScroll = new JScrollPane(payloadArea);
        gbc.gridy = 6;
        controlPanel.add(payloadScroll, gbc);

        // Log Area
        gbc.gridy = 7;
        controlPanel.add(new JLabel("Injection Log:"), gbc);
        logArea = new JTextArea(10, 30);
        logArea.setEditable(false);
        JScrollPane logScroll = new JScrollPane(logArea);
        gbc.gridy = 8;
        controlPanel.add(logScroll, gbc);

        mainPanel.add(controlPanel, BorderLayout.CENTER);

        // Add the panel to Burp's UI
        api.userInterface().registerSuiteTab("Blind XSS Injector", mainPanel);
    }

    private void loadPayloads() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(mainPanel) == JFileChooser.APPROVE_OPTION) {
            // TODO: Implement payload loading from file
        }
    }

    private void savePayloads() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(mainPanel) == JFileChooser.APPROVE_OPTION) {
            // TODO: Implement payload saving to file
        }
    }

    public void logInjection(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    public boolean isEnabled() {
        return enableButton.isSelected();
    }

    public String getTelegramToken() {
        return telegramTokenField.getText();
    }

    public String getTelegramChatId() {
        return telegramChatIdField.getText();
    }

    public List<String> getPayloads() {
        return payloads;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public TelegramNotifier getTelegramNotifier() {
        return telegramNotifier;
    }
} 