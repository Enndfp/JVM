package com.enndfp.memory_structure_test;

import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.Opcodes;

/**
 * 演示永久代内存溢出    java.lang.OutOfMemoryError: PermGen space
 *  -XX:MaxPermSize=8m
 *
 * @author Enndfp
 */
public class Demo7 extends ClassLoader {
    // 1. 继承ClassLoader可以用来加载类的二进制字节码
    public static void main(String[] args) {
        int j = 0;
        try {
            Demo7 test = new Demo7();
            for (int i = 0; i < 20000; i++, j++) {
                // 2. ClassWriter 作用是生成类的二进制字节码
                ClassWriter cw = new ClassWriter(0);
                // 3. 版本号、访问修饰符、类名、包名、父类、接口
                cw.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC, "Class" + i, null, "java/lang/Object", null);
                // 4. 返回字节码的byte[]
                byte[] code = cw.toByteArray();
                // 5. 执行了类的加载
                test.defineClass("Class" + i, code, 0, code.length);
            }
        }  finally {
            System.out.println(j);
        }
    }
}
