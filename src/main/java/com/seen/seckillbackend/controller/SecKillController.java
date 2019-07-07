package com.seen.seckillbackend.controller;

import com.seen.seckillbackend.common.access.AccessLimit;
import com.seen.seckillbackend.common.response.CodeMsg;
import com.seen.seckillbackend.common.response.Result;
import com.seen.seckillbackend.domain.Goods;
import com.seen.seckillbackend.middleware.redis.key.GoodsKeyPe;
import com.seen.seckillbackend.middleware.redis.single.RedisService;
import com.seen.seckillbackend.service.GoodsService;
import com.seen.seckillbackend.service.SeckillService;
import com.seen.seckillbackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;

@Controller
@Slf4j
public class SecKillController implements InitializingBean {

    @Autowired
    GoodsService goodsService;

    @Autowired
    RedisService redisService;

    @Autowired
    SeckillService seckillService;


    @Autowired
    SeckillService orderService;

    @Autowired
    UserService userService;

    // 内存标记Map<goodsId, isOver>, true is over
    private HashMap<Long, Boolean> localOverMap = new HashMap<Long, Boolean>();

    /**
     * 系统初始化, 秒杀商品库存加载进redis
     */
    @Override
    public void afterPropertiesSet() throws Exception {
//        orderService.reset();
        goodsService.reset();
        redisService.deleteAll();
        List<Goods> goodsList = goodsService.getGoodsList();
        if (null == goodsList) {
            return;
        }
        for (Goods goods : goodsList) {
            long stock = goodsService.getGoodsStockById(goods.getId());
            redisService.set(GoodsKeyPe.goodsKeyPe, "" + goods.getId(), stock);
            localOverMap.put(goods.getId(), false);
        }
    }

    /**
     * 高并发访问接口
     */
    @AccessLimit(seconds = 5, maxCount = 5)
    @GetMapping("/seckill/{goodsId}")
    @ResponseBody
    public Result<Integer> seckill(Long userId, @PathVariable Long goodsId) {
        if (userId == null) {
            log.info("用户未登录");
            return Result.err(CodeMsg.USER_NEEDS_LOGIN);
        }
        seckillService.seckill(userId, goodsId, localOverMap);
        return Result.success(0); //排队中
    }

}
