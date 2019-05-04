package com.seen.seckillbackend.controller;

import com.seen.seckillbackend.domain.Goods;
import com.seen.seckillbackend.domain.User;
import com.seen.seckillbackend.rabbitmq.MQSender;
import com.seen.seckillbackend.rabbitmq.SeckillMessage;
import com.seen.seckillbackend.redis.RedisService;
import com.seen.seckillbackend.redis.key.UserKeyPrefix;
import com.seen.seckillbackend.service.UserService;
import com.seen.seckillbackend.util.Result;
import com.seen.seckillbackend.util.StringBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class TestController {

    @Autowired
    UserService userService;

    @Autowired
    RedisService redisService;

    @Autowired
    MQSender sender;

    @RequestMapping("/get")
    @ResponseBody
    public Result<User> dbGet() {
        User user = userService.getTest("15850519227");
        System.out.println(user);
        return Result.success(user);
    }

    @GetMapping("/redis/{username}")
    @ResponseBody
    public Result<String> redisGet(@PathVariable String username) {
        String s = redisService.get(UserKeyPrefix.userIdPrefix, username, String.class);
        return Result.success(s);
    }

    @PostMapping("/redis")
    @ResponseBody
    public Result<Boolean> redisSet(@RequestBody User user) {
        System.out.println(user);
        Boolean s = redisService.set(UserKeyPrefix.userIdPrefix, user.getUsername()+"", user.getPassword());
        return Result.success(s);
    }

    @GetMapping("/mq")
    @ResponseBody
    public Result<String> mq() {
        sender.send("hello, johnson");
        return Result.success("");
    }

    @RequestMapping(value = "/test", method = RequestMethod.POST)
    @ResponseBody
    public void test(User user) {
        Goods goods = new Goods();
        goods.setId(1L);
        String string = StringBean.beanToString(new SeckillMessage(user, goods));
        System.out.println(string);
        SeckillMessage seckillMessage = StringBean.stringToBean(string, SeckillMessage.class);
        System.out.println(seckillMessage + "       success");
    }

}
