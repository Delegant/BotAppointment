package org.telegram;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.telegram.Config.*;


public class Bot extends TelegramLongPollingBot {
    private static final Logger LOGGER = Logger.getLogger(Bot.class.getName());
    private static final ResourceBundle MY_BUNDLE = ResourceBundle.getBundle("labels");
    private final Map<Long, String> currentMenuBase = new HashMap<>();

    public String getBotUsername() {
        return CURRENT_PROPERTIES.getProperty("filesUser");
    }

    public String getBotToken() {
        return Config.CURRENT_PROPERTIES.getProperty("filesToken");
    }

    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage()) {
                LOGGER.log(Level.INFO, "Have update - new message");
                handleIncomingUpdate(update);
            }
            if (update.hasCallbackQuery()){
                LOGGER.log(Level.INFO, "Have update - new callback query");
                handleCallbackQuery(update);
            }
        } catch (TelegramApiException e) {
            LOGGER.log(Level.WARNING, "Exception update Received", e);
            System.exit(2);
        }
        catch (MissingResourceException e) {
            LOGGER.log(Level.WARNING, "", e);
        }

    }

    private void handleIncomingUpdate(Update update) throws TelegramApiException {
        Message message = update.getMessage();
        if (message != null && message.hasText()) {
            long userId = update.getMessage().getFrom().getId();
            if (Long.toString(userId).equals(CURRENT_PROPERTIES.getProperty("adminId"))){
                adminSwitchIncomingMessage(message, userId);
            } else userSwitchIncomingMessage(message, userId);
        }
    }

    private void userSwitchIncomingMessage(Message message, long userId) throws TelegramApiException {
        if (currentMenuBase.containsKey(userId)) {
            switch (currentMenuBase.get(userId)) {
                case "USER_MAIN":
                    onUserMainMenuCommand(message);
                    break;
                case "USER_SETTINGS":
                    onUserSettingCommand(message);
                    break;
                default:
                    onUserMainMenuCommand(message);
                    break;
            }
        }
        else {
            onUserMainMenuCommand(message);
        }
    }

    private void adminSwitchIncomingMessage(Message message, long userId) throws TelegramApiException {
        if (currentMenuBase.containsKey(userId)) {
            switch (currentMenuBase.get(userId)) {
                case "ADMIN_MAIN":
                    onAdminMainMenuCommand(message);
                    break;
                case "ADMIN_SETTINGS":
                    onAdminSettingCommand(message);
                    break;
                case "ADMIN_SET_SETTING":
                    onAdminSetSettingCommand(message);
                    break;
            }
        }
        else {
            onAdminMainMenuCommand(message);
        }
    }

    private void onAdminMainMenuCommand(Message message) throws TelegramApiException {
            if (message.getText().equals(MY_BUNDLE.getString("startButton"))) {
                sendText(MY_BUNDLE.getString("startMessage"), message, getAdminMainKeyboard());
            } else if (message.getText().equals(MY_BUNDLE.getString("settingsButton"))) {
                onAdminSettingCommand(message);
            } else if (message.getText().equals(MY_BUNDLE.getString("helpButton"))) {
                sendText(MY_BUNDLE.getString("help"), message, getAdminMainKeyboard());
            } else if (message.getText().equals(MY_BUNDLE.getString("signUpButton"))) {
                sendText(MY_BUNDLE.getString("signUp"), message, getInlineKeyboardMarkup());
            } else if (message.getText().equals(MY_BUNDLE.getString("signOutButton"))) {
                sendText(MY_BUNDLE.getString("signOut"), message, getAdminMainKeyboard());
            }
    }

    private void onUserMainMenuCommand(Message message) throws TelegramApiException {
        if (message.getText().equals(MY_BUNDLE.getString("startButton"))) {
            sendText(MY_BUNDLE.getString("startMessage"), message, getAdminMainKeyboard());
        } else if (message.getText().equals(MY_BUNDLE.getString("settingsButton"))) {
            onUserSettingCommand(message);
        } else if (message.getText().equals(MY_BUNDLE.getString("helpButton"))) {
            sendText(MY_BUNDLE.getString("help"), message, getAdminMainKeyboard());
        } else if (message.getText().equals(MY_BUNDLE.getString("signUpButton"))) {
            sendText(MY_BUNDLE.getString("signUp"), message, getInlineKeyboardMarkup());
        } else if (message.getText().equals(MY_BUNDLE.getString("signOutButton"))) {
            sendText(MY_BUNDLE.getString("signOut"), message, getAdminMainKeyboard());
        }
        else if (message.getText().equals("/test2")) {
            EventMaker.createEvent();
        }
    }

    private void onAdminSettingCommand(Message message) throws TelegramApiException {
        if (message.getText().equals(MY_BUNDLE.getString("settingsButton"))) {
            currentMenuBase.put(message.getFrom().getId(), "ADMIN_SETTINGS");
            String allPropertiesAnswer = Config.sendTextProperties();
            sendText(allPropertiesAnswer, message, getAdminSettingKeyboard());
        } else if (message.getText().equals(MY_BUNDLE.getString("changeFiled"))) {

            onAdminSetSettingCommand(message);
        } else if (message.getText().equals(MY_BUNDLE.getString("mainMenu"))) {
            currentMenuBase.put(message.getFrom().getId(),"ADMIN_MAIN");
            sendText(MY_BUNDLE.getString("startMessage"), message, getAdminMainKeyboard());
        }
    }

    private void onUserSettingCommand(Message message) throws TelegramApiException {
        if (message.getText().equals(MY_BUNDLE.getString("settingsButton"))) {
            currentMenuBase.put(message.getFrom().getId(), "USER_SETTINGS");
            sendText((MY_BUNDLE.getString("setLanguage")), message, getUserSettingKeyboard());
        } else if (message.getText().equals(MY_BUNDLE.getString("rus"))) {
            currentMenuBase.put(message.getFrom().getId(),"USER_MAIN");
            sendText((MY_BUNDLE.getString("LanguageWasChanged")), message, getAdminMainKeyboard());
        } else if (message.getText().equals(MY_BUNDLE.getString("en"))) {
            currentMenuBase.put(message.getFrom().getId(),"USER_MAIN");
            sendText(MY_BUNDLE.getString("LanguageWasntChanged"), message, getAdminMainKeyboard());
        }
    }

    private void onAdminSetSettingCommand(Message message) throws TelegramApiException {
        if (message.getText().equals(MY_BUNDLE.getString("changeFiled"))){
            sendText(MY_BUNDLE.getString("setText"), message, getAdminSetSettingsKeyboard());
            currentMenuBase.put(message.getFrom().getId(),"ADMIN_SET_SETTING");
        } else if (message.getText().equals(MY_BUNDLE.getString("back"))) {
            currentMenuBase.put(message.getFrom().getId(), "ADMIN_SETTINGS");
            String allPropertiesAnswer = Config.sendTextProperties();
            sendText(allPropertiesAnswer, message, getAdminSettingKeyboard());
        } else if (message.getText().contains("=")) {
            Config.saveProperties(message.getText());
            sendText(MY_BUNDLE.getString("changesSaved"), message, getAdminSetSettingsKeyboard());
        }
    }

    private static ReplyKeyboardMarkup getAdminMainKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        keyboardFirstRow.add(new KeyboardButton(MY_BUNDLE.getString("signUpButton")));
        keyboardFirstRow.add(new KeyboardButton(MY_BUNDLE.getString("signOutButton")));

        KeyboardRow keyboardSecondRow = new KeyboardRow();
        keyboardSecondRow.add(new KeyboardButton(MY_BUNDLE.getString("settingsButton")));
        keyboardSecondRow.add(new KeyboardButton(MY_BUNDLE.getString("helpButton")));
        keyboard.add(keyboardFirstRow);
        keyboard.add(keyboardSecondRow);
        replyKeyboardMarkup.setKeyboard(keyboard);
        return replyKeyboardMarkup;
    }

    private static ReplyKeyboardMarkup getAdminSettingKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        keyboardFirstRow.add(new KeyboardButton(MY_BUNDLE.getString("changeFiled")));
        keyboardFirstRow.add(new KeyboardButton(MY_BUNDLE.getString("mainMenu")));

        keyboard.add(keyboardFirstRow);
        replyKeyboardMarkup.setKeyboard(keyboard);
        return replyKeyboardMarkup;
    }

    private static ReplyKeyboardMarkup getUserSettingKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        keyboardFirstRow.add(new KeyboardButton(MY_BUNDLE.getString("en")));
        keyboardFirstRow.add(new KeyboardButton(MY_BUNDLE.getString("rus")));

        keyboard.add(keyboardFirstRow);
        replyKeyboardMarkup.setKeyboard(keyboard);
        return replyKeyboardMarkup;
    }

    private static ReplyKeyboardMarkup getAdminSetSettingsKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        keyboardFirstRow.add(new KeyboardButton(MY_BUNDLE.getString("back")));

        keyboard.add(keyboardFirstRow);
        replyKeyboardMarkup.setKeyboard(keyboard);
        return replyKeyboardMarkup;
    }

    private static InlineKeyboardMarkup  getInlineKeyboardMarkup() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton morningButton = new InlineKeyboardButton();
        InlineKeyboardButton dayButton = new InlineKeyboardButton();
        InlineKeyboardButton eveningButton = new InlineKeyboardButton();
        morningButton.setText(MY_BUNDLE.getString("morningButton") + " " + getMorningLocalTime().toString());
        dayButton.setText(MY_BUNDLE.getString("dayButton") + " " + getDayLocalTime().toString());
        eveningButton.setText(MY_BUNDLE.getString("eveningButton") + " " + getEveningLocalTime().toString());
        morningButton.setCallbackData(getMorningLocalTime().toString());
        dayButton.setCallbackData(getDayLocalTime().toString());
        eveningButton.setCallbackData(getEveningLocalTime().toString());
        List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
        List<InlineKeyboardButton> keyboardButtonsRow2 = new ArrayList<>();
        List<InlineKeyboardButton> keyboardButtonsRow3 = new ArrayList<>();
        keyboardButtonsRow1.add(morningButton);
        keyboardButtonsRow2.add(dayButton);
        keyboardButtonsRow3.add(eveningButton);
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardButtonsRow1);
        rowList.add(keyboardButtonsRow2);
        rowList.add(keyboardButtonsRow3);
        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    private static InlineKeyboardMarkup  getResponseInlineKeyboardMarkup(List<TimeInterval> freeUnitsTimeAtWorkday) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton button1 = new InlineKeyboardButton();
        InlineKeyboardButton button2 = new InlineKeyboardButton();
        InlineKeyboardButton button3 = new InlineKeyboardButton();
        button1.setText(freeUnitsTimeAtWorkday.get(0).toString());
        button2.setText(freeUnitsTimeAtWorkday.get(1).toString());
        button3.setText(freeUnitsTimeAtWorkday.get(2).toString());
        button1.setCallbackData(freeUnitsTimeAtWorkday.get(0).toString());
        button2.setCallbackData(freeUnitsTimeAtWorkday.get(1).toString());
        button3.setCallbackData(freeUnitsTimeAtWorkday.get(2).toString());
        List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
        List<InlineKeyboardButton> keyboardButtonsRow2 = new ArrayList<>();
        List<InlineKeyboardButton> keyboardButtonsRow3 = new ArrayList<>();
        keyboardButtonsRow1.add(button1);
        keyboardButtonsRow2.add(button2);
        keyboardButtonsRow3.add(button3);
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardButtonsRow1);
        rowList.add(keyboardButtonsRow2);
        rowList.add(keyboardButtonsRow3);
        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    private void handleCallbackQuery(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        if (callbackQuery != null) {
            try {
                String[] boundTimeInterval = callbackQuery.getData().split("-");
                List<TimeInterval> freeUnitsTimeAtWorkday = EventMaker.getFreeUnitsTimeAtWorkday(new TimeInterval(boundTimeInterval [0], boundTimeInterval[1]));
                if (freeUnitsTimeAtWorkday.isEmpty()) {
                    sendText(MY_BUNDLE.getString("noFreeTime"), callbackQuery.getMessage(), getAdminMainKeyboard());
                } else {
                    sendText(MY_BUNDLE.getString("changeDate"), callbackQuery.getMessage(), getResponseInlineKeyboardMarkup(freeUnitsTimeAtWorkday));
                }

            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendText(String textMessage, Message message, ReplyKeyboard replyKeyboardMarkup) throws TelegramApiException {
        SendMessage newMessage = new SendMessage();
        newMessage.setText(parseToEscape(textMessage));
        newMessage.enableMarkdownV2(true);
        newMessage.setReplyMarkup(replyKeyboardMarkup);
        newMessage.setChatId(String.valueOf(message.getChatId()));
        LOGGER.log(Level.INFO,"tray sand message: " + textMessage);
        execute(newMessage);
    }
}

