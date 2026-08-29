package org.xiaoxu.exceldemo.handler;

import org.apache.fesod.sheet.write.handler.SheetWriteHandler;
import org.apache.fesod.sheet.write.metadata.holder.WriteSheetHolder;
import org.apache.fesod.sheet.write.metadata.holder.WriteWorkbookHolder;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.xiaoxu.exceldemo.dto.UserImportRow;

/**
 * 给模板的部门列(第 4 列, 0 基下标 3)加下拉框, 减少导入时的枚举校验失败。
 * 显式列表有 255 字符总长上限, 选项多的场景要换成隐藏字典 sheet + 公式引用。
 */
public class DeptDropdownSheetHandler implements SheetWriteHandler {

    /** 下拉覆盖到第 10002 行(0 基 10001), 前两行是两级表头 */
    private static final int LAST_DROPDOWN_ROW = 10001;

    /**
     * “sheet 刚创建好、数据还没写”这个时机被调用，参数里能拿到原生 POI 的 Sheet 对象
     */

    @Override
    public void afterSheetCreate(WriteWorkbookHolder workbookHolder, WriteSheetHolder sheetHolder) {
        Sheet sheet = sheetHolder.getSheet();
        DataValidationHelper helper = sheet.getDataValidationHelper();
        DataValidationConstraint constraint = helper.createExplicitListConstraint(
                UserImportRow.DEPARTMENTS.toArray(new String[0]));
        CellRangeAddressList range = new CellRangeAddressList(2, LAST_DROPDOWN_ROW, 3, 3);
        DataValidation validation = helper.createValidation(constraint, range);
        validation.setShowErrorBox(true);
        sheet.addValidationData(validation);
    }
}
