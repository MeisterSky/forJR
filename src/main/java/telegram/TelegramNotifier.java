package telegram;

public class TelegramNotifier {

    public void sendNotification(String message) {
        TelegramBot telegramBot = new TelegramBot();
        long chatId = -908987434L;
        SendBotMessageService botMessageService = new SendBotMessageServiceImpl(telegramBot);
        botMessageService.sendMessage(chatId, message);
    }
}
