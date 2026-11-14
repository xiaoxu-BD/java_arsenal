package org.xiaoxu.wecommail.Controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xiaoxu.wecommail.dto.UserDashboardVO;
import org.xiaoxu.wecommail.service.MockService;

@RestController
@RequestMapping("/get")
public class SupplyController {




    @Autowired
    private MockService futureService;


    @GetMapping("/futureResult")
    public UserDashboardVO futureResult(){
       return
               futureService.getUserDashboard(1L);
    }



    @GetMapping("/chain")
    public String chainSuccess(String name){

        futureService.processOne(name);

        return  "success";
    }
}
