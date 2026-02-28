package bch.unit.test.core.util;

import lombok.Getter;

@Getter
public class Singleton {

    @SuppressWarnings("java:S3077")
    private static volatile Singleton instance = null;

    private final int value;

    private Singleton() {
        value = 42;
    }

    public static Singleton getInstance() {
        // 避免每次都进入 synchronized(避免每次都进入 synchronized)
        if (instance == null) {
            synchronized (Singleton.class) {
                // 防止并发创建多个对象
                if (instance == null) {
                    // 第一个线程初始化对象到一半，第二个线程来发现已经不是null了就直接返回了 实际上该对象此时还没有完全初始化 可能会出现这个问题
                    // singleton已经不为null,但是singleton对象初始化不完全对象返回了
                    // volatile 防止重排序 保证内存可见性

                    // 这句代码的指令集
                        // ① 分配对象内存
                        // ② 执行构造方法（value = 42）
                        // ③ 把对象引用赋值给 instance

                    // 如果没有 volatile，会发生什么？
                        // ① 分配对象内存
                        // ③ 把引用赋值给 instance   ← 提前
                        // ② 执行构造方法
                    instance = new Singleton();
                }
            }
        }
        return instance;
    }
}