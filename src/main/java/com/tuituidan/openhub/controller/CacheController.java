package com.tuituidan.openhub.controller;

import com.tuituidan.openhub.service.CacheService;
import com.tuituidan.tresdin.consts.TresdinConsts;
import java.util.Map;
import javax.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * CacheController.
 *
 * @author tuituidan
 * @version 1.0
 * @date 2024/9/13
 */
@RestController
@RequestMapping(TresdinConsts.API_V1 + "/cache")
public class CacheController {

    @Resource
    private CacheService cacheService;

    /**
     * caches
     *
     * @return Map
     */
    @GetMapping
    public Map<String, Object> caches() {
        return cacheService.caches();
    }

}
