package com.tuituidan.openhub.service;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.DeleteRowsEventData;
import com.github.shyiko.mysql.binlog.event.EventData;
import com.github.shyiko.mysql.binlog.event.TableMapEventData;
import com.github.shyiko.mysql.binlog.event.UpdateRowsEventData;
import com.github.shyiko.mysql.binlog.event.WriteRowsEventData;
import com.tuituidan.openhub.bean.dto.SysDataSourceParam;
import com.tuituidan.openhub.bean.entity.SysDataSource;
import com.tuituidan.openhub.bean.entity.SysDatabaseConfig;
import com.tuituidan.openhub.config.AppPropertiesConfig;
import com.tuituidan.openhub.mapper.SysDataSourceMapper;
import com.tuituidan.openhub.mapper.SysDatabaseConfigMapper;
import com.tuituidan.tresdin.util.BeanExtUtils;
import com.tuituidan.tresdin.util.StringExtUtils;
import com.tuituidan.tresdin.util.thread.CompletableUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.jdbc.DataSourceBuilder;
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
    private AppPropertiesConfig appPropertiesConfig;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (BooleanUtils.isNotTrue(appPropertiesConfig.getBinlogEnabled())) {
            return;
        }
        List<SysDataSource> dataSourceList = selectAll();
        for (SysDataSource dataSource : dataSourceList) {
            CompletableUtils.runAsync(() -> createBinaryLogClient(dataSource));
        }
    }

    private void createBinaryLogClient(SysDataSource dataSource) {
        BinaryLogClient client = new BinaryLogClient(dataSource.getHost(),
                dataSource.getPort(),
                dataSource.getUsername(),
                dataSource.getPassword());
        client.setServerId(dataSource.getServerId());
        Map<Long, TableMapEventData> configMap = new HashMap<>();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSourceBuilder.create()
                .url(StringExtUtils.format(appPropertiesConfig.getJdbcUrlTemplate(), dataSource.getHost(),
                        dataSource.getPort(), "mysql"))
                .username(dataSource.getUsername())
                .password(dataSource.getPassword())
                .driverClassName("com.mysql.cj.jdbc.Driver")
                .build());
        client.registerEventListener(event -> analyse(jdbcTemplate, event.getData(), configMap));
        try {
            client.connect();
        } catch (Exception ex) {
            log.error("数据监控启动失败", ex);
        }
    }

    private void analyse(JdbcTemplate jdbcTemplate, EventData data, Map<Long, TableMapEventData> configMap) {
        if (data instanceof TableMapEventData) {
            TableMapEventData tableEvent = (TableMapEventData) data;
            configMap.put(tableEvent.getTableId(), tableEvent);
            return;
        }
        if (data instanceof UpdateRowsEventData) {
            UpdateRowsEventData rowsData = (UpdateRowsEventData) data;
            if (configMap.containsKey(rowsData.getTableId())) {
                databaseConfigService.analyse(jdbcTemplate, configMap.get(rowsData.getTableId()), "update",
                        rowsData.getRows().stream().map(Entry::getValue).collect(Collectors.toList()));
            }
            return;
        }
        if (data instanceof WriteRowsEventData) {
            WriteRowsEventData rowsData = (WriteRowsEventData) data;
            if (configMap.containsKey(rowsData.getTableId())) {
                databaseConfigService.analyse(jdbcTemplate, configMap.get(rowsData.getTableId()), "insert",
                        rowsData.getRows());
            }
            return;
        }
        if (data instanceof DeleteRowsEventData) {
            DeleteRowsEventData rowsData = (DeleteRowsEventData) data;
            if (configMap.containsKey(rowsData.getTableId())) {
                databaseConfigService.analyse(jdbcTemplate, configMap.get(rowsData.getTableId()), "delete",
                        rowsData.getRows());
            }
        }
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
            sysDataSourceMapper.insertSelective(saveItem);
            return;
        }
        saveItem.setId(id);
        sysDataSourceMapper.updateByPrimaryKeySelective(saveItem);
    }

    private void checkUnique(Long id, SysDataSourceParam param) {
        SysDataSource search = new SysDataSource();
        BeanExtUtils.copyProperties(param, search, "host", "port", "username");
        SysDataSource exist = sysDataSourceMapper.selectOne(search);
        Assert.isTrue(Objects.isNull(exist) || Objects.equals(id, exist.getId()),
                "已存在相同数据源（数据源地址、端口、数据库账号一致）");
    }

    /**
     * delete
     *
     * @param id id
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        List<SysDatabaseConfig> configs = sysDatabaseConfigMapper.select(new SysDatabaseConfig().setDatasourceId(id));
        Assert.isTrue(CollectionUtils.isEmpty(configs), "存在数据库监听配置，无法删除");
        sysDataSourceMapper.deleteByPrimaryKey(id);
    }

}
