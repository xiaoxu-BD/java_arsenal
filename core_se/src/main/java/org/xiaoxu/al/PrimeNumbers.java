package org.xiaoxu.al;

public class PrimeNumbers {

    public static void main(String[] args) {
        int count = 0; // 用于统计质数的数量
        for (int i = 101; i <= 200; i++) {
            if (isPrime(i)) {
                System.out.println(i); // 输出质数
                count++;
            }
        }
        System.out.println("Total prime numbers between 101 and 200: " + count);
        
    }

    public static boolean isPrime(int n) {
        // 0和1不是质数，小于0的数也不是质数
        if (n <= 1) {
            return false;
        }
        // 检查从2到n的平方根是否有因子
        for (int i = 2; i <= Math.sqrt(n); i++) {
            // 如果n能被i整除，说明n不是质数
            if (n % i == 0) {
                return false;
            }
        }
        // 如果没有找到任何因子，说明n是质数
        return true;
    }




}