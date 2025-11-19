package org.xiaoxu;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.xiaoxu.component.TaskService2;

/**
 * Hello world!
 *
 */
@SpringBootApplication
public class PracticeServiceApplication implements CommandLineRunner
{

//    @Autowired
//    private MyService myService;

    @Autowired
    private ApplicationContext applicationContext;


    @Autowired
    private TaskService2 taskService;

    public static void main( String[] args )
    {
        SpringApplication.run(PracticeServiceApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        System.out.println("test proxy");

        System.out.println("type is 类型" + taskService.getClass().getName());
        taskService.executeTask();;

        System.out.println("is over");


    }
}
