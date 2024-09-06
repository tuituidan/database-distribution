package com.tuituidan.openhub.controller;

import com.tuituidan.openhub.bean.dto.SysAppParam;
import com.tuituidan.openhub.bean.entity.SysApp;
import com.tuituidan.openhub.bean.vo.TreeView;
import com.tuituidan.openhub.service.AppDatabaseConfigService;
import com.tuituidan.openhub.service.SysAppService;
import com.tuituidan.tresdin.consts.TresdinConsts;
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
@RequestMapping(TresdinConsts.API_V1 + "/sys_app")
public class SysAppController {

    @Resource
    private SysAppService sysAppService;

    @Resource
    private AppDatabaseConfigService appDatabaseConfigService;

    /**
     * selectAll
     *
     * @return List
     */
    @GetMapping
    public List<SysApp> selectAll() {
        return sysAppService.selectAll();
    }

    /**
     * add
     *
     * @param param param
     */
    @PostMapping
    public void add(@RequestBody SysAppParam param) {
        sysAppService.save(null, param);
    }

    /**
     * update
     *
     * @param id id
     * @param param param
     */
    @PatchMapping("/{id}")
    public void update(@PathVariable Long id, @RequestBody SysAppParam param) {
        sysAppService.save(id, param);
    }

    /**
     * delete
     *
     * @param id id
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        sysAppService.delete(id);
    }

    /**
     * selectConfigTree
     *
     * @return List
     */
    @GetMapping("/database_config/tree")
    public List<TreeView> selectConfigTree() {
        return appDatabaseConfigService.selectTree();
    }

    /**
     * selectConfigTree
     *
     * @return List
     */
    @GetMapping("/{appId}/database_config")
    public List<Long> selectConfig(@PathVariable Long appId) {
        return appDatabaseConfigService.selectIds(appId);
    }

}
