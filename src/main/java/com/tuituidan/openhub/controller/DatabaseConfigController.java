package com.tuituidan.openhub.controller;

import com.tuituidan.openhub.bean.dto.PushHandlerParam;
import com.tuituidan.openhub.bean.dto.SysDatabaseConfigParam;
import com.tuituidan.openhub.bean.entity.SysDatabaseConfig;
import com.tuituidan.openhub.service.DataAnalyseService;
import com.tuituidan.openhub.service.DatabaseConfigService;
import com.tuituidan.tresdin.consts.TresdinConsts;
import com.tuituidan.tresdin.datatranslate.annotation.DataTranslate;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * SysAppController.
 *
 * @author tuituidan
 * @version 1.0
 * @date 2024/9/4
 */
@RestController
@RequestMapping(TresdinConsts.API_V1 + "/database_config")
public class DatabaseConfigController {

    @Resource
    private DatabaseConfigService databaseConfigService;

    @Resource
    private DataAnalyseService dataAnalyseService;

    /**
     * select
     *
     * @param param param
     * @return List
     */
    @GetMapping
    @DataTranslate
    public List<SysDatabaseConfig> select(SysDatabaseConfigParam param) {
        return databaseConfigService.select(param);
    }

    /**
     * add
     *
     * @param param param
     */
    @PostMapping
    public void add(@RequestBody SysDatabaseConfigParam param) {
        databaseConfigService.save(null, param);
    }

    /**
     * update
     *
     * @param id id
     * @param param param
     */
    @PatchMapping("/{id}")
    public void update(@PathVariable Long id, @RequestBody SysDatabaseConfigParam param) {
        databaseConfigService.save(id, param);
    }

    /**
     * delete
     *
     * @param id id
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        databaseConfigService.delete(id);
    }

    /**
     * handler
     *
     * @param param param
     */
    @PostMapping("/handler")
    public void handler(@RequestBody PushHandlerParam param) {
        dataAnalyseService.analyse(param.getDatasourceId(), param.getIds(), param.getIncrementValue());
    }

}
