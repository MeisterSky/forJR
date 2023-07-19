package telegram;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class SendBotMessageServiceImpl implements SendBotMessageService {

    private final TelegramBot telegramBot;

    public SendBotMessageServiceImpl(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    @Override
    public void sendMessage(Long chatId, String message) {
        try {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId.toString());
            sendMessage.enableHtml(true);
            sendMessage.setText(message);
            telegramBot.execute(sendMessage);
        } catch (TelegramApiException e) {
            //todo add logging to the project.
            e.printStackTrace();
        }
    }

    @Override
    public void sendMessage(SendMessage message) {

    }
}