package com.navercorp.pinpoint.web.alarm;

import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.web.service.UserGroupService;

import com.navercorp.pinpoint.web.alarm.checker.AlarmChecker;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessagePreparator;

import javax.mail.Message;
import javax.mail.internet.InternetAddress;

public class AlarmMessageSenderImple implements AlarmMessageSender {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String WIDER_RELAY_SMTP_HOST = "relay.cns.widerlab.io";
    private static final String WIDER_RELAY_SMTP_PORT = "25";

    private JavaMailSenderImpl mailSender;

    @Autowired
    private UserGroupService userGroupService;

    @Override
    public void sendSms(AlarmChecker checker, int sequenceCount) {
        List<String> receivers = userGroupService.selectPhoneNumberOfMember(checker.getuserGroupId());

        if (receivers.size() == 0) {
            return;
        }

        for (Object message : checker.getSmsMessage()) {
            logger.info("send SMS : {}", message.toString());

            sendMessage(message.toString());
        }
    }

    @Override
    public void sendEmail(AlarmChecker checker, int sequenceCount) {
        List<String> receivers = userGroupService.selectEmailOfMember(checker.getuserGroupId());

        if (receivers.size() == 0) {
            return;
        }

        String message = checker.getEmailMessage();
        logger.info("send email : {}", message);
        sendMessage(message);

//        for (String message : checker.getEmailMessage()) {
//            logger.info("send email : {}", message);
//            sendMessage(message);
//        }
    }

    private Properties getMailProperties() {
        Properties properties = new Properties();
        properties.setProperty("mail.smtp.host", WIDER_RELAY_SMTP_HOST);
        properties.setProperty("mail.smtp.port", WIDER_RELAY_SMTP_PORT);
        return properties;
    }

    private void sendMessage(String message) {
        this.mailSender = new JavaMailSenderImpl();
        mailSender.setJavaMailProperties(getMailProperties());

        MimeMessagePreparator preparator = mimeMessage -> {
            mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress("sspark@tg360tech.com"));
            mimeMessage.setFrom(new InternetAddress("pinpoint.no-replay@tg360tech.com"));
            mimeMessage.setSubject("pinpoint error");
            mimeMessage.setText(message, "UTF-8", "html");
        };

        this.mailSender.send(preparator);
    }
}
