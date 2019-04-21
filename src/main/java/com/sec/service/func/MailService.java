package com.sec.service.func;

import com.sec.exception.EmailException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.internet.MimeMessage;
import java.util.Map;

@Service
public class MailService {

    @Value("${spring.mail.username}")
    private String from;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;


    public void sendTextMail(String to, String title, String text) {
        try {
            //建立邮件消息
            SimpleMailMessage mainMessage = new SimpleMailMessage();
            //发送者
            mainMessage.setFrom(from);
            //接收者
            mainMessage.setTo(to);
            //发送的标题
            mainMessage.setSubject(title);
            //发送的内容
            mainMessage.setText(text);
            mailSender.send(mainMessage);
        } catch (Exception e) {
            throw new EmailException();
        }
    }

    public void sendHtmlMail(String to, String title, String content) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            //true表示需要创建一个multipart message
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(title);
            helper.setText(content, true);

            mailSender.send(message);
        } catch (Exception e) {
            throw new EmailException();
        }
    }


    public void sendTemplateMail(String to, String title, String templateName, Map<String, Object> map) {
        Context context = new Context();
        context.setVariables(map);
        String content = templateEngine.process(templateName, context);
        this.sendHtmlMail(to, title, content);
    }

}
