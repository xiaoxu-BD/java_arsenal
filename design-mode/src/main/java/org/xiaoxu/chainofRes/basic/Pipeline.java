package org.xiaoxu.chainofRes.basic;

import java.util.ArrayList;
import java.util.List;


// 管道负责任务流程执行
public class Pipeline {

    List<Handler> handlers = new ArrayList<>();



    public Pipeline add(Handler handler){
        handlers.add(handler);
        return this;
    }

    public void execute(Context ctx){

        for (Handler h : handlers) {
            if (ctx.isStopped()) break;
            h.handle(ctx);
        }


    }





}
