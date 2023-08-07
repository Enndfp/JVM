package com.enndfp.class_loader_test;

/**
 * 解析的含义
 *
 * @author Enndfp
 */
public class Demo1 {

    public static void main(String[] args) throws Exception {
        ClassLoader classLoader = Demo1.class.getClassLoader();
        // loadClass 方法不会导致类的解析和初始化
        Class<?> c = classLoader.loadClass("com.enndfp.class_loader_test.C");

        // new C();
        System.in.read();
    }
}

class C {
    D d = new D();
}

class D {

}
