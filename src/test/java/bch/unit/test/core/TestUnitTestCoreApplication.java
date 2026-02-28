package bch.unit.test.core;

import org.springframework.boot.SpringApplication;

public class TestUnitTestCoreApplication {

    public static void main(String[] args) {
        SpringApplication.from(UnitTestCoreApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
