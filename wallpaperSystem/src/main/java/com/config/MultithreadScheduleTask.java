package com.config;

import com.AppConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDateTime;

//@Component注解用于对那些比较中立的类进行注释；
//相对与在持久层、业务层和控制层分别采用 @Repository、@Service 和 @Controller 对分层中的类进行注释
@Component
@EnableScheduling   // 1.开启定时任务
@EnableAsync        // 2.开启多线程
public class MultithreadScheduleTask {
    @Autowired
    private AppConstants appConstants;

    @Async
    @Scheduled(fixedDelay = 1000 * 60 * 60 * 4)  //间隔4小时
    public void clearTempFileTask() {
        System.out.println("清理临时文件任务开始( " + LocalDateTime.now().toLocalTime()+")");
        String path = appConstants.getPath();
        File file = new File(path);
        for (File file1 : file.listFiles()){
            file1.delete();
        }
    }

}
