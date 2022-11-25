package com.coursework.telecombot.service;

import java.sql.Timestamp;
import java.util.ArrayList;

import java.util.List;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.coursework.telecombot.config.BotConfig;
import com.coursework.telecombot.model.Order;
import com.coursework.telecombot.model.OrderRepository;
import com.coursework.telecombot.model.User;
import com.coursework.telecombot.model.UserRepository;
import com.coursework.telecombot.service.impl.OrderServiceImpl;
import com.coursework.telecombot.service.impl.UserServiceImpl;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {
   

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserServiceImpl userServiceImpl;

    @Autowired 
    private OrderRepository orderRepository;

    @Autowired
    private OrderServiceImpl orderServiceImpl;

    final BotConfig config;

    public TelegramBot(BotConfig config) {
        this.config = config;
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "Start"));

        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));

        } catch (TelegramApiException e) {
            log.error("Error setting bot command list: " + e.getMessage());
        }
    }
    
    @Override
    public void onUpdateReceived(Update update) {
        
        if(update.hasMessage() && update.getMessage().hasText() && userServiceImpl.existsUserById(update.getMessage().getChatId()) == false){
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
        

            switch (messageText) {
                case "/start":
                    registerUser(update.getMessage());
                    mainMenu(chatId);
                break;

                default:
                    sendMessage(chatId, "Sorry, command was not found");
            }
        
        }else if (update.hasMessage() && update.getMessage().hasText() && userServiceImpl.existsUserById(update.getMessage().getChatId()) == true){
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();


            switch (messageText) {
                case "/start":

                    mainMenu(update.getMessage().getChatId());
                    // deleteUser(update.getMessage());
                break;

                default:
                    sendMessage(chatId, "Sorry, command was not found");
            }

        }else if (update.hasCallbackQuery()){

            String callBackData = update.getCallbackQuery().getData();
            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            User user = userServiceImpl.getById(chatId);

            if(callBackData.equals("ABOUT_BUTTON")){
                about(chatId, messageId);
            }else if(callBackData.equals("RETURN_MAINMENU")){
                mainMenuEdit(chatId, messageId);
            }else if (callBackData.equals("SUPPORT_BUTTON")){
                support(chatId, messageId);
            }else if (callBackData.equals("FAQ_BUTTON")){
                fAQ(chatId, messageId);
            }else if (callBackData.equals("LIVE_BUTTON")){
                liveSupport(chatId, messageId);
            }else if (callBackData.equals("ACCOUNT_BUTTON")){
                accountMain(chatId, messageId);
            }else if (callBackData.equals("TARIFF_BUTTON")){
                tariff(chatId, messageId);
            }else if (callBackData.equals("TARIFF_ONE")){
                setTariff(chatId, "Тариф «Твой стартовый»", messageId);
            }else if (callBackData.equals("TARIFF_TWO")){
                setTariff(chatId, "Тариф «Твой оптимальный»", messageId);
            }else if (callBackData.equals("Тариф «Твой премьерный»")){
                setTariff(chatId, "TARIFF_THREE", messageId);
            }else if (callBackData.equals("TURNOFF_TARIFF")){
                deleteTariff(chatId, messageId);
            }else if (callBackData.equals("CHANGE_TARIFF")){
                changeTariff(chatId, messageId);
            }else if (callBackData.equals("USEDATA_BUTTON")){
                userdataMain(chatId, messageId);
            }else if (callBackData.equals("BACK_USERDATA")){
                userdataMain(chatId, messageId);
            }else if (callBackData.equals("VIEV_USERDATA")){
                userdataViev(chatId, messageId);
            }else if(callBackData.equals("DELETE_USERDATA")){
                userdataDelete(chatId, messageId);
            }else if(callBackData.equals("USERDATA_DELETE")){
                deleteUser(chatId);
            }else if (callBackData.equals("BALANCE_BUTTON")){
                balanceMain(chatId, messageId);
            }else if (callBackData.equals("PAYMENT_METHOD")){
                paymentMethod(chatId, messageId);
            }else if (callBackData.equals("ENTER_AMOUNT")){
                enterAmount(chatId, messageId);
            }else if (callBackData.equals("100_AMOUNT")){
                replenishBalance(chatId, messageId, 100);
            }else if (callBackData.equals("500_AMOUNT")){
                replenishBalance(chatId, messageId, 500);
            }else if (callBackData.equals("1000_AMOUNT")){
                replenishBalance(chatId, messageId, 1000);
            }else if (callBackData.equals("HISTORY_ORDER")){
                historyOrder(chatId, messageId);
            }else if (callBackData.equals("ANALOG_FAQ")){
                fAQAnalog(chatId, messageId);
            }else if(callBackData.equals("CIFRA_FAQ")){
                fAQCifra(chatId, messageId);
            }
                
        }
    }

    public void replenishBalance(long chatId, long messageId, Integer amount){
        User user = userServiceImpl.getById(chatId);
        if (user.getBalance() == 0){
            user.setBalance(amount);
        }else if(user.getBalance() != 0){
            Integer newBalance = user.getBalance()+amount;
            user.setBalance(newBalance);
        }

        userServiceImpl.editUser(user);

        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatId);
        editMessageText.setMessageId((int)messageId);
        editMessageText.setText("Ваш баланс был успешно пополнен");

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();
        inlineKeyboardButton1.setText("Посмотреть баланс");
        inlineKeyboardButton1.setCallbackData("BALANCE_BUTTON");
        inlineKeyboardButton2.setText("Вернуться в главное меню");
        inlineKeyboardButton2.setCallbackData("RETURN_MAINMENU");
        List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
        keyboardButtonsRow1.add(inlineKeyboardButton1);
        keyboardButtonsRow1.add(inlineKeyboardButton2);
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardButtonsRow1);
        inlineKeyboardMarkup.setKeyboard(rowList);
        editMessageText.setReplyMarkup(inlineKeyboardMarkup);

        try {
            executeEditMessage(editMessageText);
        } catch (Exception e) {
            log.error("Error liveSupport inline keyboard: " + e.getMessage());
        }
    }

    private void enterAmount (long chatId, long messageId){
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatId);
        editMessageText.setMessageId((int)messageId);
        editMessageText.setText("Выберите сумму : ");

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton3 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton4 = new InlineKeyboardButton();
        inlineKeyboardButton1.setText("100");
        inlineKeyboardButton1.setCallbackData("100_AMOUNT");
        inlineKeyboardButton2.setText("500");
        inlineKeyboardButton2.setCallbackData("500_AMOUNT");
        inlineKeyboardButton3.setText("1000");
        inlineKeyboardButton3.setCallbackData("1000_AMOUNT");
        inlineKeyboardButton4.setText("Вернутся в главное меню");
        inlineKeyboardButton4.setCallbackData("RETURN_MAINMENU");
        List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
        List<InlineKeyboardButton> keyboardButtonsRow2 = new ArrayList<>();
        keyboardButtonsRow1.add(inlineKeyboardButton1);
        keyboardButtonsRow1.add(inlineKeyboardButton2);
        keyboardButtonsRow2.add(inlineKeyboardButton3);
        keyboardButtonsRow2.add(inlineKeyboardButton4);
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardButtonsRow1);
        rowList.add(keyboardButtonsRow2);
        inlineKeyboardMarkup.setKeyboard(rowList);
        editMessageText.setReplyMarkup(inlineKeyboardMarkup);
    

        try {
            execute(editMessageText);
        } catch (TelegramApiException e) {
            log.error("Error MAIN MENU inline keyboard: " + e.getMessage());
        }
    } 

    private void paymentMethod(long chatId, long messageId){
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatId);
        editMessageText.setMessageId((int)messageId);
        editMessageText.setText("Способ оплаты: ");

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton3 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton4 = new InlineKeyboardButton();
        inlineKeyboardButton1.setText("QIWI");
        inlineKeyboardButton1.setCallbackData("ENTER_AMOUNT");
        inlineKeyboardButton2.setText("Visa/MasterCard/Мир");
        inlineKeyboardButton2.setCallbackData("ENTER_AMOUNT");
        inlineKeyboardButton3.setText("Юmoney");
        inlineKeyboardButton3.setCallbackData("ENTER_AMOUNT");
        inlineKeyboardButton4.setText("Вернутся в главное меню");
        inlineKeyboardButton4.setCallbackData("RETURN_MAINMENU");
        List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
        List<InlineKeyboardButton> keyboardButtonsRow2 = new ArrayList<>();
        keyboardButtonsRow1.add(inlineKeyboardButton1);
        keyboardButtonsRow1.add(inlineKeyboardButton2);
        keyboardButtonsRow2.add(inlineKeyboardButton3);
        keyboardButtonsRow2.add(inlineKeyboardButton4);
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardButtonsRow1);
        rowList.add(keyboardButtonsRow2);
        inlineKeyboardMarkup.setKeyboard(rowList);
        editMessageText.setReplyMarkup(inlineKeyboardMarkup);
    

        try {
            execute(editMessageText);
        } catch (TelegramApiException e) {
            log.error("Error MAIN MENU inline keyboard: " + e.getMessage());
        }
    }

    private void balanceMain(long chatId, long messageId){
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatId);
        editMessageText.setMessageId((int)messageId);
        User user = userServiceImpl.getById(chatId);
        editMessageText.setText("Ваш баланс: " + user.getBalance().toString() + "$");

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();
        inlineKeyboardButton1.setText("Пополнить счет");
        inlineKeyboardButton1.setCallbackData("PAYMENT_METHOD");
        inlineKeyboardButton2.setText("Вернутся в главное меню");
        inlineKeyboardButton2.setCallbackData("RETURN_MAINMENU");
        List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
        keyboardButtonsRow1.add(inlineKeyboardButton1);
        keyboardButtonsRow1.add(inlineKeyboardButton2);
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardButtonsRow1);
        inlineKeyboardMarkup.setKeyboard(rowList);
        editMessageText.setReplyMarkup(inlineKeyboardMarkup);

        try {
            executeEditMessage(editMessageText);
        } catch (Exception e) {
            log.error("Error liveSupport inline keyboard: " + e.getMessage());
        }

    }



    private void userdataDelete(long chatId, long messageId){
        User user = userServiceImpl.getById(chatId);
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatId);
        editMessageText.setMessageId((int)messageId);
        editMessageText.setText("Вы уверены, что хотите стереть свои данные? \n Для повторной регистрации введите \start");

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();
        inlineKeyboardButton1.setText("Удалить");
        inlineKeyboardButton1.setCallbackData("USERDATA_DELETE");
        inlineKeyboardButton2.setText("Назад");
        inlineKeyboardButton2.setCallbackData("BACK_USERDATA");
        List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
        keyboardButtonsRow1.add(inlineKeyboardButton1);
        keyboardButtonsRow1.add(inlineKeyboardButton2);
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardButtonsRow1);
        inlineKeyboardMarkup.setKeyboard(rowList);
        editMessageText.setReplyMarkup(inlineKeyboardMarkup);

        try {
            executeEditMessage(editMessageText);
        } catch (Exception e) {
            log.error("Error liveSupport inline keyboard: " + e.getMessage());
        }
       

    }

    private void userdataViev(long chatId, long messageId){
        User user = userServiceImpl.getById(chatId);
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatId);
        editMessageText.setMessageId((int)messageId);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Имя: ").append(user.getFirstname()).append("\n")
        .append("Фамилия: ").append(user.getLastname()).append("\n")
        .append("Юзернейм: ").append(user.getUsername()).append("\n")
        .append("Дата регистрации: ").append(user.getDataregister().toString()).append("\n")
        .append("Ваш тариф: ").append(user.getTariff()).append("\n")
        .append("Ваш баланс: ").append(user.getBalance()).append("\n");

        String userData = stringBuilder.toString();

        editMessageText.setText(userData);

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();
        inlineKeyboardButton1.setText("Назад");
        inlineKeyboardButton1.setCallbackData("BACK_USERDATA");
        inlineKeyboardButton2.setText("Вернутся в главное меню");
        inlineKeyboardButton2.setCallbackData("RETURN_MAINMENU");
        List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
        keyboardButtonsRow1.add(inlineKeyboardButton1);
        keyboardButtonsRow1.add(inlineKeyboardButton2);
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardButtonsRow1);
        inlineKeyboardMarkup.setKeyboard(rowList);
        editMessageText.setReplyMarkup(inlineKeyboardMarkup);

        try {
            executeEditMessage(editMessageText);
        } catch (Exception e) {
            log.error("Error liveSupport inline keyboard: " + e.getMessage());
        }


    }

    private void userdataMain(long chatId, long messageId){
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatId);
        editMessageText.setMessageId((int)messageId);
        editMessageText.setText("Данные пользователя");

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton3 = new InlineKeyboardButton();

        inlineKeyboardButton1.setText("Посмотреть данные ");
        inlineKeyboardButton1.setCallbackData("VIEV_USERDATA");
        inlineKeyboardButton2.setText("Удалить данные");
        inlineKeyboardButton2.setCallbackData("DELETE_USERDATA");
        inlineKeyboardButton3.setText("Вернутся в главное меню");
        inlineKeyboardButton3.setCallbackData("RETURN_MAINMENU");
        List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
        List<InlineKeyboardButton> keyboardButtonsRow2 = new ArrayList<>();
        keyboardButtonsRow1.add(inlineKeyboardButton1);
        keyboardButtonsRow1.add(inlineKeyboardButton2);
        keyboardButtonsRow2.add(inlineKeyboardButton3);
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardButtonsRow1);
        rowList.add(keyboardButtonsRow2);
        inlineKeyboardMarkup.setKeyboard(rowList);
        editMessageText.setReplyMarkup(inlineKeyboardMarkup);
    

        try {
            execute(editMessageText);
        } catch (TelegramApiException e) {
            log.error("Error MAIN MENU inline keyboard: " + e.getMessage());
        }

    }

    private void changeTariff(long chatId, long messageId){
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatId);
        editMessageText.setMessageId((int)messageId);

        User user = userServiceImpl.getById(chatId);

        editMessageText.setText("Вы используете: "+ user.getTariff().toString() + ". \n \n Выберите новый тариф: \n \n • Тариф «Твой стартовый» \n Пакет основных телеканалов для всей семьи \n 113 телеканалов \n 320 рублей в месяц \n \n •	Тариф «Твой оптимальный» \n Включает «Твой стартовый» + спортивные, детские и музыкальные телеканалы \n 141 телеканал \n 420 рублей в месяц \n \n  • Тариф «Твой премьерный» \n Пакет для любителей качественных сериалов и кино \n + Пакеты каналов Amedia Premium и Viasat Премиум HD\n + Подписка на видео Amedia Premium и TV1000PLAY\n + 20 федеральных каналов\n 620 рублей в месяц ");

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton3 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton4 = new InlineKeyboardButton();
        inlineKeyboardButton1.setText("Тариф «Твой стартовый»");
        inlineKeyboardButton1.setCallbackData("TARIFF_ONE");
        inlineKeyboardButton2.setText("Тариф «Твой оптимальный»");
        inlineKeyboardButton2.setCallbackData("TARIFF_TWO");
        inlineKeyboardButton3.setText("Тариф «Твой премьерный»");
        inlineKeyboardButton3.setCallbackData("TARIFF_THREE");
        inlineKeyboardButton4.setText("Вернутся в главное меню");
        inlineKeyboardButton4.setCallbackData("RETURN_MAINMENU");
        List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
        List<InlineKeyboardButton> keyboardButtonsRow2 = new ArrayList<>();
        keyboardButtonsRow1.add(inlineKeyboardButton1);
        keyboardButtonsRow1.add(inlineKeyboardButton2);
        keyboardButtonsRow2.add(inlineKeyboardButton3);
        keyboardButtonsRow2.add(inlineKeyboardButton4);
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardButtonsRow1);
        rowList.add(keyboardButtonsRow2);
        inlineKeyboardMarkup.setKeyboard(rowList);
        editMessageText.setReplyMarkup(inlineKeyboardMarkup);
    

        try {
            execute(editMessageText);
        } catch (TelegramApiException e) {
            log.error("Error MAIN MENU inline keyboard: " + e.getMessage());
        }
    }

    private void deleteTariff(long chatId, long messageId){
        User user = userServiceImpl.getById(chatId);
        user.setTariff("none");
        userServiceImpl.editUser(user);

        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatId);
        editMessageText.setMessageId((int)messageId);
        editMessageText.setText("Вы успешно отключили тариф");

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        inlineKeyboardButton1.setText("Вернуться в главное меню");
        inlineKeyboardButton1.setCallbackData("RETURN_MAINMENU");
        List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
        keyboardButtonsRow1.add(inlineKeyboardButton1);
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardButtonsRow1);
        inlineKeyboardMarkup.setKeyboard(rowList);
        editMessageText.setReplyMarkup(inlineKeyboardMarkup);

        try {
            executeEditMessage(editMessageText);
        } catch (Exception e) {
            log.error("Error liveSupport inline keyboard: " + e.getMessage());
        }

    }

    private void setTariff(long chatId, String tariffName, long messageId){
        User user = userServiceImpl.getById(chatId);
        user.setTariff(tariffName);
        userServiceImpl.editUser(user);

        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatId);
        editMessageText.setMessageId((int)messageId);
        editMessageText.setText("Вы используете: " + tariffName);

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        inlineKeyboardButton1.setText("Вернутся в главное меню");
        inlineKeyboardButton1.setCallbackData("RETURN_MAINMENU");
        List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
        keyboardButtonsRow1.add(inlineKeyboardButton1);
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardButtonsRow1);
        inlineKeyboardMarkup.setKeyboard(rowList);
        editMessageText.setReplyMarkup(inlineKeyboardMarkup);

        try {
            executeEditMessage(editMessageText);
        } catch (Exception e) {
            log.error("Error liveSupport inline keyboard: " + e.getMessage());
        }
    }

    private void tariff(long chatId, long messageId){
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatId);
        editMessageText.setMessageId((int)messageId);

        User user = userServiceImpl.getById(chatId);

        if(user.getTariff().equals("none")){
            editMessageText.setText("У Вас не подключен тариф. \n \n Выберите новый тариф: \n • Тариф «Твой стартовый» \n Пакет основных телеканалов для всей семьи \n 113 телеканалов \n 320 рублей в месяц \n \n •	Тариф «Твой оптимальный» \n Включает «Твой стартовый» + спортивные, детские и музыкальные телеканалы \n 141 телеканал \n 420 рублей в месяц \n \n  • Тариф «Твой премьерный» \n Пакет для любителей качественных сериалов и кино \n + Пакеты каналов Amedia Premium и Viasat Премиум HD\n + Подписка на видео Amedia Premium и TV1000PLAY\n + 20 федеральных каналов\n 620 рублей в месяц ");

            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
            InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
            InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();
            InlineKeyboardButton inlineKeyboardButton3 = new InlineKeyboardButton();
            InlineKeyboardButton inlineKeyboardButton4 = new InlineKeyboardButton();
            inlineKeyboardButton1.setText("Тариф «Твой стартовый»");
            inlineKeyboardButton1.setCallbackData("TARIFF_ONE");
            inlineKeyboardButton2.setText("Тариф «Твой оптимальный»");
            inlineKeyboardButton2.setCallbackData("TARIFF_TWO");
            inlineKeyboardButton3.setText("Тариф «Твой премьерный»");
            inlineKeyboardButton3.setCallbackData("TARIFF_THREE");
            inlineKeyboardButton4.setText("Вернутся в главное меню");
            inlineKeyboardButton4.setCallbackData("RETURN_MAINMENU");
            List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
            List<InlineKeyboardButton> keyboardButtonsRow2 = new ArrayList<>();
            keyboardButtonsRow1.add(inlineKeyboardButton1);
            keyboardButtonsRow1.add(inlineKeyboardButton2);
            keyboardButtonsRow2.add(inlineKeyboardButton3);
            keyboardButtonsRow2.add(inlineKeyboardButton4);
            List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
            rowList.add(keyboardButtonsRow1);
            rowList.add(keyboardButtonsRow2);
            inlineKeyboardMarkup.setKeyboard(rowList);
            editMessageText.setReplyMarkup(inlineKeyboardMarkup);
        

            try {
                execute(editMessageText);
            } catch (TelegramApiException e) {
                log.error("Error MAIN MENU inline keyboard: " + e.getMessage());
            }

        }else if (!user.getTariff().equals("none")){
            editMessageText.setText("Вы используете: " + user.getTariff().toString());

            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
            InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
            InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();
            InlineKeyboardButton inlineKeyboardButton3 = new InlineKeyboardButton();
            inlineKeyboardButton1.setText("Сменить тариф");
            inlineKeyboardButton1.setCallbackData("CHANGE_TARIFF");
            inlineKeyboardButton2.setText("Отключить тариф");
            inlineKeyboardButton2.setCallbackData("TURNOFF_TARIFF");
            inlineKeyboardButton3.setText("Вернутся в главное меню");
            inlineKeyboardButton3.setCallbackData("RETURN_MAINMENU");
            List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
            List<InlineKeyboardButton> keyboardButtonsRow2 = new ArrayList<>();
            keyboardButtonsRow1.add(inlineKeyboardButton1);
            keyboardButtonsRow2.add(inlineKeyboardButton2);
            keyboardButtonsRow2.add(inlineKeyboardButton3);
            List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
            rowList.add(keyboardButtonsRow1);
            rowList.add(keyboardButtonsRow2);
            inlineKeyboardMarkup.setKeyboard(rowList);
            editMessageText.setReplyMarkup(inlineKeyboardMarkup);
        
    
            try {
                execute(editMessageText);
            } catch (TelegramApiException e) {
                log.error("Error MAIN MENU inline keyboard: " + e.getMessage());
            }
        }

    }

    private void accountMain (long chatId, long messageId){
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatId);
        editMessageText.setMessageId((int)messageId);
        editMessageText.setText("Мой аккаунт");

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton3 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton4 = new InlineKeyboardButton();
        inlineKeyboardButton1.setText("Тарифы");
        inlineKeyboardButton1.setCallbackData("TARIFF_BUTTON");
        inlineKeyboardButton2.setText("Баланс");
        inlineKeyboardButton2.setCallbackData("BALANCE_BUTTON");
        inlineKeyboardButton3.setText("Данные пользователя");
        inlineKeyboardButton3.setCallbackData("USEDATA_BUTTON");
        inlineKeyboardButton4.setText("Вернутся в главное меню");
        inlineKeyboardButton4.setCallbackData("RETURN_MAINMENU");
        List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
        List<InlineKeyboardButton> keyboardButtonsRow2 = new ArrayList<>();
        keyboardButtonsRow1.add(inlineKeyboardButton1);
        keyboardButtonsRow1.add(inlineKeyboardButton2);
        keyboardButtonsRow2.add(inlineKeyboardButton3);
        keyboardButtonsRow2.add(inlineKeyboardButton4);
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardButtonsRow1);
        rowList.add(keyboardButtonsRow2);
        inlineKeyboardMarkup.setKeyboard(rowList);
        editMessageText.setReplyMarkup(inlineKeyboardMarkup);
    

        try {
            execute(editMessageText);
        } catch (TelegramApiException e) {
            log.error("Error MAIN MENU inline keyboard: " + e.getMessage());
        }
    }

    private void liveSupport(long chatId, long messageId){
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatId);
        editMessageText.setMessageId((int)messageId);
        // editMessageText.setText("Связь с оператором. Номер вашей заявки 5874583. ");
        int random = getRandomNumber(1000, 9999);
        newOrder(chatId, random);
        editMessageText.setText("Cвязь с оператором. Номер вашей заявки: " + random);

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        inlineKeyboardButton1.setText("Вернутся в главное меню");
        inlineKeyboardButton1.setCallbackData("RETURN_MAINMENU");
        List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
        keyboardButtonsRow1.add(inlineKeyboardButton1);
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardButtonsRow1);
        inlineKeyboardMarkup.setKeyboard(rowList);
        editMessageText.setReplyMarkup(inlineKeyboardMarkup);

        try {
            executeEditMessage(editMessageText);
        } catch (Exception e) {
            log.error("Error liveSupport inline keyboard: " + e.getMessage());
        }
    }

    private void fAQ(long chatId, long messageId){
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatId);
        editMessageText.setMessageId((int)messageId);
        editMessageText.setText("Выберите пункт меню:");

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton3 = new InlineKeyboardButton();
        inlineKeyboardButton1.setText("Аналоговое ТВ");
        inlineKeyboardButton1.setCallbackData("ANALOG_FAQ");
        inlineKeyboardButton2.setText("Цифровое ТВ");
        inlineKeyboardButton2.setCallbackData("CIFRA_FAQ");
        inlineKeyboardButton3.setText("Вернутся в главное меню");
        inlineKeyboardButton3.setCallbackData("RETURN_MAINMENU");
        List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
        List<InlineKeyboardButton> keyboardButtonsRow2 = new ArrayList<>();
        keyboardButtonsRow1.add(inlineKeyboardButton1);
        keyboardButtonsRow1.add(inlineKeyboardButton2);
        keyboardButtonsRow2.add(inlineKeyboardButton3);
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardButtonsRow1);
        rowList.add(keyboardButtonsRow2);
        inlineKeyboardMarkup.setKeyboard(rowList);
        editMessageText.setReplyMarkup(inlineKeyboardMarkup);
    

        try {
            executeEditMessage(editMessageText);
        } catch (Exception e) {
            log.error("Error liveSupport inline keyboard: " + e.getMessage());
        }
    }

    private void fAQCifra(long chatId, long messageId){
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatId);
        editMessageText.setMessageId((int)messageId);
        editMessageText.setText("*• Декодер не реагирует на пульт.*\n Перезагрузите декодер, если это не поможет, то замените батарейки в пульте. \n \n *•На некоторых каналах цифрового ТВ выдает сообщение «Техническая проблема»*\n Перезагрузите декодер. Если проблема останется, запишите каналы, которые не работают, и обратитесь в службу технической поддержки по телефону 595-81-22.\n \n *• Декодер выдает сообщение «Эта карта не является картой для просмотра»*\nВытащите карту из декодера и аккуратно протрите чип ластиком, после этого вставьте обратно. Если это не помогло, то обратитесь в службу технической поддержки по телефону 595-81-22.\n \n *• Один из телевизоров в квартире плохо показывает.*\n Проверьте контакты у делителя и около телевизора. При возможности поменяйте телевизоры местами и проверьте еще раз. Возможно, что проблема в телевизоре. Если же после смены телевизора, проблема осталась и на другом, то вероятно неисправность где-то с разводкой по квартире от делителя до телевизора.\n \n *• Изображения - нет, звук есть.*\n 1) Проверьте правильность и качество соединения кабеля (тюльпанов при подключении по низкой частоте); \n 2) Проверьте правильность выбора видеовхода телевизора (AV 1, AV 2, AV 3).");
        editMessageText.setParseMode("markdown");
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        inlineKeyboardButton1.setText("Вернутся в главное меню");
        inlineKeyboardButton1.setCallbackData("FAQ_BUTTON");
        List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
        keyboardButtonsRow1.add(inlineKeyboardButton1);
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardButtonsRow1);
        inlineKeyboardMarkup.setKeyboard(rowList);
        editMessageText.setReplyMarkup(inlineKeyboardMarkup);

        try {
            executeEditMessage(editMessageText);
        } catch (Exception e) {
            log.error("Error FAQ inline keyboard: " + e.getMessage());
        }
    }

    private void fAQAnalog(long chatId, long messageId){
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatId);
        editMessageText.setMessageId((int)messageId);
        editMessageText.setText("*• Выравнивание звука на каналах.*\nПоскольку единого стандарта на звук, к сожалению, не существует, каждая передающая станция (каждый канал) устанавливает свой уровень звука. Кроме того, на разных передачах одного канала уровень звука тоже может быть разным. Мы ретранслируем каналы, и поэтому не можем изменить их параметры, в том числе и уровень звука. \n \n *• О помехах.* \n Распространение радиоволн подчиняется законам физики. Различные преграды ослабляют сигнал. Для телевизионного сигнала существенными преградами являются грозовые облака, снег, сильный дождь. Кроме того, когда спутник, Солнце и принимающая антенна находятся на одной линии, телевизионный сигнал ухудшается. При ослаблении цифрового сигнала картинка рассыпается на квадратики или замирает и появляются сбои звука. При ослаблении аналогового сигнала на изображении появляется цветная рябь. Наиболее частые причины ухудшения аналогового сигнала: \n 1) по ГОСТу на каждую квартиру подается сигал, уровень которого достаточен для работы 2-х телевизоров. Чем больше телевизоров, тем больше помех. (Если в квартире работает только 1 телевизор, но телевизионный кабель разветвлен на 20 телевизионных розеток, то качество будет плохим, т.к. сигнал гасится на разветвителях); \n 2) плохой контакт антенного штекера в телевизоре или в разветвителях; \n 3) тонкий или старый антенный кабель. \n \n *• Шум вместо звука*\n Изменить звуковую частоту в настройках телевизора.\n\n *• Отсутствует звук.*\n1) Звук отключен (нажать кнопку MUTE);\n2) Звук выведен до нуля;\n3) Проверьте правильность и качество соединения кабеля (тюльпанов при подключении по низкой частоте).");
        editMessageText.setParseMode("markdown");
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        inlineKeyboardButton1.setText("Вернутся в главное меню");
        inlineKeyboardButton1.setCallbackData("FAQ_BUTTON");
        List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
        keyboardButtonsRow1.add(inlineKeyboardButton1);
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardButtonsRow1);
        inlineKeyboardMarkup.setKeyboard(rowList);
        editMessageText.setReplyMarkup(inlineKeyboardMarkup);

        try {
            executeEditMessage(editMessageText);
        } catch (Exception e) {
            log.error("Error FAQ inline keyboard: " + e.getMessage());
        }
    }

    private void support(long chatId, long messageId){
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatId);
        editMessageText.setMessageId((int)messageId);
        editMessageText.setText("Тех.поддержка");

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton3 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton4 = new InlineKeyboardButton();
        inlineKeyboardButton1.setText("Часто задаваемые вопросы");
        inlineKeyboardButton1.setCallbackData("FAQ_BUTTON");
        inlineKeyboardButton2.setText("Связатся с оператором");
        inlineKeyboardButton2.setCallbackData("LIVE_BUTTON");
        inlineKeyboardButton3.setText("История заявок");
        inlineKeyboardButton3.setCallbackData("HISTORY_ORDER");
        inlineKeyboardButton4.setText("Вернутся в главное меню");
        inlineKeyboardButton4.setCallbackData("RETURN_MAINMENU");
        List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
        List<InlineKeyboardButton> keyboardButtonsRow2 = new ArrayList<>();
        keyboardButtonsRow1.add(inlineKeyboardButton1);
        keyboardButtonsRow1.add(inlineKeyboardButton2);
        keyboardButtonsRow2.add(inlineKeyboardButton3);
        keyboardButtonsRow2.add(inlineKeyboardButton4);
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardButtonsRow1);
        rowList.add(keyboardButtonsRow2);
        inlineKeyboardMarkup.setKeyboard(rowList);
        editMessageText.setReplyMarkup(inlineKeyboardMarkup);
    

        try {
            execute(editMessageText);
        } catch (TelegramApiException e) {
            log.error("Error MAIN MENU inline keyboard: " + e.getMessage());
        }
        

    }

    private void about(long chatId, long messageId){
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatId);
        editMessageText.setMessageId((int)messageId);
        editMessageText.setText("О компании. \n ЦифроТВ – крупный российский оператор современных услуг связи и системный интегратор, работающий на рынке более 13 лет! Наш продукт – комплексные решения в области телекоммуникаций, цифровых технологий и системной интеграции. Кроме того, наши эксперты проводят проектно-изыскательские, строительно-монтажные, пуско-наладочные работы, а также обеспечивают дальнейшее сопровождение и техническое обслуживание разработанных и установленных систем. ы постоянно анализируем мировые технологии и предлагаем лучшие решения под индивидуальные потребности и возможности Клиента! Мы делаем новые технологии доступными производственным и коммерческим предприятиям, а также культурным и государственно-частным компаниям. Нашими услугами пользуется уже более 80 000 Клиентов по всей России! "); 

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        inlineKeyboardButton1.setText("Вернутся в главное меню");
        inlineKeyboardButton1.setCallbackData("RETURN_MAINMENU");
        List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
        keyboardButtonsRow1.add(inlineKeyboardButton1);
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardButtonsRow1);
        inlineKeyboardMarkup.setKeyboard(rowList);
        editMessageText.setReplyMarkup(inlineKeyboardMarkup);

        try {
            executeEditMessage(editMessageText);
        } catch (Exception e) {
            log.error("Error ABOUT inline keyboard: " + e.getMessage());
        }

    }

    private void mainMenu(long chatId) {


        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Главное меню");

        // EditMessageText editMessageText = new EditMessageText();
        // editMessageText.setChatId(chatId);
        // editMessageText.setMessageId((int)messageId);
        // editMessageText.setText("Main Menu");

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton3 = new InlineKeyboardButton();
        inlineKeyboardButton1.setText("Аккаунт");
        inlineKeyboardButton1.setCallbackData("ACCOUNT_BUTTON");
        inlineKeyboardButton2.setText("Тех.поддержка");
        inlineKeyboardButton2.setCallbackData("SUPPORT_BUTTON");
        inlineKeyboardButton3.setText("О компании");
        inlineKeyboardButton3.setCallbackData("ABOUT_BUTTON");
        List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
        List<InlineKeyboardButton> keyboardButtonsRow2 = new ArrayList<>();
        keyboardButtonsRow1.add(inlineKeyboardButton1);
        keyboardButtonsRow2.add(inlineKeyboardButton2);
        keyboardButtonsRow2.add(inlineKeyboardButton3);
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardButtonsRow1);
        rowList.add(keyboardButtonsRow2);
        inlineKeyboardMarkup.setKeyboard(rowList);
        message.setReplyMarkup(inlineKeyboardMarkup);
    

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error MAIN MENU inline keyboard: " + e.getMessage());
        }


    }

    private void mainMenuEdit(long chatId, long messageId) {


        // SendMessage message = new SendMessage();
        // message.setChatId(chatId);
        // message.setText("Main Menu");

        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatId);
        editMessageText.setMessageId((int)messageId);
        editMessageText.setText("Главное меню");

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton3 = new InlineKeyboardButton();
        inlineKeyboardButton1.setText("Аккаунт");
        inlineKeyboardButton1.setCallbackData("ACCOUNT_BUTTON");
        inlineKeyboardButton2.setText("Тех.поддержка");
        inlineKeyboardButton2.setCallbackData("SUPPORT_BUTTON");
        inlineKeyboardButton3.setText("О компании");
        inlineKeyboardButton3.setCallbackData("ABOUT_BUTTON");
        List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
        List<InlineKeyboardButton> keyboardButtonsRow2 = new ArrayList<>();
        keyboardButtonsRow1.add(inlineKeyboardButton1);
        keyboardButtonsRow2.add(inlineKeyboardButton2);
        keyboardButtonsRow2.add(inlineKeyboardButton3);
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardButtonsRow1);
        rowList.add(keyboardButtonsRow2);
        inlineKeyboardMarkup.setKeyboard(rowList);
        editMessageText.setReplyMarkup(inlineKeyboardMarkup);
    

        try {
            execute(editMessageText);
        } catch (TelegramApiException e) {
            log.error("Error MAIN MENU inline keyboard: " + e.getMessage());
        }


    }

    private void registerUser(Message message) {
        if(userRepository.findById(message.getChatId()).isEmpty()){
            
            var chatId = message.getChatId();
            var chat = message.getChat();
            
            User user = new User();


            user.setChatId(chatId);
            user.setFirstname(chat.getFirstName());
            user.setLastname(chat.getLastName());
            user.setUsername(chat.getUserName());
            user.setDataregister(new Timestamp(System.currentTimeMillis()));
            user.setBalance(0);
            user.setTariff("none");
            

            try {
                userServiceImpl.addUser(user);
            } catch (Exception e) {
                log.error("Save new user error: ", e.getMessage());
            }
            

            sendMessage(chatId, "Вы успешно зарегистрированны!");
           
        }
    }

    private void historyOrder (long chatId, long messageId){
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatId);
        editMessageText.setMessageId((int)messageId);
        StringBuffer orderStringBuffer = new StringBuffer();

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        inlineKeyboardButton1.setText("Вернутся в главное меню");
        inlineKeyboardButton1.setCallbackData("RETURN_MAINMENU");
        List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
        keyboardButtonsRow1.add(inlineKeyboardButton1);
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardButtonsRow1);
        inlineKeyboardMarkup.setKeyboard(rowList);
        editMessageText.setReplyMarkup(inlineKeyboardMarkup);

        orderStringBuffer.append("Ваша история заявок: ").append("\n");
        List <Order> orderList = orderServiceImpl.getAll();
        Order[] orderMass = orderList.toArray(new Order[orderList.size()]);
        for (Order order : orderMass){
            if (order.getChatId() == chatId){
                orderStringBuffer.append(order.toString()).append("\n");
            }
        }
        editMessageText.setText(orderStringBuffer.toString());
        try {
            execute(editMessageText);
        } catch (TelegramApiException e) {
            log.error("Error EXECUTE: " + e.getMessage());
        }




    }

    private void newOrder(long chatId, int  number) {
        
        Order order = new Order();
        order.setNumber(number);
        order.setOrderdate(new Timestamp(System.currentTimeMillis()));
        order.setChatId(chatId);

        orderServiceImpl.addOrder(order);

    }

    public int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

    private void deleteUser(Message message){
        userServiceImpl.deleteUser(message.getChatId());
    }

    private void deleteUser(long chatId){
        userServiceImpl.deleteUser(chatId);
    }

    private void sendMessage(long chatId, String textToSend){
        SendMessage messageText = new SendMessage();

        messageText.setChatId(String.valueOf(chatId));
        messageText.setText(textToSend);

        executeMessage(messageText);
    }

    private void sendEditMessage(long chatId, String textToSend, long messageId){
        EditMessageText editMessageText = new EditMessageText();    
        editMessageText.setChatId(chatId);
        editMessageText.setText(textToSend);
        editMessageText.setMessageId((int)messageId);

        try {
            executeEditMessage(editMessageText);
        } catch (Exception e) {
            log.error("Error EXECUTE EDIT MESSAGE: " + e.getMessage());
        }
    }

    private void executeMessage(SendMessage message){
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error EXECUTE: " + e.getMessage());
        }
    }

    private void executeEditMessage(EditMessageText editMessage){
        try {
            execute(editMessage);
        } catch (TelegramApiException e) {
            log.error("Error EXECUTE EDIT MESSAGE: " + e.getMessage());
        }
    } 

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }
    
}
