package org.xiaoxu.al;

import java.util.Arrays;

/**
 * @className: MergeArray
 * @author: xiaoxu
 * @date: 2025/8/5 21:46
 * @Version: 1.0
 * @description: 合并两个有序数组8.5
 */
public class MergeArray {



    int [] array1 = new int[]{1,2,3,0,0,0};

    int [] array2 = new int[]{2,3,5};

    public void merge(int[] nums1, int m, int[] nums2, int n) {
        for (int i = 0; i != n; ++i) {
            nums1[m + i] = nums2[i];
        }
        Arrays.sort(nums1);

        System.out.println(Arrays.toString(nums1));
    }

    public static void main(String[] args) {
        MergeArray mergeArray = new MergeArray();
        mergeArray.merge(mergeArray.array1,3,mergeArray.array2,3);

    }
}
