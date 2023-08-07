package com.enndfp.class_loader_test;

/**
 * 验证双亲委派模型加载过程
 *
 * @author Enndfp
 */
public class Demo5 {
    public static void main(String[] args) throws ClassNotFoundException {
        Class<?> aClass = Demo5.class.getClassLoader().loadClass("com.enndfp.class_loader_test.H");
        System.out.println(aClass.getClassLoader());
    }
}
