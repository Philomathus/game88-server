package tv.game88.core.utils;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import tv.game88.core.config.cache.ConfigEnvCacheUtil;

@Component
@Log4j2
public class TelegramBotMessage extends TelegramLongPollingBot {
    private final ConfigEnvCacheUtil configEnvCacheUtil;

    @Autowired
    public TelegramBotMessage( ConfigEnvCacheUtil configEnvCacheUtil ) {
        super( configEnvCacheUtil.getConf( "robot_message_token" ) );
        this.configEnvCacheUtil = configEnvCacheUtil;
    }

    @Override
    public String getBotUsername() {
        // 填写username
        return configEnvCacheUtil.getConf( "bot_username_telegram", "yanQin" );
    }

    @Override
    public void onUpdateReceived( Update update ) {
        if ( update.hasMessage() && update.getMessage().hasText() ) {
            SendMessage message = new SendMessage();
            message.setChatId( update.getMessage().getChatId().toString() );
            message.setText( "Robot回复的内容" );
            try {
                execute( message );
            } catch ( TelegramApiException e ) {
                log.error( e.getMessage(), e );
            }
        }
    }

    public void sendByChatId( String tex, String chatId ) {
        if ( StringUtils.isBlank( chatId ) || StringUtils.isBlank( tex ) ) {
            return;
        }

        SendMessage message = new SendMessage();
        message.setChatId( chatId );
        message.setText( tex );
        try {
            execute( message );
        } catch ( TelegramApiException e ) {
            log.error( e.getMessage(), e );
        }
    }
}