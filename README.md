## 一、JVM介绍

### 1.定义

JVM（Java Virtual Machine）：Java程序的运行环境（Java二进制字节码运行的环境）

### 2.好处

- 一次编写，到处运行（**write once，run anywhere**）
- 自动内存管理，垃圾回收机制
- 数组下标越界检查
- 多态

### 3.JVM、JRE、JDK

![image-20230801220806077](https://img.enndfp.cn/image-20230801220806077.png)

### 4.学习路线

![image-20230801225349144](https://img.enndfp.cn/image-20230801225349144.png)

![image-20230801225052865](https://img.enndfp.cn/image-20230801225052865.png)

## 二、内存结构

### 1.程序计数器

#### 1.1 定义

程序计数器（Program Counter Register）是一块较小的内存空间，它可以看作是当前线程所执行的 字节码的行号指示器。

#### 1.2 作用

- **记住下一条JVM指令执行的地址**

#### 1.3 特点

- 线程私有
- 不会存在内存溢出（**唯一不会出现内存溢出的区**）**OutOfMemoryError**

#### 1.4 演示

（1）记住下一条JVM指令执行地址

Java源代码经过编译成为二进制字节码(JVM指令)，二进制字节码经过解释器翻译为机器码，机器码交给CPU执行。
如果线程正在执行的是一个Java方法，这个计数器记录的是正在执行的虚拟机字节码指令的地址；如果正在执行的是本地（Native）方法，这个计数器值则应为空（Undefined）。

![image-20230801231502564](https://img.enndfp.cn/image-20230801231502564.png)

（2）线程是私有的

由于Java虚拟机的多线程是通过线程轮流切换、分配处理器执行时间的方式来实现的，在任何一个确定的时刻，一个处理器都只会执行一条线程中的指令。因此，为了线程切换后能恢复到正确的执行位置，每条线程都需要有一个独立的程序计数器，各条线程之间计数器互不影响，独立存储，我们称这类内存区域为“线程私有”的内存。

**有多个线程，每个线程会有一个时间片，在线程1执行的时候会执行线程1的字节码，时间片用完会停止执行给其他线程使用。每个线程都有自己的程序寄存器。**

![image-20230801232040083](https://img.enndfp.cn/image-20230801232040083.png)

### 2.虚拟机栈

#### 2.1 定义

Java Virtual Machine Stacks (Java 虚拟机栈 )

- 每个线程运行时需要的内存，称为虚拟机栈
- 每个栈有多个栈帧（Frame）组成，对应着每次方法调用时所占用的内存
- 每个线程只能有一个活动栈帧（正在运行的方法，即栈顶的方法）对应着当前正在执行的方法

#### 2.2 演示

每个方法运行时需要的内存就是一个栈帧

![image-20230802093906932](https://img.enndfp.cn/image-20230802093906932.png)

![image-20230802101011548](https://img.enndfp.cn/image-20230802101011548.png)

#### 2.3 问题辨析

1.垃圾回收是否涉及栈内存？

答：没有涉及，栈帧在运行完方法是将方法弹出栈，被自动回收掉，根本不需要垃圾回收。

2，栈内存是越大越好吗？
答：不是，栈内存越大，会让线程数变小，因为物理内存是一定的。

3.方法内的局部变量是否线程安全？
答：如果方法内局部变量没有逃离方法的作用范围，它是线程安全的。
		如果是局部变量引用了对象，并逃离方法的作用范围，需要考虑线程安全。(传入对象且返回对象需要考虑线程安全)

```java
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
```

```java
/**
 * 局部变量的线程安全问题
 *
 * @author Enndfp
 */
public class Demo3 {

    public static void main(String[] args) {
        StringBuilder sb = new StringBuilder();
        sb.append(4);
        sb.append(5);
        sb.append(6);
        new Thread(() -> {
            m2(sb);
        }).start();
    }

    /**
     * 线程安全
     */
    public static void m1() {
        StringBuilder sb = new StringBuilder();
        sb.append(1);
        sb.append(2);
        sb.append(3);
        System.out.println(sb.toString());
    }

    /**
     * 非线程安全
     */
    public static void m2(StringBuilder sb) {
        sb.append(1);
        sb.append(2);
        sb.append(3);
        System.out.println(sb.toString());
    }

    /**
     * 非线程安全
     */
    public static StringBuilder m3() {
        StringBuilder sb = new StringBuilder();
        sb.append(1);
        sb.append(2);
        sb.append(3);
        return sb;
    }
}
```

#### 2.4 栈内存溢出

- 栈帧过多导致栈内存溢出
- 栈帧过大导致栈内存溢出

![1690944147946](https://img.enndfp.cn/1690944147946.png)

```java
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
```

#### 2.5 线程运行诊断（CPU占用过多）

- 用top定位哪个进程对CPU占用过多

![image-20230802105941307](https://img.enndfp.cn/image-20230802105941307.png)

- ps H -eo pid,tid,%cpu | grep 进程id （用ps命令进一步定位是哪个线程引起的cpu占用过高）

![image-20230802110203994](https://img.enndfp.cn/image-20230802110203994.png)

- jstack 进程id
  - 可以根据线程id 找到有问题的线程，进一步定位到问题代码的源码行号

![image-20230802110542597](https://img.enndfp.cn/image-20230802110542597.png)

### 3.本地方法栈

#### 3.1 定义

不是由Java编写的方法，调用本地方法时就是使用本地方法栈

关键字：**native**

![image-20230802111818057](https://img.enndfp.cn/image-20230802111818057.png)

![image-20230802111849286](https://img.enndfp.cn/image-20230802111849286.png)

![image-20230802111912156](https://img.enndfp.cn/image-20230802111912156.png)

> **注意**
>
> 程序计数器和栈都是线程私有的

### 4.堆

#### 4.1 定义

Head 堆

- 通过new关键字创建的对象都会使用堆内存

#### 4.2 特点

- 它是线程共享的，堆中对象都需要考虑线程安全问题
- 有垃圾回收机制

#### 4.3 堆内存溢出

```java
/**
 * 演示堆内存溢出 java.lang.OutOfMemoryError
 * -Xmx8m
 * @author Enndfp
 */
public class Demo5 {
    public static void main(String[] args) {
        int i = 0;
        try {
            List<String> list = new ArrayList<>();
            String a = "hello";
            while (true) {
                list.add(a); // hello,hellohello...
                a = a + a;  // hellohello
                i++;
            }
        } catch (Throwable e) {
            e.printStackTrace();
            System.out.println(i);
        }
    }
}
```

![image-20230802113559832](https://img.enndfp.cn/image-20230802113559832.png)

### 5.方法区

#### 5.1 定义

方法区（Method Area）与Java堆一样，是各个线程共享的内存区域，它用于存储已被虚拟机加载的类型信息、常量、静态变量、即时编译器编译后的代码缓存等数据。

#### 5.2 特点

- 线程共享的区域
- 启动时创建
- 存储跟类结构相关的信息：属性、方法、构造方法

#### 5.3 组成

- 1.6版本：PermGen 永久代(实现)
  - 字符串常量池在方法区内部
  - 方法区在JVM内存中
- 1.7版本及以后：Metaspace 元空间(实现)
  - 字符串常量池在堆空间中
  - 方法区在本地内存中

![image-20230802130204750](https://img.enndfp.cn/image-20230802130204750.png)

#### 5.4 方法区内存溢出

- 1.8 之前会导致永久代内存溢出

```java
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
```

- 1.8之后会导致元空间内存溢出

```java
/**
 * 演示元空间内存溢出    java.lang.OutOfMemoryError: Metaspace
 * -XX:MaxMetaspaceSize=8m
 *
 * @author Enndfp
 */
public class Demo6 extends ClassLoader {
    // 1. 继承ClassLoader可以用来加载类的二进制字节码
    public static void main(String[] args) {
        int j = 0;
        try {
            Demo6 test = new Demo6();
            for (int i = 0; i < 20000; i++, j++) {
                // 2. ClassWriter 作用是生成类的二进制字节码
                ClassWriter cw = new ClassWriter(0);
                // 3. 版本号、访问修饰符、类名、包名、父类、接口
                cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "Class" + i, null, "java/lang/Object", null);
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
```

可能溢出的场景：

- Spring
  - 使用cglib动态生成代理类，完成AOP功能

- MyBatis
  - 使用cglib动态生成Mapper接口的实现类

#### 5.5 运行时常量池

运行时常量池（Runtime Constant Pool）是方法区的一部分。Class文件中除了有类的版本、字段、方法、接口等描述信息外，还有一项信息是常量池表（Constant Pool Table），用于存放编译期生成的各种字面量与符号引用，这部分内容将在类加载后存放到方法区的运行时常量池中。

```java
/**
 * 二进制字节码（类的基本信息、常量池表、方法区）
 * @author Enndfp
 */
public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello world!");
    }
}
```

![](https://img.enndfp.cn/image-20230802160103181.png)

![image-20230802160445105](https://img.enndfp.cn/image-20230802160445105.png)

- 常量池，就是一张表，虚拟机指令根据这张常量表找到要执行的类名、方法名、参数类型、字面量等信息 

- 运行时常量池，常量池是 *.class 文件中的，当该类被加载，它的常量池信息就会放入运行时常量池，并把里面的符号地址变为真实地址

### 6.StringTable（串池）

#### 6.1 定义

当类被加载时会将二进制字节码文件中的常量池表放入StringTable（串池）

```java
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
    }
}
```

![image-20230802173847119](https://img.enndfp.cn/image-20230802173847119.png)

#### 6.2 特点

- 常量池表中的字符串仅是符号，第一次用到时才变为对象
- 利用串池的机制，来避免重复创建字符串对象 
- 字符串变量拼接的原理是 StringBuilder （1.8） 

```java
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
        // new String("ab")
        String s4 = s1 + s2; // new StringBuilder().append("a").append("b").toString()
    }
}
```

![image-20230802175656046](https://img.enndfp.cn/image-20230802175656046.png)

- 字符串常量拼接的原理是编译期优化 

  （1）如果是常量拼接，在编译时直接优化

```java
//常量字符串的"+"操作，编译阶段直接会合成为一个字符串。
String str = "计算" + "机"; //编译时合并成String str = "计算机";
```

​	   （2）如果是final修饰的变量，也是在编译时优化

```java
//对于final字段，编译期直接进行了常量替换。
final String str1 = "计算";
final String str2 = "机";
String str3 = str1 + str2; //编译时直接替换成了String str3 = "计算" + "机";
```

​	    **总结：即一个变量不可变时，编译期间会直接优化**

- 可以使用 `intern() `方法，主动将串池中还没有的字符串对象放入串池 

  - 1.8 将这个字符串对象尝试放入串池，如果有则并不会放入，如果没有则直接将**引用地址**放入串池， 都会把串池中的对象返回 

  - 1.6 将这个字符串对象尝试放入串池，如果有则并不会放入，如果没有会把此**对象复制一份**， 将**复制的引用地址**放入串池， 都会会把串池中的对象返回

#### 6.3 位置

在JDK 1.7之前，字符串常量池是位于**方法区**（永久代）中的，而在JDK 1.7以后，它被移动到了**Java堆**中。

这个改变是为了解决在旧版本中字符串常量池可能导致的性能和内存问题。在JDK 1.7之前，字符串常量池存储的字符串对象一直存在于方法区中，而方法区是有固定大小限制的。在大量创建字符串的场景下，可能会导致**方法区的空间不足**，从而引发OutOfMemoryError异常。另外，由于**方法区是共享的**，可能会造成多个线程在进行字符串常量池操作时出现竞争，导致性能瓶颈。

为了解决这些问题，JDK 1.7引入了StringTable，将字符串常量池移到了Java堆中，这样就能够更好地利用堆的空间，避免方法区的限制，并且能够为每个线程提供独立的StringTable，从而减少竞争，提高性能。

```java
/**
 * 演示 StringTable 位置
 * 在jdk8下设置 -Xmx10m -XX:-UseGCOverheadLimit
 * 在jdk6下设置 -XX:MaxPermSize=10m
 *
 * @author Enndfp
 */
public class Demo10 {

    public static void main(String[] args) {
        ArrayList<String> list = new ArrayList<>();
        int i = 0;
        try {
            for (int j = 0; j < 260000; j++) {
                list.add(String.valueOf(j).intern());
                i++;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            System.out.println(i);
        }
    }
}
```

没设置参数：**-XX:-UseGCOverheadLimit**

![image-20230802203629269](https://img.enndfp.cn/image-20230802203629269.png)

**GC overhead limit exceeded**：表示应用程序在垃圾收集（GC）上花费的时间比在有用工作上花费的时间要多。当应用程序花费98%的时间进行垃圾回收时，只释放了2%堆空间，JVM会抛出此错误。

#### 6.4 垃圾回收

```java
/**
 * 演示 StringTable 垃圾回收
 * -Xmx10m -XX:+PrintStringTableStatistics -XX:+PrintGCDetails -verbose:gc
 *
 * @author Enndfp
 */
public class Demo11 {

    public static void main(String[] args) {
        int i = 0;
        try {
            for (int j = 0; j < 10000; j++) {
                String.valueOf(j).intern();
                i++;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            System.out.println(i);
        }
    }
}
```

- **-Xmx10m**：设置虚拟机堆内存最大空间
- **-XX:+PrintStringTableStatistics**：打印常量池表的统计信息
- **-XX:+PrintGCDetails -verbose:gc**：打印垃圾回收的相关信息

#### 6.5 性能调优

- 调整 -XX:StringTableSize=桶个数
- 考虑将字符串对象入池

### 7.直接内存

#### 7.1 定义

Direct Memory 

- 常见于 NIO 操作时，用于数据缓冲区 
- 分配回收成本较高，但读写性能高 
- 不受 JVM 内存回收管理

#### 7.2 ByteBuffer

使用ByteBuffer比使用io的性能更高

![image-20230803093324277](https://img.enndfp.cn/image-20230803093324277.png)

在没有用ByteBuffer时，系统的内部操作如下图

![image-20230803093428556](https://img.enndfp.cn/image-20230803093428556.png)

使用了直接内存后，系统内部操作如下图。不再需要经过系统缓存区传给java缓冲区，他们**共同划出一块缓冲区**，java代码和系统都可以直接访问，大大的提升了效率。少了缓冲区的复制操作。

![image-20230803093624039](https://img.enndfp.cn/image-20230803093624039.png)

#### 7.3 直接内存释放原理

直接内存的回收不是通过JVM的垃圾回收来释放的，而是拿到**Unsafe**对象，然后调用去分配和调用内存

```java
/**
 * 演示直接内存释放原理 Unsafe对象
 *
 * @author Enndfp
 */
public class Demo12 {
    public static int _1Gb = 1024 * 1024 * 1024;

    public static void main(String[] args) throws IOException {
        Unsafe unsafe = getUnsafe();
        long base = unsafe.allocateMemory(_1Gb);
        unsafe.setMemory(base, _1Gb, (byte) 0);
        System.in.read();

        unsafe.freeMemory(base);
        System.in.read();
    }
    public static Unsafe getUnsafe() {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            Unsafe unsafe = (Unsafe) f.get(null);
            return unsafe;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
```

#### 7.4 分配与回收的原理

- 使用了`Unsafe` 对象完成直接内存的分配回收，并主动回收需要主动调用`freeMemory`方法
- `ByteBuffer`的实现类内部，使用了`Cleaner` (虚引用) 来监测`ByteBuffer`对象，一旦`ByteBuffer`对象被垃圾回收，那么就会由`ReferenceHandler`线程通过`Cleaner`的`clean`方法调用`freeMemory`来释放直接内存

![image-20230803095315645](https://img.enndfp.cn/image-20230803095315645.png)

![image-20230803095351774](https://img.enndfp.cn/image-20230803095351774.png)

## 三、垃圾回收

### 1.如何判断对象可以回收

#### 1.1 引用计数法

##### 1.1.1 定义

在对象中添加一个引用计数器，每当有一个地方引用它时，计数器值就加一；当引用失效时，计数器值就减一；任何时刻计数器为零的对象就是不可能再被使用的

##### 1.1.2 弊端

![image-20230803112136064](https://img.enndfp.cn/image-20230803112136064.png)

 循环引用，A对象引用B对象，B对象引用计数+1，B对象引用A，A对象引用计数+1。当没有谁再引用他们，他们不能被垃圾回收，因为引用计数没有归零。python在早期垃圾回收用的引用计数法。

#### 1.2 可达性分析算法

##### 1.2.1 定义

- java虚拟机中的垃圾回收器采用的是可达性分析算法（Reachability Analysis）
- **扫描堆中的对象，看是否能够沿着GC Root(根对象) 为起点的引用链找到该对象，找不到就可以进行垃圾回收**

![image-20230803112750525](https://img.enndfp.cn/image-20230803112750525.png)

##### 1.2.2 GC Root对象分类

- 在虚拟机栈（栈帧中的本地变量表）中引用的对象，譬如各个线程被调用的方法堆栈中使用到的参数、局部变量、临时变量等。 
- 在方法区中类静态属性引用的对象，譬如Java类的引用类型静态变量（一般指被static修饰的对象，加载类的时候就加载到内存中）
- 在方法区中常量引用的对象，譬如字符串常量池（String Table）里的引用。 
- 在本地方法栈中JNI（即通常所说的Native方法）引用的对象。
- Java虚拟机内部的引用，如基本数据类型对应的Class对象，一些常驻的异常对象（比如 NullPointExcepiton、OutOfMemoryError）等，还有系统类加载器。 
- 所有被同步锁（synchronized关键字）持有的对象。 
- 反映Java虚拟机内部情况的JMXBean、JVMTI中注册的回调、本地代码缓存等。

#### 1.3 五种引用

![image-20230803114618877](https://img.enndfp.cn/image-20230803114618877.png)

##### 1.3.1 强引用（Strong Reference）

- 无论任何情况下，即使系统内存不足，只要强引用关系还存在，垃圾收集器就永远不会回收掉被引用的对象。

##### 1.3.2 软引用（Soft Reference）

- 软引用是用来描述一些还有用，但非必须的对象。
  - 仅有软引用引用该对象时，在垃圾回收后，**内存仍不足时**会再次出发垃圾回收，回收软引用对象 
  - 可以配合引用队列来释放软引用自身

##### 1.3.3 弱引用（Weak Reference）

- 弱引用也是用来描述那些非必须对象
  - 仅有弱引用引用该对象时，在垃圾回收时，**无论内存是否充足**，都会回收弱引用对象 
  - 可以配合引用队列来释放弱引用自身

##### 1.3.4 虚引用（Phantom Reference）

- 虚引用也称为“幽灵引用”或者“幻影引用”，它是最弱的一种引用关系。
  - 必须配合引用队列使用，主要配合 ByteBuffer 使用，被引用对象回收时，会将虚引用入队， 由 Reference Handler 线程调用虚引用相关方法释放直接内存

##### 1.3.5 终结器引用（Final Reference）

- 无需手动编码，但其内部配合引用队列使用，在垃圾回收时，终结器引用入队（被引用对象暂时没有被回收），再由 Finalizer 线程通过终结器引用找到被引用对象并调用它的 finalize 方法，第二次 GC 时才能回收被引用对象

##### 1.3.6 示例代码

```java
/**
 * 演示软引用，配合引用队列
 * -Xmx20m
 *
 * @author Enndfp
 */
public class Demo2 {
    private static final int _4MB = 4 * 1024 * 1024;

    public static void main(String[] args) {
        List<SoftReference<byte[]>> list = new ArrayList<>();

        // 引用队列
        ReferenceQueue<byte[]> queue = new ReferenceQueue<>();

        for (int i = 0; i < 5; i++) {
            // 关联了引用队列，当软引用所关联的 byte[] 被回收时，软引用自己会加到 queue 中
            SoftReference<byte[]> ref = new SoftReference<>(new byte[_4MB], queue);
            System.out.println(ref.get());
            list.add(ref);
            System.out.println(list.size());
        }

        // 从队列中获取无用的软引用对象，并移除
        Reference<? extends byte[]> poll = queue.poll();
        while (poll != null) {
            list.remove(poll);
            poll = queue.poll();
        }

        System.out.println("===========================");
        for (SoftReference<byte[]> ref : list) {
            System.out.println(ref.get());
        }

    }
}
```

**弱引用的使用和软引用类似**，只是将 **SoftReference** 换为了 **WeakReference**

### 2.垃圾回收算法

#### 2.1 标记清除算法（Mark-Sweep）

![image-20230804095727269](https://img.enndfp.cn/image-20230804095727269.png)

##### 2.1.1 定义

首先标记出所有需要回收的对象，在标记完成后，统一回收掉所有被标记的对象，也可以反过来，标记存活的对象，统一回收所有未被标记的对象。

##### 2.1.2 优点

- **速度快**

##### 2.1.3 缺点

- **执行效率不稳定**（如果Java堆中包含大量对象，而且其中大部分是需要被回收的，这时必须进行大量标记和清除的动作，导致标记和清除两个过程的执行效率都随对象数量增长而降低）

- **内存空间的碎片化问题**（标记、清除之后会产生大 量不连续的内存碎片，空间碎片太多可能会导致当以后在程序运行过程中需要分配较大对象时无法找 到足够的连续内存而不得不提前触发另一次垃圾收集动作）

#### 2.2 标记整理算法（Mark Compact）

![image-20230804101439895](https://img.enndfp.cn/image-20230804101439895.png)

##### 2.2.1 定义

在执行垃圾回收时，先标记完引用的对象，然后清除没有被引用的对象，最后整理剩余的空间，避免因内存碎片导致的问题。

##### 2.2.2 优点

- **避免了内存碎片问题**

##### 2.2.3 缺点

- **效率低，速度慢**（因为整理内存是为了避免存在内存碎片问题，所以整理需要消耗一定的时间，导致效率较低）
- **时间换空间**

#### 2.3 标记复制算法（Mark Copy）

![image-20230804102831404](https://img.enndfp.cn/image-20230804102831404.png)

##### 2.3.1 定义

它将可用内存按容量划分为大小相等的两块，每次只使用其中的一块。当这一块的内存用完了，就将还存活着的对象复制到另外一块上面，然后再把已使用过的内存空间一次清理掉。

##### 2.3.2 优点

- **避免了内存碎片问题**

##### 2.3.3 缺点

- **将可用内存空间缩小为原来一半**
- **空间换时间**

### 3.分代回收

![image-20230804105402003](https://img.enndfp.cn/image-20230804105402003.png)

![image-20230804105428062](https://img.enndfp.cn/image-20230804105428062.png)

- 对象首先分配在伊甸园区域
- 当伊甸园空间不足时，就会触发`Minor GC`，伊甸园和幸存区`From`存活的对象使用**标记复制算法**移动到幸存区`To`中，存活的对象年龄加`1`，并且交换`From`和`To`
- 移动存活对象（`Minor  GC`、`Major GC`、`Full GC`等）都会触发“`Stop The World`”（需要暂停用户线程来标记、清理可回收对象的，只是停顿时间相对而言要来的短而已，等垃圾回收结束，用户线程恢复运行）
- 当对象年龄超过阈值时，会晋升至老年代，最大年龄为**15（4bit）**

#### 3.1 垃圾回收触发条件

1.触发新生代垃圾回收（Minor GC）的条件：

- 当新生代中的伊甸园（Eden）空间满时，会触发Minor GC。伊甸园空间用于分配新创建的对象。
- 在Minor GC期间，存活的对象会被移动到幸存者（Survivor）空间中。幸存者空间也会被使用和填满，但大部分的对象通常会在Minor GC后晋升到老年代。
- Minor GC的主要目标是清理伊甸园和幸存者空间中的垃圾对象，将存活对象晋升到老年代。

2.触发老年代垃圾回收（Major GC或Full GC）的条件：

- 当老年代空间快要满时，会触发Major GC。老年代空间用于存放生命周期较长的对象。
- 如果执行Minor GC后，存活的对象在老年代中无法找到足够的空间存放，就会触发Major GC。
- 在使用CMS（Concurrent Mark-Sweep）垃圾回收器时，如果CMS的并发收集阶段无法跟上应用程序的垃圾产生速度，就可能会触发Full GC来进行老年代的回收。
- 在使用G1（Garbage-First）垃圾回收器时，如果在进行Mixed GC（混合收集）时，无法为存活的对象分配所需的内存空间，就会触发Full GC。

#### 3.2 相关VM参数

| 含义               | 参数                                                         |
| ------------------ | ------------------------------------------------------------ |
| 堆初始大小         | -Xms                                                         |
| 堆最大大小         | -Xmx 或 -XX:MaxHeapSize=size                                 |
| 新生代大小         | -Xmn 或 (-XX:NewSize=size + -XX:MaxNewSize=size )            |
| 幸存区比例（动态） | -XX:InitialSurvivorRatio=ratio 和 -XX:+UseAdaptiveSizePolicy |
| 幸存区比例         | -XX:SurvivorRatio=ratio                                      |
| 晋升阈值           | -XX:MaxTenuringThreshold=threshold                           |
| 晋升阈值           | -XX:+PrintTenuringDistribution                               |
| GC详情             | -XX:+PrintGCDetails -verbose:gc                              |
| FullGC 前 MinorGC  | -XX:+ScavengeBeforeFullGC                                    |

### 4.垃圾回收器

#### 4.1 相关概念

- **并行（Parallel）**：并行描述的是多条垃圾收集器线程之间的关系，说明同一时间有多条这样的线程在协同工作，通常默认此时用户线程是处于等待状态。
- **并发（Concurrent）**：并发描述的是垃圾收集器线程与用户线程之间的关系，说明同一时间垃圾 收集器线程与用户线程都在运行。由于用户线程并未被冻结，所以程序仍然能响应服务请求，但由于 垃圾收集器线程占用了一部分系统资源，此时应用程序的处理的吞吐量将受到一定影响。
- **吞吐量（Throughput）**：就是处理器用于运行用户代码的时间与处理器总消耗时间的比值

![image-20230804132526520](https://img.enndfp.cn/image-20230804132526520.png)

如果虚拟机完成某个任务，用户代码加上垃圾收集总共耗费了100分钟，其中垃圾收集花掉1分 钟，那吞吐量就是99%。

#### 4.2 串行回收器

```bash
-XX:+UseSerialGC = Serial + SerialOld
```

![image-20230804160852359](https://img.enndfp.cn/image-20230804160852359.png)

![image-20230804161048193](https://img.enndfp.cn/image-20230804161048193.png)

##### 4.2.1 特点

- 单线程
- 内存较小
- 安全点：让其他线程都在这个点停下来，以免垃圾回收时移动对象地址，使得其他线程找不到被移动的对象
- 因为是串行的，所以只有一个垃圾回收线程。且在该线程执行回收工作时，其他线程进入**阻塞**状态。

##### 4.2.2 Serial收集器

Serial收集器是最基础、历史最悠久的收集器

**特点**：单线程、简单高效（与其他收集器的单线程相比），采用**复制算法**。对于限定单个CPU的环境来说，Serial收集器由于没有线程交互的开销，专心做垃圾收集自然可以获得最高的单线程收集效率。收集器进行垃圾回收时，必须暂停其他所有的工作线程，直到它结束（Stop The World）

##### 4.2.3 ParNew 收集器

ParNew收集器其实就是Serial收集器的多线程版本。

**特点**：多线程、ParNew收集器默认开启的收集线程数与CPU的数量相同，采用**复制算法**，在处理器核心非常多的环境中，可以使用-XX:ParallelGCThreads参数来限制垃圾收集的线程数。和Serial收集器一样存在Stop The World问题

##### 4.2.4 Serial Old 收集器

Serial Old是Serial收集器的老年代版本

**特点**：同样是单线程收集器，采用**标记-整理算法**

#### 4.3 吞吐量优先

```bash
-XX:+UseParallelGC ~ -XX:+UseParallelOldGC
-XX:+UseAdaptiveSizePolicy
-XX:GCTimeRatio=ratio
-XX:MaxGCPauseMillis=ms
-XX:ParallelGCThreads=n
```

![image-20230804164546077](https://img.enndfp.cn/image-20230804164546077.png)

![image-20230804164605866](https://img.enndfp.cn/image-20230804164605866.png)

##### 4.3.1 特点

- 多线程
- 堆内存较大，多核CPU
- 单位时间内，STW(stop the world,停掉其他所有工作线程）时间最短
- JDK1.8默认使用的垃圾回收器

##### 4.3.2 Parallel Scavenge 收集器

与吞吐量关系密切，故也称为**吞吐量优先收集器**

**特点**：

属于**新生代收集器**，也是采用**复制算法**的收集器（用到了新生代的幸存区），又是**并行**的多线程收集器（与ParNew收集器类似）

该收集器的目标是达到一个可控制的吞吐量。还有一个值得关注的点是：**GC自适应调节策略**（与ParNew收集器最重要的一个区别）

**GC自适应调节策略**：

Parallel Scavenge收集器可设置-XX:+UseAdptiveSizePolicy参数。当开关打开时**不需要**手动指定新生代的大小（-Xmn）、Eden与Survivor区的比例（-XX:SurvivorRation）、晋升老年代的对象年龄（-XX:PretenureSizeThreshold）等，虚拟机会根据系统的运行状况收集性能监控信息，动态设置这些参数以提供最优的停顿时间和最高的吞吐量，这种调节方式称为GC的自适应调节策略。

Parallel Scavenge收集器使用两个参数控制吞吐量：

- -XX:MaxGCPauseMillis ：控制最大的垃圾收集停顿时间
- -XX：GCTimeRatio： 直接设置吞吐量的大小   [计算公式：**1 / (1+ratio) **]

##### 4.3.3 **Parallel Old 收集器**

是Parallel Scavenge收集器的老年代版本

**特点**：多线程，采用**标记-整理算法**

#### 4.4 响应时间优先

```bash
-XX:+UseConcMarkSweepGC ~ -XX:+UseParNewGC ~ SerialOld
-XX:ParallelGCThreads=n ~ -XX:ConcGCThreads=threads
-XX:CMSInitiatingOccupancyFraction=percent
-XX:+CMSScavengeBeforeRemark
```

![image-20230804170257270](https://img.enndfp.cn/image-20230804170257270.png)

![image-20230804170336525](https://img.enndfp.cn/image-20230804170336525.png)

##### 4.4.1 特点

- 多线程
- 堆内存较大，多核CPU
- 尽可能让单次STW时间变短（尽量不影响其他线程运行）

##### 4.4.2 CMS收集器

Concurrent Mark Sweep，一种以获取**最短回收停顿时间**为目标的**老年代**收集器

**特点**：基于**标记-清除算法**实现。**并发收集**、低停顿，但是会产生内存碎片

**CMS收集器的运行过程分为下列4步：**

- **初始标记**：标记GC Roots对象。速度很快但是**仍存在Stop The World问题**
- **并发标记**：进行GC Roots Tracing 的过程，**找出GC Roots对象所关联的对象且用户线程可并发执行**
- **重新标记**：为了**修正并发标记期间**因用户程序继续运行而导致标记产生变动的那一部分对象的标记记录(可达对象变不可达)。仍然存在Stop The World问题
- **并发清除**：对没有标记的对象进行清除回收

**CMS收集器**的**内存回收过程**是与**用户线程**一起**并发执行**的

#### 4.5 Garbage First收集器

##### 4.5.1 定义

Garbage First（简称G1）收集器是垃圾收集器技术发展历史上的里程碑式的成果，它开创了收集器面向局部收集的设计思路和基于Region的内存布局形式。

- 2004年论文发布
- 2009 JDK 6u14体验
- 2012 JDK 7u4官方支持
- 2017 JDK 9 默认

---

##### 4.5.2 使用场景

- 同时注重吞吐量（Throughput）和低延迟（Low latency)，默认的暂停时间是200ms。
- 超大堆内存，会将堆划分为多个等大的Region。
- **整体上是标记 - 整理算法**，**两个Region（区域）之间是复制算法**。

---

##### 4.5.3 相关JVM参数

```bash
-XX:+UserG1GC
-XX:G1HeapRegionSize=size
-XX:MaxGCPauseMillis=time
```

第一个参数：开启G1
第二个参数：设置Region大小，取值范围为1MB～32MB，且应为2的N次幂
第三个参数：设置暂停时间ms

---

##### 4.5.4 G1垃圾回收阶段

![image-20230805110809450](https://img.enndfp.cn/image-20230805110809450.png)

第一阶段对新生代进行收集（Young Collection），第二阶段对新生代的收集同时会执行并发标记（Young Collection+ Concurrent Mark） ，第三阶段对新生代、新生代幸存区和老年区进行混合收集（Mixed Collection），以此循环。

Garbage First 将堆划分大小相等的一个个区域，**每个区域都可以作为新生代、幸存区和老年代。**

- E 代表伊甸园区域
- S 代表幸存区
- O 代表老年代

1. **Young Collection(新生代收集)**

![image-20230805111121585](https://img.enndfp.cn/image-20230805111121585.png)

- 会STW（Stop The World），但相对于时间还是比较短的

![image-20230805111316066](https://img.enndfp.cn/image-20230805111316066.png)

- 新生代垃圾回收会将幸存对象以复制算法复制到幸存区

![image-20230805111437742](https://img.enndfp.cn/image-20230805111437742.png)

- 新生代垃圾回收会将幸存对象以复制算法复制到幸存区，幸存区存活的对象达到阈值后会以转发复制的方式进入老年代

2. **Young Collection+ Concurrent Mark(新生代收集+并发标记)**

初始标记：找到GC Root（根对象）
并发标记：和用户线程并发执行，针对区域内所有的存活对象进行标记

- 在**Young GC时**会进行GC Root的**初始标记 **
- **老年代占用堆空间比例达到阈值时**，进行**并发标记**（不会STW),由下面的JVM参数决定

```bash
-XX:InitiatingHeapOccupancyPercent=percent   (默认45%)
```

![image-20230805112537224](https://img.enndfp.cn/image-20230805112537224.png)

- 当O占到45%就进行并发标记了

​	3.**Mixed Collection(混合收集)**

会对 E、S、O进行全面垃圾回收

**最终标记**：在并发标记的过程中，可能会漏掉一些对象，因为并发标记的同时，其他用户线程还在工作，产生一些垃圾，所以进行最终标记。清理没被标记的对象

- 最终标记（Remark）会STW
- **拷贝存活**（Evacuation）会STW

```bash
-XX:MaxGCPauseMillis=ms
```

![image-20230805113313962](https://img.enndfp.cn/image-20230805113313962.png)

- 在进行混合回收时，新生代垃圾回收会将幸存对象以复制算法复制到幸存区，幸存区存活的对象达到阈值后会以转发复制的方式进入老年代，老年代中根据最大暂停时间有选择的进行回收，优先处理回收价值收益最大的那些Region，将老年代中存活下来的对象以复制算法重新赋值到一个新的老年代中

---

##### 4.5.5 Full GC触发条件

![image-20230805114254454](https://img.enndfp.cn/image-20230805114254454.png)

**1.** **调用** **System.gc()**

只是建议虚拟机执行 Full GC，但是虚拟机不一定真正去执行。不建议使用这种方式，而是让虚拟机管理内存。

**2. 未指定老年代和新生代大小，堆伸缩时会产生Full GC,所以一定要配置-Xms、-Xmx**

**3.** **老年代空间不足**

老年代空间不足的常见场景比如大对象、大数组直接进入老年代、长期存活的对象进入老年代等。

为了避免以上原因引起的 Full GC，应当尽量不要创建过大的对象以及数组。

除此之外，可以通过 -Xmn 虚拟机参数调大新生代的大小，让对象尽量在新生代被回收掉，不进入老年代。

还可以通过 -XX:MaxTenuringThreshold 调大对象进入老年代的年龄，让对象在新生代多存活一段时间。

在执行Full GC后空间仍然不足，则抛出错误：<font color=red>java.lang.OutOfMemoryError: Java heap space</font>

**4. 空间分配担保失败**

空间担保，下面两种情况是空间担保失败：

1、每次晋升的对象的平均大小 > 老年代剩余空间

2、Minor GC后存活的对象超过了老年代剩余空间

>**注意**
>
>GC日志中是否有`promotion failed`和`concurrent mode failure`两种状况，当出现这两种状况的时候就有可能会触发Full GC。
>
>- promotion failed 是在进行 Minor GC时候，survivor space空间放不下只能晋升老年代，而此时老年代也空间不足时发生的。
>
>- concurrent mode failure 是在进行CMS GC过程，此时有对象要放入老年代而空间不足造成的，这种情况下会退化使用Serial Old收集器变成单线程的，此时是相当的慢的。

**5. JDK 1.7 及以前的（永久代）空间满**

在 JDK 1.7 及以前，HotSpot 虚拟机中的方法区是用永久代实现的，永久代中存放的为一些 Class 的信息、常量、静态变量等数据。

当系统中要加载的类、反射的类和调用的方法较多时，永久代可能会被占满，在未配置为采用 CMS GC 的情况下也会执行 Full GC。

如果经过 Full GC 仍然回收不了，那么虚拟机会抛出：<font color=red>java.lang.OutOfMemoryError: PermGen space</font>

为避免以上原因引起的 Full GC，可采用的方法为增大Perm Gen或转为使用 CMS GC。

------

<mark>**空间分配担保补充**</mark>

**在发生Minor GC之前，虚拟机必须先检查老年代最大可用的连续空间是否大于新生代所有对象总空间，如果这个条件成立，那这一次Minor GC可以确保是安全的。如果不成立，则虚拟机会先查看-XX：HandlePromotionFailure参数的设置值是否允许担保失败（Handle Promotion Failure）；如果允许，那会继续检查老年代最大可用的连续空间是否大于历次晋升到老年代对象的平均大小，如果大于，将尝试进行一次Minor GC，尽管这次Minor GC是有风险的；如果小于，或者-XX： HandlePromotionFailure设置不允许冒险，那这时就要改为进行一次Full GC。**

---

##### 4.5.6 Young Collection 跨代引用

- 卡表与Remembered Set（记忆集）
  - **Remembered Set**存在于**E**中，**用于保存新生代对象对应的脏卡**
    - 脏卡: 老年代被划分为多个区域(一个区域512K)，如果该
      区域引用了新生代对象，则该区域被称为**脏卡**

- 在引用变更时通过post-write barried（写屏障） + dirty card queue（脏卡队列）
- concurrent refinement threads更新Remembered Set
- 新生代回收的跨代引用（老年代引用新生代）问题

![image-20230805194743404](https://img.enndfp.cn/image-20230805194743404.png)

在进行新生代回收时要找到GC Root根对象。有一部分GC Root对象是来自老年代，老年代存活的对象很多，如果遍历老年代找根对象效率非常低，采用**卡表**（Card Table)的技术，将老年代分成一个个Card,每个Card差不多512k， 老年代其中一个对象引用了新生代对象，那么就称这个Card为**脏卡**（dirty card)

![image-20230805194914288](https://img.enndfp.cn/image-20230805194914288.png)

将来进行垃圾回收时不需要找整个老年代，只需要找脏卡区就行了

---

##### 4.5.7 Remark（重新标记）

```bash
pre-write barrier + satb_mark_queue
```

在垃圾回收时，收集器处理对象的过程中

- 黑色：已被处理，需要保留的
- 灰色：正在处理中的
- 白色：还未处理的

![image-20230805195634265](https://img.enndfp.cn/image-20230805195634265.png)

但是在**并发标记过程中**，有可能A已经被处理了还没有引用C，但该处理过程还未结束，在处理过程结束之前A引用了C，这时就会用到remark过程如下：

- 之前C未被引用，这时A引用了C，就会给C加一个写屏障，写屏障的指令会被执行，将C放入一个队列当中，并将C变为处理中状态
- 在**并发标记**阶段结束以后，重新标记阶段会STW，然后将放在该队列中的对象重新处理，发现有强引用引用它，就会处理它

![image-20230805200931536](https://img.enndfp.cn/image-20230805200931536.png)

---

##### 4.5.8 JDK 8u20 字符串去重

- 优点：节省大量内存 
- 缺点：略微多占用了 cpu 时间，新生代回收时间略微增加

```bash
-XX:+UseStringDeduplication		#打开开关
```

- 将所有新分配的字符串放入一个队列
- 当新生代回收时，G1并发检查是否有字符串重复 
- 如果它们值一样，让它们引用同一个 char[] 
- 注意，与 `String.intern() `不一样
  - `String.intern()` 关注的是字符串对象 
  - 而字符串去重关注的是 char[]
  - 在 JVM 内部，使用了不同的字符串表

---

##### 4.5.9 JDK 8u40 并发标记类卸载

所有对象都经过并发标记后，就能知道哪些类不再被使用，当一个类加载器的所有类都不再使用，则卸 载它所加载的所有类

`-XX:+ClassUnloadingWithConcurrentMark` 默认启用

---

##### 4.5.10 JDK 8u60 回收巨型对象

- 一个对象大于region的一半时，就称为巨型对象
- G1不会对巨型对象进行拷贝
- 回收时被优先考虑
- G1会跟踪老年代所有incoming引用，如果老年代incoming引用为0的巨型对象就可以在新生代垃圾回收时处理掉

![image-20230805202907119](https://img.enndfp.cn/image-20230805202907119.png)

---

##### 4.5.11 JDK 9并发标记起始时间的调整

- 并发标记必须在堆空间占满前完成，否则退化为 FullGC
- JDK9之前需要使用`-XX:InitiatingHeapOccupancyPercent`
- JDK9可以动态调整
  - `-XX:InitiatingHeapoccupancyPercent` 用来设置初始值，默认45%
  - 进行数据采样并动态调整
  - 总会添加一个安全的空档空间

### 5.垃圾回收调优

查看虚拟机运行参数

```bash
"D:\product\java\java8\bin\java" -XX:+PrintFlagsFinal -version | findstr "GC"
```

#### 5.1 调优领域

- 内存
- 锁竞争
- CPU 占用
- IO

#### 5.2 确定目标

- 【低延迟】还是【高吞吐量】，选择合适的回收器 

- CMS，G1，ZGC
- ParallelGC 
- Zing

#### 5.3 最快的 GC

答案是不发生 GC

- 查看 FullGC 前后的内存占用，考虑下面几个问题
  - 数据是不是太多？
    - resultSet = statement.executeQuery("select * from 大表 limit n")
  - 数据表示是否太臃肿？
    - 对象图
    - 对象大小 16 Integer 24  int 4
  - 是否存在内存泄漏？
    - static Map map =
    - 软
    - 弱
    - 第三方缓存实现

#### 5.4 新生代调优

新生代的特点

- 所有new操作的内存分配非常廉价
  - TLAB   Thread-local Allocation Buffer
- 死亡对象的回收代价是零
- 大部分对象用过即死
- Minor GC 的时间远远低于 Full GC

调优参数：<font color='red'>`-Xmn`</font>设置新生代的大小

**设置越大越好吗？**

不是，官方说明如下：

![image-20230806111700820](https://img.enndfp.cn/image-20230806111700820.png)

- 新生代内存太小：频繁触发 `Minor GC` ，`Minor GC`会 `stop the world`暂停 ，会使得吞吐量下降

- 新生代内存太大：老年代内存占比有所降低，会更频繁地触发 `Full GC`。而且触发 `Minor GC `时，清理新生代所花费的时间会更长

<mark>oracle建议你的新生代内存大于整个堆的25%。小于堆的50%</mark>

调优要考虑的条件：

- 新生代能容纳所有【并发量 * (请求-响应)】的数据
- 幸存区大到能保留【当前活跃对象+需要晋升对象】
- 晋升阈值配置得当，让长时间存活对象尽快晋升
  - 调整最大晋升阈值：`-XX:MaxTenuringThreshold=threshold`
  - 垃圾回收时打印存活对象详情：`-XX:+PrintTenuringDistribution`

#### 5.5 老年代调优

以 CMS 为例：

- CMS 的老年代内存越大越好
- 先尝试不做调优，如果没有 Full GC 那么已经…，否则先尝试调优新生代
- 观察发生 Full GC 时老年代内存占用，将老年代内存预设调大 1/4 ~ 1/3
  - 老年代空间占用达到多少比例时触发垃圾回收参数：`-XX:CMSInitiatingOccupancyFraction=percent`

#### 5.6 案例

- **案例1：Full GC 和Minor GC 频繁**

说明空间紧张，如果是新生代空间紧张，当业务高峰期来了，大量对象被创建，很快新生代空间满，会经常Minor GC，而原本很多生存周期短的对象也会被晋升到老年代，老年代存了大量的生存周期短的对象，导致频繁触发Full GC,所以应该先调整新生代的内存空间大一点，让对象尽可能的在新生代

- 

- **案例2：请求高峰期发生Full GC，单次暂停时间特别长。（CMS）**

分析在哪一部分耗时较长，通过查看GC日志，比较慢的通常会是在重新标记阶段，重新标记会扫描整个堆内存，根据对象找引用，所以解决办法是在重新标记之前，先在新生代进行垃圾回收，这样就会减少重新标记的耗时时间，通过 `-XX:+CMSScavengeBeforeRemark `参数在重新标记之前进行垃圾回收

## 四、类加载

### 1.类文件结构

根据 JVM 规范，类文件结构如下：

```java
ClassFile {
	u4 			   magic;
	u2 			   minor_version;
	u2 			   major_version;
	u2 			   constant_pool_count;
	cp_info 	   constant_pool[constant_pool_count-1];
	u2 			   access_flags;
	u2 			   this_class;
	u2             super_class;
	u2             interfaces_count;
	u2             interfaces[interfaces_count];
	u2             fields_count;
	field_info     fields[fields_count];
	u2             methods_count;
	method_info    methods[methods_count];
	u2 			   attributes_count;
	attribute_info attributes[attributes_count];
}
```

#### 1.1 魔数

0~3 字节，表示它是否是【class】类型的文件 

0000000 <font color="red">ca fe ba be</font> 00 00 00 34 00 23 0a 00 06 00 15 09

#### 1.2 版本

4~7 字节，表示类的版本

0000000 ca fe ba be <font color="red">00 00 00 34</font> 00 23 0a 00 06 00 15 09

00 00 ---表示小版本
00 34 ---主版本，表示52，代表JDK8

#### 1.3 常量池

![image-20230806143304555](https://img.enndfp.cn/image-20230806143304555.png)

#### 1.4 访问标识与继承信息

![image-20230806143529604](https://img.enndfp.cn/image-20230806143529604.png)

#### 1.5 Field 信息

![image-20230806143647191](https://img.enndfp.cn/image-20230806143647191.png)

#### 1.6 Method 信息

![image-20230806143805632](https://img.enndfp.cn/image-20230806143805632.png)

### 2.类加载阶段

#### 2.1 加载

- 将类的字节码载入方法区中，内部采用 C++ 的 instanceKlass 描述 java 类，它的重要 field 有：
  - _java_mirror 即 java 的类镜像，例如对 String 来说，就是 String.class，作用是把 instanceKlass 暴露给 java 使用
  - _super 即父类 
  - _fields 即成员变量 
  - _methods 即方法 
  - _constants 即常量池 
  - _class_loader 即类加载器 
  - _vtable 虚方法表 
  - _itable 接口方法表
- 如果这个类还有父类没有加载，先加载父类
- **加载**和**连接**可能是交替运行的

**注意：**

- instanceKlass 这样的【元数据】是存储在方法区（1.8 后的元空间内），但 _java_mirror 是存储在堆中
- 可以通过前面介绍的 HSDB 工具查看

![image-20230807104845967](https://img.enndfp.cn/image-20230807104845967.png)

#### 2.2 验证

验证类是否符合 JVM规范，安全性检查，大致分为以下四个阶段：

- 文件格式验证
- 元数据验证
- 字节码验证
- 符号引用验证

#### 2.3 准备

为static变量分配空间，设置默认值

- static 变量在 JDK 7 之前存储于 instanceKlass 末尾，从 JDK 7 开始，存储于 _java_mirror 末尾
- static 变量分配空间和赋值是两个步骤，分配空间在准备阶段完成，赋值在初始化阶段完成
- 如果 static 变量是 final 的基本类型，以及字符串常量，那么编译阶段值就确定了，赋值在准备阶段完成
- 如果 static 变量是 final 的，但属于引用类型（new），那么赋值也会在初始化阶段完成

#### 2.4 解析

将常量池中的符号引用解析为直接引用

```java
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
```

#### 2.5 初始化

`<clinit>()`方法

初始化即调用`<clinit>()` ，虚拟机会保证这个类的『构造方法』的线程安全

##### 2.5.1 会发生初始化

- main 方法所在的类，总会被首先初始化
- 首次访问这个类的静态变量或静态方法时
- 子类初始化，如果父类还没初始化，会引发
- 子类访问父类的静态变量，只会触发父类的初始化
- Class.forName
- new 会导致初始化

##### 2.5.2 不会发生初始化

- 访问类的 static final 静态常量（基本类型和字符串）不会触发初始化
- 类对象.class 不会触发初始化
- 创建该类的数组不会触发初始化
- 类加载器的 loadClass 方法
- Class.forName 的参数 2 为 false 时

实验

```java
class A {
    static int a = 0;

    static {
        System.out.println("a init");
    }
}

class B extends A {
    final static double b = 5.0;
    static boolean c = false;

    static {
        System.out.println("b init");
    }
}
```

验证（实验时请先全部注释，每次只执行其中一个）

```java
public class Demo2 {
    static {
        System.out.println("main init");
    }

    public static void main(String[] args) throws ClassNotFoundException {
        // 1. 静态常量（基本类型和字符串）不会触发初始化
        System.out.println(B.b);
        // 2. 类对象.class 不会触发初始化
        System.out.println(B.class);
        // 3. 创建该类的数组不会触发初始化
        System.out.println(new B[0]);
        // 4. 不会初始化类 B，但会加载 B、A
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        cl.loadClass("com.enndfp.class_loader_test.B");
        // 5. 不会初始化类 B，但会加载 B、A
        ClassLoader c2 = Thread.currentThread().getContextClassLoader();
        Class.forName("com.enndfp.class_loader_test.B", false, c2);

        // 1. 首次访问这个类的静态变量或静态方法时
        System.out.println(A.a);
        // 2. 子类初始化，如果父类还没初始化，会引发
        System.out.println(B.c);
        // 3. 子类访问父类静态变量，只触发父类初始化
        System.out.println(B.a);
        // 4. 会初始化类 B，并先初始化类 A
        Class.forName("com.enndfp.class_loader_test.B");
    }
}
```

### 3.类加载器

#### 3.1 基本介绍

Java虚拟机设计团队有意把类加载阶段中的“通过一个类的**全限定名**来获取描述该类的**二进制字节流**”**这个动作放到 Java 虚拟机外部去实现，以便让应用程序自己决定如何去获取所需的类。实现这个动作的代码**被称为“类加载器”（**Class Loader**）

**类与类加载器**

类加载器虽然只用于实现类的加载动作，但它在 Java 程序中起到的作用却远超类加载阶段。

对于任意一个类，都必须由加载它的**类加载器**和这个**类本身**一起共同确立其在 Java 虚拟机中的唯一性，每一个类加载器，都拥有一个独立的类名称空间。这句话可以表达得更通俗一些：**比较两个类是否“相等”，只有在这两个类是由同一个类加载器加载的前提下才有意义**，否则，即使这两个类来源于同一个 Class文件，被同一个Java虚拟机加载，只要加载它们的类加载器不同，那这两个类就必定不相等。

| 名称                    | 加载哪的类            | 说明                          |
| ----------------------- | --------------------- | ----------------------------- |
| Bootstrap ClassLoader   | JAVA_HOME/jre/lib     | 无法直接访问                  |
| Extension ClassLoader   | JAVA_HOME/jre/lib/ext | 上级为 Bootstrap，显示为 null |
| Application ClassLoader | classpath             | 上级为 Extension              |
| 自定义类加载器          | 自定义                | 上级为 Application            |

**<mark>第一遍自下而上询问有没有加载，第二遍自上而下看自己加载目录有没有，如果有，则加载；没有，则看下一级加载器</mark>**

#### 3.2 启动类加载器

用 Bootstrap 类加载器加载类：

```java
package com.enndfp.class_loader_test;

public class F {
    static {
        System.out.println("bootstrap F init");
    }
}
```

执行

```java
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
```

输出

![image-20230807195058751](https://img.enndfp.cn/image-20230807195058751.png)

- -Xbootclasspath 表示设置 bootclasspath
- 其中 /a:. 表示将当前目录追加至 bootclasspath 之后
- 可以用这个办法替换核心类
  - `java -Xbootclasspath:<new bootclasspath>`
  - `java -Xbootclasspath/a:<追加路径>`
  - `java -Xbootclasspath/p:<追加路径>`

#### 3.3 扩展类加载器

```java
package com.enndfp.class_loader_test;

public class G {
    static {
        System.out.println("classpath G init");
    }
}
```

执行

```java
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
```

输出

![image-20230807200524225](https://img.enndfp.cn/image-20230807200524225.png)

写一个同名的类

```java
package com.enndfp.class_loader_test;

public class G {
    static {
        System.out.println("ext G init");
    }
}
```

打个 jar 包

![image-20230807200818538](https://img.enndfp.cn/image-20230807200818538.png)

将 jar 包拷贝到 JAVA_HOME/jre/lib/ext

重新执行 Demo4 

输出

![image-20230807201425883](https://img.enndfp.cn/image-20230807201425883.png)

#### 3.4 双亲委派模式

**工作流程：**

<mark>如果一个类加载器收到了类加载的请求，它首先不会自己去尝试加载这个类，而是把这个请求委派给父类加载器去完成，每一个层次的类加载器都是如此，因此所有的加载请求最终都应该传送到最顶层的启动类加载器中，只有当父加载器反馈自己无法完成这个加载请求（它的搜索范围中没有找到所需的类）时，子加载器才会尝试自己去完成加载</mark>

所谓的双亲委派，就是指调用类加载器的 **loadClass** 方法时，查找类的规则

> **注意**
>
> 这里的双亲，翻译为上级似乎更为合适，因为它们并没有继承关系

```java
protected Class<?> loadClass(String name, boolean resolve)
        throws ClassNotFoundException
    {
        synchronized (getClassLoadingLock(name)) {
            // 1. 检查该类是否已经加载
            Class<?> c = findLoadedClass(name);
            if (c == null) {
                long t0 = System.nanoTime();
                try {
                    if (parent != null) {
                        // 2. 有上级的话，委派上级 loadClass
                        c = parent.loadClass(name, false);
                    } else {
                        // 3. 如果没有上级了（ExtClassLoader），则委派BootstrapClassLoader
                        c = findBootstrapClassOrNull(name);
                    }
                } catch (ClassNotFoundException e) {
                    //若找不到，抛出ClassNotFoundException异常
                }

                if (c == null) {
                    long t1 = System.nanoTime();
                    // 4. 每一层找不到，调用 findClass 方法（每个类加载器自己扩展）来加载
                    c = findClass(name);

                    // 5. 记录耗时
                    sun.misc.PerfCounter.getParentDelegationTime().addTime(t1 - t0);
                    sun.misc.PerfCounter.getFindClassTime().addElapsedTimeFrom(t1);
                    sun.misc.PerfCounter.getFindClasses().increment();
                }
            }
            if (resolve) {
                resolveClass(c);
            }
            return c;
        }
    }
```

例如：

```java
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
```

执行流程为：

1. `sun.misc.Launcher$AppClassLoader`  //1 处，开始查看已加载的类，结果没有
2.  `sun.misc.Launcher$AppClassLoader` // 2 处，委派上级

​        `sun.misc.Launcher$ExtClassLoader.loadClass()`

3. `sun.misc.Launcher$ExtClassLoader` // 1 处，查看已加载的类，结果没有
4. `sun.misc.Launcher$ExtClassLoader` // 3 处，没有上级了，则委派 `BootstrapClassLoader` 查找
5. `BootstrapClassLoader` 是在 JAVA_HOME/jre/lib 下找 H 这个类，显然没有
6. `sun.misc.Launcher$ExtClassLoader` // 4 处，调用自己的 findClass 方法，是在 JAVA_HOME/jre/lib/ext 下找 H 这个类，显然没有，回到 `sun.misc.Launcher$AppClassLoader` 的 // 2 处
7. 继续执行到 `sun.misc.Launcher$AppClassLoader` // 4 处，调用它自己的 findClass 方法，在 classpath 下查找，找到了
