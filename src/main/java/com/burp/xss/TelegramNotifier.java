package com.burp.xss;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.concurrent.CompletableFuture;

public class TelegramNotifier {
    private TelegramBotsApi botsApi;
    private String botToken;
    private String chatId;

    public TelegramNotifier() {
        try {
            botsApi = new TelegramBotsApi(DefaultBotSession.class);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendNotification(String botToken, String chatId, String message) {
        this.botToken = botToken;
        this.chatId = chatId;

        CompletableFuture.runAsync(() -> {
            try {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(chatId);
                sendMessage.setText(message);
                sendMessage.enableMarkdown(true);

                TelegramLongPollingBot bot = new TelegramLongPollingBot() {
                    @Override
                    public void onUpdateReceived(Update update) {
                        // Not needed for sending messages
                    }

                    @Override
                    public String getBotUsername() {
                        return "BlindXssNotifier";
                    }

                    @Override
                    public String getBotToken() {
                        return botToken;
                    }
                };

                bot.execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        });
    }
} 