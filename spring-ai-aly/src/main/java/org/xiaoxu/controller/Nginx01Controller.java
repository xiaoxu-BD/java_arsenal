package org.xiaoxu.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class Nginx01Controller {


    @GetMapping("/api/nginx01")
    public ResponseEntity<Map<String,String>> nginx01(){
        HashMap<String, String> map = new HashMap<>();
        map.put("K1","等值匹配");
        map.put("K2","前缀匹配");
        map.put("K3","正则不区分大小写匹配");
        map.put("K4","正则区分大小写匹配");
        map.put("K5","兜底匹配");

        return  ResponseEntity.ok(map);
    }
}
