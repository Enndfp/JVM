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

#### 3.2 注意

**程序计数器和栈都是线程私有的**

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
- `ByteBuffer`的实现类内部，使用了`Cleaner` (虚引用) 来监测`ByteBuffer`对象，一旦`ByteBuffer`对象被垃圾回收，那么就会由`ReferenceHandler`线程通过`Cleaner`的`create`方法调用`freeMemory`来释放直接内存

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

