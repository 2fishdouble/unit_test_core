package bch.unit.test.core;

import bch.unit.test.core.util.Singleton;
import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.lang.func.Func0;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.IdcardUtil;
import cn.hutool.core.util.PhoneUtil;
import com.google.common.primitives.Booleans;
import com.nlf.calendar.Lunar;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

@SpringBootTest
@Slf4j
class UnitTestCoreApplicationTests {

    @Test
    void idCardTest() {
        Assertions.assertTrue(IdcardUtil.isValidCard18("512922197601023488", true));
        Assertions.assertTrue(LocalDateTimeUtil.parse(IdcardUtil.getBirthByIdCard("511321202602043479"), "yyyyMMdd")
                .isAfter(LocalDateTimeUtil.parse("1986-04-21", "yyyy-MM-dd")));
        Assertions.assertEquals(0, IdcardUtil.getAgeByIdCard("511321202602033479", new Date()));
        Assertions.assertEquals("511321000512347", IdcardUtil.convert18To15("511321200005123479"));
        Assertions.assertEquals("511321200005123479", IdcardUtil.convert15To18("511321000512347"));
        log.info(IdcardUtil.getIdcardInfo("511321200005123479").toString());
    }

    @Test
    void phoneTest() {
        Assertions.assertTrue(PhoneUtil.isPhone("13378245395"));
        Assertions.assertTrue(PhoneUtil.isPhone("19138958796"));
        Assertions.assertTrue(PhoneUtil.isPhone("028-84442338"));
    }

    @Test
    void emailTest() {
        Assertions.assertTrue(Validator.isEmail("cjd0655@gmail.com"));
    }

    private static final Cache<String, List<String>> CACHE = CacheUtil.newLFUCache(1, 10);

    @Test
    void cacheTest() throws ExecutionException, InterruptedException {
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            Future<?> future = ThreadUtil.execAsync(() -> {
                List<String> dataList = CACHE.get("x1", new Func0<List<String>>() {
                    @Override
                    public List<String> call() {
                        return ListUtil.of("x1", "x2", "x3");
                    }
                });
                if (CollUtil.isEmpty(dataList)) {
                    log.error("dataList is empty");
                }
            });
            futures.add(future);
        }
        for (Future<?> future : futures) {
            future.get();
        }
        log.info("done");
    }

    @Test
    void lunarTest() {
        Lunar date = new Lunar(1986,4,21);
        log.info(date.toFullString());
        System.out.println(date.getSolar().toFullString());
    }

    @Test
    void reentrantLockTest() throws ExecutionException, InterruptedException {
        ReentrantLock reentrantLock = new ReentrantLock(true);

        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Future<?> future = ThreadUtil.execAsync(() -> {
                if (reentrantLock.tryLock()) {
                    log.info("{} try lock success", Thread.currentThread().getName());
                    reentrantLock.unlock();
                }else {
                    log.error("{} lock fail", Thread.currentThread().getName());
                }

            });
            futures.add(future);
        }
        for (Future<?> future : futures) {
            future.get();
        }

        log.info("done");
    }


}
