package com.enndfp.memory_structure_test;

/**
 * 局部变量的线程安全问题
 *
 * @author Enndfp
 */
public class Demo2 {

    /**
     * 多个线程同时执行此方法
     * 线程安全的
     */
    public static void m1() {
        int x = 0;
        for (int i = 0; i < 5000; i++) {
            x++;
        }
        System.out.println(x);
    }
}
