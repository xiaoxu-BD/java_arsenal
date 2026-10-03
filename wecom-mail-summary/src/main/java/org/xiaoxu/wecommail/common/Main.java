package org.xiaoxu.wecommail.common;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

@Slf4j
public class Main {
    public static void main(String[] args) {

                // 1. 模拟当前页的数据列表（第 2 页，共 3 条）
                List<String> userList = Arrays.asList("张三", "李四", "王五");

                // 2. 模拟总记录数
                int total = 7;
                int pageSize = 3;
                int currentPage = 2;

                // 3. 调用 PageResponse.of() 构造分页响应
                PageResponse<String> pageResponse = PageResponse.of(userList, total, pageSize, currentPage);

                // 4. 打印结果
                System.out.println("success      = " + pageResponse.getSuccess());
                System.out.println("dataList     = " + pageResponse.getDataList());
                System.out.println("total        = " + pageResponse.getTotal());
                System.out.println("pageSize     = " + pageResponse.getPageSize());
                System.out.println("currentPage  = " + pageResponse.getCurrentPage());
                System.out.println("totalPage    = " + pageResponse.getTotalPage());


                log.info("responseList is :{}",pageResponse);
            }
        }
