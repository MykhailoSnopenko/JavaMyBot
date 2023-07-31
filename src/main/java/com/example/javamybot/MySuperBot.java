package com.example.javamybot;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.util.List;
import java.util.Optional;

@Component
public class MySuperBot extends TelegramLongPollingBot {

    private final BotCredentials botCredentials;
    private final UserService userService;

    public MySuperBot(TelegramBotsApi telegramBotsApi,
                      BotCredentials botCredentials, UserService userService) {
        super((botCredentials.getBotToken()));

        this.botCredentials = botCredentials;
        this.userService = userService;

        try {
            telegramBotsApi.registerBot(this);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if(!update.hasMessage() || !update.getMessage().hasText())
            return;

        String text = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();

        Optional<User> userOpt = userService.findUserByChatId(chatId);
        User user;

        if (userOpt.isPresent()) {
            user = userOpt.get();

            if(isAdminCommand(text)) {
                processAdminCommand(chatId, text);
                return;
            }

            if(isDeleteCommand(text)) {
                processDeleteCommand(chatId);
                return;
            }

            if(user.getState() == 1) {
                boolean valid = Utils.isValidUkrainianPhoneNumber(text);

                if(valid) {
                    user.setPhone(text);
                    sendMessage(chatId, "Thanks! What is you name?");
                    user.incrementState();
                } else {
                    sendMessage(chatId, "Wrong phone number! Try again!");
                }

            } else if(user.getState() == 2) {
                user.setName(text);
                sendMessage(chatId, "Thanks!");
                user.incrementState();
            }

            userService.updateUser(user);

        } else {
            sendPhotoToUser(chatId);
            System.out.println(text);

            String[] parts = text.split(" ");
            String password = (parts.length == 2) ? parts[1] : "";

            sendMessage(chatId, "Hello, I'm bot!");

            sendMessage(chatId, "What is your phone number (380XXXXXXXX)?");

            user = new User();

            user.setAdmin(isValidPassword(password));
            user.setChatId(chatId);
            user.setState(1L);


            userService.saveUser(user);
        }
    }

    private Boolean isValidPassword(String password) {
        return "SecretPassword".equals(password);
    }

    private void processAdminCommand(long chatId, String text) {

        String[] parts = text.split(" ");
        if(parts.length < 2 && (!parts[1].equals("set") || !parts[1].equals("broadcast"))) {
            sendMessage(chatId, "Wrong admin command! Try again!");
            return;
        }

        if(parts[1].equals("set")){
            Long chatIdUserToAdmin = Long.valueOf(parts[2]);
            Optional<User> userOpt = userService.findUserByChatId(chatIdUserToAdmin);
            User user = userOpt.get();
            user.setAdmin(true);
            userService.updateUser(user);
        }

        String message = "";
        if(parts[1].equals("broadcast")) {
            for (int i = 2; i < parts.length; i++) {
                message = message + " " + parts[i];
            }
        }

        List<User> users = userService.findAllUser();
        String finalMessage = message;
        users.forEach(user -> sendMessage(user.getChatId(), finalMessage));
    }

    private boolean isAdminCommand(String text) {
        return text.startsWith("/admin ");
    }

    private void processDeleteCommand(long chatId) {
        Optional<User> userOpt = userService.findUserByChatId(chatId);
        User user = userOpt.get();
        userService.deleteUser(user);
    }

    private boolean isDeleteCommand(String text) {
        return text.startsWith("/delete me");
    }

    private void sendMessage(long chatId, String message) {
        var msg = new SendMessage();
        msg.setText(message);
        msg.setChatId(chatId);

        try {
            execute(msg);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    public void sendPhotoToUser(Long chatId) {
        // Определяем путь к файлу с помощью относительного пути
        File photo = new File("./bot.jpeg");

        SendPhoto message = new SendPhoto();
        message.setChatId(chatId);
        message.setPhoto(new InputFile(photo));

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    @Override
    public String getBotUsername() {
        return botCredentials.getBotName();
    }

//    private void sendImage(long chatId) {
//        var img = new SendPhoto();
//        File photo
//        img.setPhoto("/bot.jpg");
//        img.setChatId(chatId);
//
//        try {
//            execute(img);
//        } catch (TelegramApiException e) {
//            e.printStackTrace();
//        }
//    }
}
