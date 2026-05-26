package org.xiaoxu.se.foundation;

import java.io.*;

/**
 * Java 异常体系
 *
 * Throwable
 * ├── Error（系统级错误，不要 catch）
 * │   ├── OutOfMemoryError
 * │   └── StackOverflowError
 * └── Exception（程序可处理的异常）
 *     ├── Checked Exception（编译期强制要求处理）
 *     │   ├── IOException
 *     │   ├── SQLException
 *     │   └── ClassNotFoundException
 *     └── RuntimeException（Unchecked，运行时才暴露）
 *         ├── NullPointerException
 *         ├── ArrayIndexOutOfBoundsException
 *         ├── ClassCastException
 *         └── IllegalArgumentException
 */
public class ThrowableDEM {
    public static void main(String[] args) {

        // 1. try-catch-finally 基本用法
//         tryCatchFinallyDemo();

        // 2. Checked vs Unchecked
//         checkedVsUncheckedDemo();

        // 3. finally 的坑：什么情况不执行
//         finallyTrapDemo();

        // 4. 异常链：包装底层异常
        // exceptionChainDemo();

        // 5. try-with-resources（自动关闭资源）
//        tryWithResourcesDemo();

        // 6. 自定义异常
         customExceptionDemo();
    }

    // ==================== 1. try-catch-finally ====================
    private static void tryCatchFinallyDemo() {
        System.out.println("===== 1. try-catch-finally =====");

        // 基本结构
        try {
            int result = 10 / 0;
            System.out.println("这行不会执行");
        } catch (ArithmeticException e) {
            // 只捕获算术异常
            System.out.println("捕获异常: " + e.getMessage());  // / by zero
        } finally {
            System.out.println("finally 一定执行");
        }

        // 多 catch（从具体到宽泛，子类必须在父类前面）
        try {
            String s = null;
            s.length();
        } catch (NullPointerException e) {
            System.out.println("\n空指针: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("其他异常: " + e.getMessage());
        }

        // Java 7+ 多异常合并写法
        try {
            Object obj = "hello";
            Integer num = (Integer) obj;
        } catch (ClassCastException | NumberFormatException e) {
            System.out.println("\n类型/格式异常: " + e.getClass().getSimpleName());
        }
    }

    // ==================== 2. Checked vs Unchecked ====================
    private static void checkedVsUncheckedDemo() {
        System.out.println("===== 2. Checked vs Unchecked =====");

        // Unchecked（RuntimeException）：编译器不管你处不处理
        // int[] arr = new int[3];
        // arr[5];  // ArrayIndexOutOfBoundsException — 编译通过，运行炸

        // Checked（非 RuntimeException 的 Exception）：编译器强制要求处理
        // 以下两种方式任选其一：

        // 方式一：try-catch 自己处理
        try {
            readFile("不存在的文件.txt");
        } catch (IOException e) {
            System.out.println("自己处理: " + e.getMessage());
        }

        // 方式二：throws 抛给调用者处理（main 也 throws 的话最终交给 JVM）
        // readFile("不存在的文件.txt");  // 编译报错：Unhandled exception
    }

    // throws 声明：告诉调用者"我可能会抛 IOException，你来处理"
    private static void readFile(String path) throws IOException {
        new FileInputStream(path);  // FileInputStream 构造器 throws FileNotFoundException
    }

    // ==================== 3. finally 的坑 ====================
    private static void finallyTrapDemo() {
        System.out.println("===== 3. finally 的坑 =====");

        // 坑一：finally 里 return 会吞掉 try/catch 的返回值
        System.out.println("吞返回值: " + swallowReturn());  // 20，不是 10

        // 坑二：System.exit() 之后 finally 不执行
        try {
            System.out.println("即将 exit...");
            // System.exit(0);  // 取消注释可验证：finally 不会执行
        } finally {
            System.out.println("finally 执行了（如果上面 exit 则不会打印）");
        }

        // 坑三：finally 对返回值的影响 — 基本类型值不变，但引用类型内部状态可变
        int[] arr = {1};
        System.out.println("引用类型改值: " + modifyInFinally(arr));  // 100
        System.out.println("原始数组: " + arr[0]);  // 100，被改了
    }

    private static int swallowReturn() {
        try {
            return 10;
        } finally {
            return 20;  // 覆盖了 try 的 return 10
        }
    }

    private static int modifyInFinally(int[] arr) {
        try {
            return arr[0];  // 准备返回 1
        } finally {
            arr[0] = 100;  // 把原数组改了
            // return 的值已经确定为 1（基本类型），但数组内容被改了
        }
    }

    // ==================== 4. 异常链 ====================
    private static void exceptionChainDemo() {
        System.out.println("===== 4. 异常链 =====");
        try {
            businessMethod();
        } catch (BusinessException e) {
            System.out.println("业务异常: " + e.getMessage());
            System.out.println("根本原因: " + e.getCause().getMessage());
            e.printStackTrace();  // 打印完整链路
        }
    }

    private static void businessMethod() {
        try {
            int result = 10 / 0;
        } catch (ArithmeticException e) {
            // 包装底层异常，往上抛业务含义更明确的异常
            throw new BusinessException("计算订单金额失败", e);
        }
    }

    // ==================== 5. try-with-resources ====================
    private static void tryWithResourcesDemo() {
        System.out.println("===== 5. try-with-resources =====");

        // 旧写法：finally 里手动 close，嵌套很丑
        // BufferedReader br = null;
        // try {
        //     br = new BufferedReader(new FileReader("test.txt"));
        // } finally {
        //     if (br != null) br.close();  // close 也可能抛异常...
        // }

        // Java 7+ 写法：实现了 AutoCloseable 的资源自动关闭
        String file = "auto_close_test.txt";
        try {
            // 先写文件
            try (FileWriter fw = new FileWriter(file)) {
                fw.write("Hello try-with-resources");
            }  // 这里自动调用 fw.close()，即使写入抛异常也会 close

            // 再读文件
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                System.out.println("读取内容: " + br.readLine());
            }  // 自动关闭 br
            System.out.println("文件已自动关闭，无需 finally");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            new File(file).delete();
        }

        // 多资源一起声明
        // try (FileInputStream in = new FileInputStream("a.txt");
        //      FileOutputStream out = new FileOutputStream("b.txt")) {
        //     // ...
        // }
    }

    // ==================== 6. 自定义异常 ====================
    private static void customExceptionDemo() {
        System.out.println("===== 6. 自定义异常 =====");

        try {
            login("", "123");
        } catch (BusinessException e) {
            System.out.println("捕获业务异常: " + e.getMessage());
        }

        try {
            login("admin", "");
        } catch (BusinessException e) {
            System.out.println("捕获业务异常: " + e.getMessage());
        }
    }

    private static void login(String username, String password) {
        if (username == null || username.isEmpty()) {
            throw new BusinessException("用户名不能为空");
        }
        if (password == null || password.isEmpty()) {
            throw new BusinessException("密码不能为空");
        }
        System.out.println("登录成功: " + username);
    }
}

// 自定义业务异常 — 继承 RuntimeException（unchecked），调用者不强制 catch
class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    // 支持异常链
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
