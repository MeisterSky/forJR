package telegram;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

public class TelegramBot extends TelegramLongPollingBot {

    @Override
    public String getBotUsername() {
        return "SheffVictorbot";
    }

    @Override
    public String getBotToken() {
        return "472198295:AAE2QZKlFxKsA63c_t4NF5iPWLVVC8-RBfM";
    }

    @Override
    public void onUpdateReceived(Update update) {

    }
}
