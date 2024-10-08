package com.tuituidan.openhub.controller;

import com.tuituidan.openhub.bean.dto.SysDataSourceParam;
import com.tuituidan.openhub.bean.entity.SysDataSource;
import com.tuituidan.openhub.service.DataSourceService;
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
@RequestMapping(TresdinConsts.API_V1 + "/datasource")
public class DataSourceController {

    @Resource
    private DataSourceService dataSourceService;

    /**
     * selectAll
     *
     * @return List
     */
    @GetMapping
    public List<SysDataSource> selectAll() {
        return dataSourceService.selectAll();
    }

    /**
     * add
     *
     * @param param param
     */
    @PostMapping
    public void add(@RequestBody SysDataSourceParam param) {
        dataSourceService.save(null, param);
    }

    /**
     * update
     *
     * @param id id
     * @param param param
     */
    @PatchMapping("/{id}")
    public void update(@PathVariable Long id, @RequestBody SysDataSourceParam param) {
        dataSourceService.save(id, param);
    }

    /**
     * setStatus
     *
     * @param id id
     * @param status status
     */
    @PatchMapping("/{id}/status/{status}")
    public void setStatus(@PathVariable Long id, @PathVariable String status) {
        dataSourceService.setStatus(id, status);
    }

    /**
     * delete
     *
     * @param id id
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        dataSourceService.delete(id);
    }

    /**
     * getDatabase
     *
     * @param id id
     * @return List
     */
    @GetMapping("/{id}/database")
    public List<String> getDatabase(@PathVariable Long id) {
        return dataSourceService.getDatabase(id);
    }

    /**
     * getDatabaseTables
     *
     * @param id id
     * @param database database
     * @return List
     */
    @GetMapping("/{id}/database/{database}")
    public List<String> getDatabaseTables(@PathVariable Long id, @PathVariable String database) {
        return dataSourceService.getDatabaseTables(id, database);
    }

    /**
     * getDatabaseTablesColumn
     *
     * @param id id
     * @param database database
     * @param tableName tableName
     * @return List
     */
    @GetMapping("/{id}/database/{database}/table/{tableName}")
    public List<String> getDatabaseTablesColumn(@PathVariable Long id, @PathVariable String database,
            @PathVariable String tableName) {
        return dataSourceService.getDatabaseTablesColumn(id, database, tableName);
    }

}
