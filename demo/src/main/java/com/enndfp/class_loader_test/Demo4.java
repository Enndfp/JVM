package com.enndfp.class_loader_test;

/**
 * 验证扩展类加载器
 *
 * @author Enndfp
 */
public class Demo4 {
    public static void main(String[] args) throws ClassNotFoundException {
        Class<?> aClass = Class.forName("com.enndfp.class_loader_test.G");
        System.out.println(aClass.getClassLoader());
    }
}
