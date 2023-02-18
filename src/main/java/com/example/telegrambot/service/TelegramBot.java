package com.example.telegrambot.service;

import com.example.telegrambot.config.BotConfig;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class TelegramBot extends TelegramLongPollingBot {


    private final CurrencyService currencyService;

    private final BotConfig config;

    public TelegramBot(CurrencyService currencyService, BotConfig config) {
        this.currencyService = currencyService;
        this.config = config;
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            String data = callbackQuery.getData();
            long chatId = callbackQuery.getMessage().getChatId();

            switch (data) {
                case "/menu":
                    sendMenu(chatId);
                    break;
                case "/Доллар_в_Тенге":
                    exchangeRateKZT(chatId);
                    break;
                case "/Доллар_в_Рублях":
                    exchangeRateRUB(chatId);
                    break;
                case "/Рубль_в_Тенге":
                    rubToKztRate(chatId);
                    break;
                case "/Доллар_в_валютах_мира":
                    exchangeRate(chatId);
                    break;
                default:
                    // Handle other callback data
                    break;
            }
        } else if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            switch (messageText) {
                case "/start":
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    break;
                case "/menu":
                    sendMenu(chatId);
                    break;
                case "/Доллар_в_валютах_мира":
                    exchangeRate(chatId);
                    break;
                default:
                    // Handle other message text
                    break;
            }
        }
    }


    private void startCommandReceived(long chatId, String name) {
        String answer = "Привет, " + name + ", введи команду /menu";
        var message = new SendMessage();
        message.setText(answer);
        sendMessage(chatId, message);
    }

    private void exchangeRate(long chatId) {
        String exchangeRateMessage;

        try {
            exchangeRateMessage = currencyService.getExchangeRateAsString();
        } catch (IOException e) {
            exchangeRateMessage = "Failed to fetch exchange rates";
        }
        var message = new SendMessage();
        message.setText(exchangeRateMessage);
        sendMessage(chatId, message);
    }

    private void exchangeRateKZT(long chatId) {
        String exchangeRateMessage;

        try {
            double exchangeRate = currencyService.getExchangeRate("KZT");
            exchangeRateMessage = String.format("1 USD = %.2f KZT", exchangeRate);
        } catch (IOException e) {
            exchangeRateMessage = "Failed to fetch exchange rates";
        }

        var message = new SendMessage();
        message.setText(exchangeRateMessage);
        sendMessage(chatId, message);
    }

    private void exchangeRateRUB(long chatId) {
        String exchangeRateMessage;
        try {
            double exchangeRate = currencyService.getExchangeRate("RUB");
            exchangeRateMessage = String.format("1 USD = %.2f RUB", exchangeRate);
        } catch (IOException e) {
            exchangeRateMessage = "Failed to fetch exchange rates";
        }

        var message = new SendMessage();
        message.setText(exchangeRateMessage);
        sendMessage(chatId, message);
    }

    private void rubToKztRate(long chatId) {
        String rubToKztRateMessage;

        try {
            double exchangeRate = currencyService.getRUBToKZTExchangeRate();
            rubToKztRateMessage = String.format("1 RUB = %.2f KZT", exchangeRate);

        } catch (IOException e) {
            rubToKztRateMessage = "Failed to fetch RUB to KZT rate";
        }

        var message = new SendMessage();
        message.setText(rubToKztRateMessage);
        sendMessage(chatId, message);
    }


    private void sendMenu(long chatId) {
        var message = new SendMessage();
        message.setText("Select a command:");

        var inlineKeyboardMarkup = new InlineKeyboardMarkup();
        var keyboardRows = new ArrayList<List<InlineKeyboardButton>>();

        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("Доллар в Рублях");
        button1.setCallbackData("/Доллар_в_Рублях");

        InlineKeyboardButton button2 = new InlineKeyboardButton();
        button2.setText("Доллар в Тенге");
        button2.setCallbackData("/Доллар_в_Тенге");

        InlineKeyboardButton button3 = new InlineKeyboardButton();
        button3.setText("Рубль в Тенге");
        button3.setCallbackData("/Рубль_в_Тенге");

        InlineKeyboardButton button4 = new InlineKeyboardButton();
        button4.setText("Доллар в валютах_мира");
        button4.setCallbackData("/Доллар_в_валютах_мира");

        InlineKeyboardButton button5 = new InlineKeyboardButton();
        button5.setText("menu");
        button5.setCallbackData("/menu");

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(button1);
        row1.add(button2);
        keyboardRows.add(row1);

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(button3);
        row2.add(button4);
        keyboardRows.add(row2);

        List<InlineKeyboardButton> row3 = new ArrayList<>();
        row3.add(button5);
        keyboardRows.add(row3);

        inlineKeyboardMarkup.setKeyboard(keyboardRows);
        message.setReplyMarkup(inlineKeyboardMarkup);
        message.setChatId(String.valueOf(chatId));

        sendMessage(chatId, message);
    }


    private void sendMessage(long chatId, SendMessage message) {
        message.setChatId(String.valueOf(chatId));
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
