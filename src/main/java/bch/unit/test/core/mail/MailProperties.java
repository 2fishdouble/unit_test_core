package bch.unit.test.core.mail;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author DCJ
 */
@Data
@Component
@ConfigurationProperties(prefix = "mail")
public class MailProperties {
    /**
     * 收件人邮箱地址列表
     */
    private List<String> receivers;
}