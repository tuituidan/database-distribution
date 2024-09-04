package com.tuituidan.openhub.controller;

import com.tuituidan.openhub.bean.dto.SysDatabaseConfigParam;
import com.tuituidan.openhub.bean.entity.SysDatabaseConfig;
import com.tuituidan.openhub.service.DatabaseConfigService;
import com.tuituidan.tresdin.consts.TresdinConsts;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

    /**
     * select
     *
     * @param param param
     * @return List
     */
    @GetMapping
    public List<SysDatabaseConfig> select(SysDatabaseConfigParam param) {
        return databaseConfigService.select(param);
    }

    /**
     * add
     *
     * @param param param
     */
    @PostMapping
    public void add(SysDatabaseConfigParam param) {
        databaseConfigService.save(null, param);
    }

    /**
     * update
     *
     * @param id id
     * @param param param
     */
    @PatchMapping("/{id}")
    public void update(@PathVariable Long id, SysDatabaseConfigParam param) {
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

}