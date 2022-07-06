package com.index.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author YanZhao
 * @description
 * @date 2022年07月04日 20:42
 */

@RestController
public class IndexController {

    @RequestMapping("/")
    public String hello(){
        return "Hello, " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

}
