package org.xiaoxu.exceldemo.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.xiaoxu.exceldemo.service.DataGenService;

@RestController
@RequestMapping("/api/data")
public class DataGenController {

    private final DataGenService dataGenService;

    public DataGenController(DataGenService dataGenService) {
        this.dataGenService = dataGenService;
    }

    /** 生成成功返回插入行数 */
    @PostMapping("/generate")
    public long generate(@RequestParam(defaultValue = "100000") int count) {
        return dataGenService.generate(count);
    }
}
