package com.enndfp.class_loader_test;

/**
 * 验证启动类加载器
 *
 * @author Enndfp
 */
public class Demo3 {
    public static void main(String[] args) throws ClassNotFoundException {
        Class<?> aClass = Class.forName("com.enndfp.class_loader_test.F");
        System.out.println(aClass.getClassLoader());
    }
}
