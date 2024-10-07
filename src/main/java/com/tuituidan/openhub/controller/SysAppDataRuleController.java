package com.tuituidan.openhub.controller;

import com.tuituidan.openhub.bean.dto.SysAppDataRuleParam;
import com.tuituidan.openhub.bean.entity.SysAppDataRule;
import com.tuituidan.openhub.service.SysAppDataRuleService;
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
 * SysAppDataRuleController.
 *
 * @author tuituidan
 * @version 1.0
 * @date 2024/9/4
 */
@RestController
@RequestMapping(TresdinConsts.API_V1 + "/sys_app_data_rule")
public class SysAppDataRuleController {

    @Resource
    private SysAppDataRuleService sysAppDataRuleService;

    /**
     * selectAll
     *
     * @return List
     */
    @GetMapping("/app/{appId}/database_config/{databaseConfigId}")
    @DataTranslate
    public List<SysAppDataRule> selectList(@PathVariable Long appId, @PathVariable Long databaseConfigId) {
        return sysAppDataRuleService.selectList(appId, databaseConfigId);
    }

    /**
     * add
     *
     * @param param param
     */
    @PostMapping
    public void add(@RequestBody SysAppDataRuleParam param) {
        sysAppDataRuleService.save(null, param);
    }

    /**
     * update
     *
     * @param id id
     * @param param param
     */
    @PatchMapping("/{id}")
    public void update(@PathVariable Long id, @RequestBody SysAppDataRuleParam param) {
        sysAppDataRuleService.save(id, param);
    }

    /**
     * delete
     *
     * @param id id
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long[] id) {
        sysAppDataRuleService.delete(id);
    }

}
