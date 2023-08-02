package com.enndfp.memory_structure_test;

/**
 * StringTable(串池)
 *
 * @author Enndfp
 */
// StringTable [ “a”, “b”, “ab” ]   hashtable结构，不能扩容
public class Demo8 {
    // 常量池表中的信息，都会被加载到运行时常量池中，这时 a b ab 都是常量池表中的符号，还没有变为 java 字符串对象
    // ldc #2 会把 a 符号变为 “a” 字符串对象
    // ldc #3 会把 b 符号变为 “b” 字符串对象
    // ldc #4 会把 ab 符号变为 “ab” 字符串对象

    public static void main(String[] args) {
        // 懒惰模式加载
        String s1 = "a";
        String s2 = "b";
        String s3 = "ab";
        // new String(ab)
        String s4 = s1 + s2; // new StringBuilder().append("a").append("b").toString()
    }
}
