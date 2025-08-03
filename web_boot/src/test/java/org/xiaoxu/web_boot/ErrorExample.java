package org.xiaoxu.web_boot;

/**
 * @className: ErrorExample
 * @author: xiaoxu
 * @date: 2025/7/28 8:06
 * @Version: 1.0
 * @description: 错误示例 分析背后的问题;
 */
public class ErrorExample {
//        @Transactional(rollbackFor = Exception.class)
//        public void ModifyHistoryData() {
//
//                List<ItoSapInvoiceData> invoiceCustomerNoList = invoiceDataSourceService.getInvoiceCustomerNoListPage();
//                if (invoiceCustomerNoList == null || invoiceCustomerNoList.isEmpty()) {
//                    log.info("invoiceCustomerNoList为空:{}",invoiceCustomerNoList);
//
//                }
//                Map<String, String> cohToCustomerMap = invoiceCustomerNoList.stream().filter(Objects::nonNull).filter(data -> StringUtils.isNotEmpty(data.getCohOrderId()) && StringUtils.isNotEmpty(data.getCohLineNo())).collect(Collectors.toMap(
//                        data -> data.getCohOrderId() + "_" + data.getCohLineNo(),
//                        ItoSapInvoiceData::getCustomerNo,
//                        (v1, v2) -> v1
//                ));
//                // 三个任务并发执行
//                CompletableFuture<Void> task1 = CompletableFuture.runAsync(() -> handleInvoiceData(cohToCustomerMap), executor);
//                CompletableFuture<Void> task2 = CompletableFuture.runAsync(() -> handleOrderThird(cohToCustomerMap), executor);
//                CompletableFuture<Void> task3 = CompletableFuture.runAsync(() -> handleOrderList(cohToCustomerMap), executor);
//                // 等待所有任务完成
//                CompletableFuture.allOf(task1, task2, task3).join();
//
//        }
//
//
//        private void handleInvoiceData(Map<String, String> cohToCustomerMap) {
//            PageHandler<ItoSapInvoiceData> handler = new PageHandler<>();
//            handler.handleByPage(1000,
//                    (pageNo, pageSize) -> {
//                        PageHelper.startPage(pageNo, pageSize);
//                        List<String> customerNoList = cohToCustomerMap.values().stream().collect(Collectors.toList());
//                        List<ItoSapInvoiceData> list = dataServiceImpl.selectCohIdAndLineNo(customerNoList);
//                        if (list.isEmpty()) {
//                            return new Page<ItoSapInvoiceData>(pageNo, pageSize);
//                        }
//                        Page<ItoSapInvoiceData> page = new Page<>(pageNo, pageSize);
//                        page.addAll(list);
//                        return page;
//                    },
//                    (records) -> {
//                        if (records.isEmpty()) {
//                            return;
//                        }
//                        List<ItoSapInvoiceData> updateList = new ArrayList<>();
//                            updateList.addAll(records);
//                            dataServiceImpl.updateBatch(updateList);
//
//                    });
//        }
//
//        private void handleOrderThird(Map<String, String> cohToCustomerMap) {
//            PageHandler<ItoOrderRpaReturnOrderThird> pageHandler = new PageHandler<>();
//            pageHandler.handleByPage(1000,
//                    (pageNo, pageSize) -> {
//                        PageHelper.startPage(pageNo, pageSize);
//                        List<String> customerNoList = cohToCustomerMap.values().stream().collect(Collectors.toList());
//                        List<ItoOrderRpaReturnOrderThird> thirdList = thirdService.selectByCustomerNoWithCoh(customerNoList);
//                        if (thirdList.isEmpty()) {
//                            return new Page<ItoOrderRpaReturnOrderThird>(pageNo, pageSize);
//                        }
//                        Page<ItoOrderRpaReturnOrderThird> page2 = new Page<>(pageNo, pageSize);
//                        page2.addAll(thirdList);
//                        return page2;
//                    },
//                    (records) -> {
//                        if (records.isEmpty()) {
//                            return;
//                        }
//                        List<ItoOrderRpaReturnOrderThird> updateList = new ArrayList<>();
//                        updateList.addAll(records);
//                        thirdService.updateBatchCustomerNo(updateList);
//                    });
//        }
//
//        private void handleOrderList(Map<String, String> cohToCustomerMap){
//            PageHandler<ItoOrderRpaReturnOrderList> pageHandlerOrderList = new PageHandler<>();
//            pageHandlerOrderList.handleByPage(1000,
//                    (pageNo, pageSize) -> {
//                        PageHelper.startPage(pageNo, pageSize);
//                        List<String> customerNoList = cohToCustomerMap.values().stream().collect(Collectors.toList());
//                        List<ItoOrderRpaReturnOrderList> orderLists = orderListService.selectByCustomerNoContact(customerNoList);
//                        if (orderLists.isEmpty()) {
//                            return new Page<ItoOrderRpaReturnOrderList>(pageNo, pageSize);
//                        }
//                        Page<ItoOrderRpaReturnOrderList> page3 = new Page<>(pageNo, pageSize);
//                        page3.addAll(orderLists);
//                        return page3;
//                    },
//                    (records) -> {
//                        if (records.isEmpty()) {
//                            return;
//                        }
//                        List<ItoOrderRpaReturnOrderList> updateList = new ArrayList<>();
//                        updateList.addAll(records);
//                        orderListService.updateBatch(updateList);
//                    });
//        }
}
