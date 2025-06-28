package org.xiaoxu.web_boot.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.handler.WriteHandler;
import com.alibaba.excel.write.metadata.WriteSheet;
import org.apache.commons.lang3.StringUtils;
import org.xiaoxu.web_boot.strategy.ExcelExportAutoWidthStrategy;

import java.io.File;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @className: ExcelUtils
 * @author: xiaoxu
 * @date: 2025/6/28 19:37
 * @Version: 1.0
 * @description:
 */
public class ExcelUtils {
    private static final String TEMP_DIR = SpringUtil.getProperty("tempfile.dir");


    /**
     * 导出开启任务
     * @return
     */

    /**
     * excel导出
     * @return
     */
    public static <T> String export(String workId, String title, String workName, Map<String, List<String>> fieldMap, Function<Integer, List<T>> getData){
        // 处理表头
        List<List<String>> heads=new ArrayList<>();
        List<String> fieldNames=new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : fieldMap.entrySet()) {
            heads.add(entry.getValue());
            fieldNames.add(entry.getKey());
        }
        //导出
        exportBig(title,workName,getData,heads,fieldNames,workId,new ExcelExportAutoWidthStrategy());
        return workId;
    }

    public static <T> void exportBig(String title,String workName, Function<Integer, List<T>> getData, List<List<String>> heads, List<String> fieldNames,String workId, WriteHandler... writeHandlers) {
        title = StringUtils.isBlank(title)?workName:title;
        String fileName= StrUtil.format("{}_{}", title, IdUtil.fastSimpleUUID()) + ".xlsx";
        File file = Paths.get(TEMP_DIR, fileName).toFile();
        // 第一个writeHandler为样式配置
        ExcelWriter excelWriter = null;
        try {
            ExcelWriterBuilder builder = EasyExcelFactory.write(file).head(heads);
            builder.registerWriteHandler(ExcelStyleHandler.defaultStyleHandler());
            if (ArrayUtil.isNotEmpty(writeHandlers)) {
                for (WriteHandler writeHandler : writeHandlers) {
                    builder.registerWriteHandler(writeHandler);
                }
            }
            excelWriter = builder.build();
            WriteSheet writeSheet = EasyExcelFactory.writerSheet(workName).build();
            int page = 1;
            List<T> datas = null;
            boolean isEnd = false;
            do {
                datas = getData.apply(page);
                if (CollUtil.isEmpty(datas)) {
                    datas = new ArrayList<>();
                    isEnd = true;
                }
                excelWriter.write(parseToExportData(datas, fieldNames), writeSheet);
                if (isEnd) {
                    break;
                }
                page++;
            } while (CollUtil.isNotEmpty(datas));
        } finally {
            if (excelWriter != null) {
                excelWriter.close();
            }
//            saveExportFile(workId,fileName,file);
        }
    }

//    /**
//     * 导出文件存放
//     */
//    private static void saveExportFile(String workId, String fileName, File file) {
//        String url = UploadUtil.uploadToS3(file, FILE_CONFIG.getKeys() + fileName, FILE_CONFIG.getBucketName());
//
//        //上传完成删除
//        FileUtil.deleteFile(file);
//
//        ItoBusinessWorkEntity businessWorkEntity = new ItoBusinessWorkEntity();
//        businessWorkEntity.setId(workId);
//        businessWorkEntity.setProgress(100);
//        businessWorkEntity.setFileUrl(url);
//        businessWorkEntity.preUpdate();
//        SYSTEM_CLIENT.updateBusinessWork(businessWorkEntity);
//    }


    private static <T> List<List<String>> parseToExportData(List<T> datas, List<String> fieldNames) {
        List<List<String>> rowDatas = new ArrayList<>();
        if (CollUtil.isEmpty(datas)) {
            return rowDatas;
        }
        for (T data : datas) {
            List<String> rowData = new ArrayList<>();
            for (String fieldName : fieldNames) {
                if (!ReflectUtil.hasField(data.getClass(), fieldName)) {
                    rowData.add(StrUtil.EMPTY);
                    continue;
                }
                Object fieldValue = ReflectUtil.getFieldValue(data, fieldName);
                if (fieldValue == null) {
                    rowData.add(StrUtil.EMPTY);
                } else if(fieldValue instanceof LocalDateTime){
                    rowData.add(((LocalDateTime)fieldValue).format(DateTimeFormatter.ofPattern(DatePattern.NORM_DATETIME_PATTERN)));
                } else if (fieldValue instanceof LocalDate) {
                    rowData.add(((LocalDate) fieldValue).format(DateTimeFormatter.ofPattern(DatePattern.NORM_DATE_PATTERN)));
                } else {
                    rowData.add(fieldValue.toString());
                }
            }
            rowDatas.add(rowData);
        }
        return rowDatas;
    }
}







