package com.tuituidan.openhub.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.shyiko.mysql.binlog.event.TableMapEventData;
import com.tuituidan.openhub.bean.dto.SysDataSourceParam;
import com.tuituidan.openhub.bean.entity.SysDataSource;
import com.tuituidan.openhub.bean.entity.SysDatabaseConfig;
import com.tuituidan.openhub.consts.enums.StatusEnum;
import com.tuituidan.openhub.mapper.SysDataSourceMapper;
import com.tuituidan.openhub.mapper.SysDatabaseConfigMapper;
import com.tuituidan.tresdin.util.BeanExtUtils;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * DataSourceService.
 *
 * @author tuituidan
 * @version 1.0
 * @date 2024/8/31
 */
@Service
@Slf4j
public class DataSourceService implements ApplicationRunner {

    @Resource
    private SysDataSourceMapper sysDataSourceMapper;

    @Resource
    private DatabaseConfigService databaseConfigService;

    @Resource
    private SysDatabaseConfigMapper sysDatabaseConfigMapper;

    @Resource
    private Cache<Long, SysDataSource> sysDataSourceCache;

    private final Map<Long, DatasourceClient> datasourceClientMap = new HashMap<>();

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<SysDataSource> dataSourceList = selectAll();
        for (SysDataSource dataSource : dataSourceList) {
            datasourceClientMap.put(dataSource.getId(), createClient(dataSource));
        }
    }

    private DatasourceClient createClient(SysDataSource dataSource) {
        return new DatasourceClient(dataSource) {
            @Override
            public void handler(JdbcTemplate jdbcTemplate, TableMapEventData tableEvent, String type,
                    List<Serializable[]> rows) {
                databaseConfigService.analyse(jdbcTemplate, tableEvent, type, rows);
            }
        };
    }

    /**
     * selectAll
     *
     * @return PageData
     */
    public List<SysDataSource> selectAll() {
        return sysDataSourceMapper.selectAll();
    }

    /**
     * save
     *
     * @param id id
     * @param param param
     */
    public void save(Long id, SysDataSourceParam param) {
        checkUnique(id, param);
        SysDataSource saveItem = BeanExtUtils.convert(param, SysDataSource::new);
        if (id == null) {
            saveItem.setStatus(StatusEnum.STOP.getCode());
            sysDataSourceMapper.insertSelective(saveItem);
            datasourceClientMap.put(id, createClient(saveItem));
            return;
        }
        saveItem.setId(id);
        sysDataSourceMapper.updateByPrimaryKeySelective(saveItem);
        sysDataSourceCache.invalidate(id);
    }

    private void checkUnique(Long id, SysDataSourceParam param) {
        SysDataSource search = new SysDataSource();
        BeanExtUtils.copyProperties(param, search, "host", "port", "username");
        SysDataSource exist = sysDataSourceMapper.selectOne(search);
        Assert.isTrue(Objects.isNull(exist) || Objects.equals(id, exist.getId()),
                "已存在相同数据源（数据源地址、端口、数据库账号一致）");
    }

    /**
     * setStatus
     *
     * @param id id
     * @param status status
     */
    public void setStatus(Long id, String status) {
        SysDataSource dataSource = new SysDataSource().setId(id).setStatus(status);
        if (StatusEnum.OPEN.getCode().equals(status)) {
            datasourceClientMap.get(id).start();
        } else {
            datasourceClientMap.get(id).stop();
            dataSource.setLastStopTime(LocalDateTime.now());
        }
        sysDataSourceMapper.updateByPrimaryKeySelective(dataSource);
    }

    /**
     * delete
     *
     * @param id id
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        SysDataSource dataSource = sysDataSourceMapper.selectByPrimaryKey(id);
        Assert.isTrue(StatusEnum.STOP.getCode().equals(dataSource.getStatus()), "启用中的数据源无法删除，请先停止");
        List<SysDatabaseConfig> configs = sysDatabaseConfigMapper.select(new SysDatabaseConfig().setDatasourceId(id));
        Assert.isTrue(CollectionUtils.isEmpty(configs), "存在数据库监听配置，无法删除");
        sysDataSourceMapper.deleteByPrimaryKey(id);
        sysDataSourceCache.invalidate(id);
        datasourceClientMap.remove(id);
    }

    /**
     * getDatabase
     *
     * @param id id
     * @return List
     */
    public List<String> getDatabase(Long id) {
        return datasourceClientMap.get(id).getDatabase();
    }

    /**
     * getDatabaseTables
     *
     * @param id id
     * @param database database
     * @return List
     */
    public List<String> getDatabaseTables(Long id, String database) {
        return datasourceClientMap.get(id).getDatabaseTables(database);
    }

}
