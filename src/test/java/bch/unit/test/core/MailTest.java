package bch.unit.test.core;

import bch.unit.test.core.mail.MailProperties;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@Slf4j
class MailTest {
    @Resource
    private MailSender mailSender;

    @Resource
    private MailProperties mailProperties;

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${spring.profiles.active}")
    private String profile;

    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(
            4,
            8,
            60L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(100),
            r -> new Thread(r, "mail-thread"),
            new ThreadPoolExecutor.CallerRunsPolicy()
    );

    @Test
    void sendTest() {
        UnsupportedOperationException unsupportedOperationException = new UnsupportedOperationException("Is mail valid?");
        List<String> receivers = mailProperties.getReceivers();
        if (receivers != null && !receivers.isEmpty()) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            unsupportedOperationException.printStackTrace(pw);
            String stackTrace = sw.toString();

            // 发送邮件
            receivers.forEach(receiver -> {
                SimpleMailMessage mailMessage = new SimpleMailMessage();
                mailMessage.setFrom("2737224577@qq.com");
                mailMessage.setSubject(applicationName + "-" + profile + "系统异常警告");
                mailMessage.setText(stackTrace);
                mailMessage.setTo(receiver);
                mailSender.send(mailMessage);
//                executor.submit(() -> mailSender.send(mailMessage));
            });
        }
    }
}
