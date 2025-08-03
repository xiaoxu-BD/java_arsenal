package org.xiaoxu;

import cn.hutool.core.util.StrUtil;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

public class CompletableFutureTest  extends TestCase {

    public void testFuture(){
        CompletableFuture.supplyAsync(() -> {
            throw new RuntimeException("出错了！");
        }).exceptionally(throwable -> {
            System.err.println("捕获到异常：" + throwable.getMessage());
            return 3232; // 提供默认值
        }).thenAccept(System.out::println);
    }

    public void testThreadPoolExecutor(){
    }


//    public void testCompletableFuture(){
//        List<CompletableFuture<Void>> futures = new ArrayList<>();
//        for (ItoQuotationItemForSql item : items) {
//            if (StringUtils.isBlank(item.getSkfProductDsgn())) continue;
//            String fddsgn = item.getSkfProductDsgn();
//            if ("9757".equals(item.getQuotationOuc()) && (StrUtil.isNotEmpty(fddsgn) && fddsgn.startsWith("SE.C"))) {
//                abasProductList.add(fddsgn);
//                continue;
//            }
//            // 创建异步任务
//            CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {
//                ItoProductQueryDto dto = new ItoProductQueryDto();
//                dto.setSkfProductDsgn(item.getSkfProductDsgn());
//                dto.setOuc(itoQuotationQueryDto.getOuc());
//                dto.setPackageCode(item.getPackageCode());
//                return productClient.getProductCacheBySkfProductDsgnAndPackageCode(dto.getSkfProductDsgn(), dto.getPackageCode(), dto.getOuc());
//            }, executor).thenAcceptAsync(productDto -> {
//                if (productDto == null) {
//                    String tip = String.format("产品型号：%s，包装代码：%s（请联系：Eling Chen,经销商用户请联系对应客服人员咨询详情）",
//                            item.getSkfProductDsgn(),
//                            item.getPackageCode());
//                    invalidTips.add(tip);
//                    return;
//                }
//                String productStatus = productDto.getFdavcd();
//                String statusLocal = productDto.getA3avcd();
//                ItoSnowflakeAllProduct allProduct = Optional.ofNullable(productTable.get(item.getSkfProductDsgn(), item.getPackageCode())).orElse(null);
//                String globalStatus = Optional.ofNullable(allProduct).map(ItoSnowflakeAllProduct::getCrossPlantMaterialStatus).orElse(null);
//                boolean isLocalValid = StringUtils.isNotBlank(statusLocal) && allowedProductStatus.contains(statusLocal);
//                boolean isFactoryValid = StringUtils.isNotBlank(productStatus) && allowedProductStatus.contains(productStatus);
//                boolean isGlobalValid = StringUtils.isNotBlank(globalStatus) && allowedProductStatus.contains(globalStatus);
//                if ("56".equals(statusLocal)) {
//                    String tip = String.format("产品型号：%s，包装代码：%s（请联系：关务 Rosie Sun,经销商用户请联系对应客服人员咨询详情）",
//                            item.getSkfProductDsgn(),
//                            item.getPackageCode());
//                    invalidTips.add(tip);
//                } else if (!isLocalValid || !isGlobalValid || !isFactoryValid) {
//                    String tip = String.format("产品型号：%s，包装代码：%s（请联系：Product Owner激活,经销商用户请联系对应客服人员咨询详情）",
//                            item.getSkfProductDsgn(),
//                            item.getPackageCode());
//                    invalidTips.add(tip);
//                }
//            }, executor).exceptionally(throwable -> {
//                // 捕获 supplyAsync 或 thenAccept 中的异常
//                String tip = String.format("产品型号：%s，包装代码：%s（处理失败，异常信息：%s）",
//                        item.getSkfProductDsgn(),
//                        item.getPackageCode(),
//                        throwable.getCause() != null ? throwable.getCause().getMessage() : throwable.getMessage());
//                invalidTips.add(tip);
//                return null; // 继续完成链式操作
//            });
//            futures.add(future);
//        }
//
//// 等待所有任务完成
//        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
//        try {
//            allOf.join(); // 等待所有任务完成
//            // 检查每个 future 是否异常完成
//            for (CompletableFuture<Void> future : futures) {
//                if (future.isCompletedExceptionally()) {
//                    try {
//                        future.join(); // 触发异常抛出
//                    } catch (CompletionException e) {
//                        // 异常已通过 exceptionally 处理，这里仅记录日志
//                        System.err.println("Future 异常：" + e.getCause());
//                    }
//                }
//            }
//        } catch (Exception e) {
//            // 捕获 allOf.join() 的异常（通常不会触发，但以防万一）
//            System.err.println("allOf 异常：" + e.getMessage());
//        }
//    }


    public void testAtomic(){
        AtomicInteger atomicInteger = new AtomicInteger(0);

        for (int i  = 0;  i < 10000 ; i++) {
            new Thread(atomicInteger::incrementAndGet).start();
        }
        // 等待线程结束后输出
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
        System.out.println("最终结果：" + atomicInteger.get());
    }
}
