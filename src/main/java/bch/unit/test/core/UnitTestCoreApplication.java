package bch.unit.test.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
public class UnitTestCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(UnitTestCoreApplication.class, args);
    }

}
