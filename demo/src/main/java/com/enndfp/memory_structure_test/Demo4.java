package com.enndfp.memory_structure_test;

/**
 * 演示栈帧过多导致栈内存溢出 java.lang.StackOverflowError
 * -Xss256k
 * @author Enndfp
 */
public class Demo4 {
    public static int count;

    public static void main(String[] args) {
        try {
            method1();
        } catch (Throwable e) {
            e.printStackTrace();
            System.out.println(count);
        }
    }

    private static void method1() {
        count++;
        method1();
    }
}
