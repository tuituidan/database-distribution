package com.tuituidan.openhub.service;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.DeleteRowsEventData;
import com.github.shyiko.mysql.binlog.event.EventData;
import com.github.shyiko.mysql.binlog.event.TableMapEventData;
import com.github.shyiko.mysql.binlog.event.UpdateRowsEventData;
import com.github.shyiko.mysql.binlog.event.WriteRowsEventData;
import com.tuituidan.openhub.bean.entity.SysDataSource;
import com.tuituidan.openhub.bean.entity.SysDatabaseConfig;
import com.tuituidan.openhub.bean.vo.SysDatabaseConfigView;
import com.tuituidan.openhub.bean.vo.TableStruct;
import com.tuituidan.openhub.config.AppPropertiesConfig;
import com.tuituidan.openhub.mapper.SysDataSourceMapper;
import com.tuituidan.openhub.mapper.SysDatabaseConfigMapper;
import com.tuituidan.tresdin.consts.Separator;
import com.tuituidan.tresdin.util.BeanExtUtils;
import com.tuituidan.tresdin.util.StringExtUtils;
import com.tuituidan.tresdin.util.thread.CompletableUtils;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * BinlogAnalyse.
 *
 * @author tuituidan
 * @version 1.0
 * @date 2024/8/31
 */
@Service
@Slf4j
public class BinlogAnalyse implements ApplicationRunner {

    @Resource
    private SysDatabaseConfigMapper sysDatabaseConfigMapper;

    @Resource
    private SysDataSourceMapper sysDataSourceMapper;

    @Resource
    private DataPushService dataPushService;

    @Resource
    private AppPropertiesConfig appPropertiesConfig;

    @Override
    public void run(ApplicationArguments args) {
        if (BooleanUtils.isNotTrue(appPropertiesConfig.getBinlogEnabled())) {
            return;
        }
        Map<Long, List<SysDatabaseConfig>> databaseConfigMap =
                sysDatabaseConfigMapper.selectAll().stream()
                        .collect(Collectors.groupingBy(SysDatabaseConfig::getDatasourceId));
        List<SysDataSource> dataSourceList =
                sysDataSourceMapper.selectByIds(StringUtils.join(databaseConfigMap.keySet(), Separator.COMMA));
        for (SysDataSource dataSource : dataSourceList) {
            CompletableUtils.runAsync(() -> createBinaryLogClient(dataSource,
                    databaseConfigMap.get(dataSource.getId())));
        }
    }

    private void createBinaryLogClient(SysDataSource dataSource, List<SysDatabaseConfig> configs) {
        BinaryLogClient client = new BinaryLogClient(dataSource.getHost(),
                dataSource.getPort(),
                "mysql",
                dataSource.getUsername(),
                dataSource.getPassword());
        client.setServerId(dataSource.getServerId());
        Map<String, SysDatabaseConfigView> configMap = buildTableConfigMap(dataSource, configs);
        Map<Long, SysDatabaseConfigView> tableMap = new HashMap<>(configMap.keySet().size());
        client.registerEventListener(event -> analyse(event.getData(), configMap, tableMap));
        try {
            client.connect();
        } catch (Exception ex) {
            log.error("数据监控启动失败", ex);
        }
    }

    private void analyse(EventData data, Map<String, SysDatabaseConfigView> databaseConfigMap,
            Map<Long, SysDatabaseConfigView> tableMap) {
        if (data instanceof TableMapEventData) {
            TableMapEventData tableEvent = (TableMapEventData) data;
            String tableKey = tableEvent.getDatabase() + tableEvent.getTable();
            if (databaseConfigMap.containsKey(tableKey)) {
                tableMap.put(tableEvent.getTableId(), databaseConfigMap.get(tableKey));
            }
            return;
        }
        if (data instanceof UpdateRowsEventData) {
            UpdateRowsEventData rowsData = (UpdateRowsEventData) data;
            if (tableMap.containsKey(rowsData.getTableId())) {
                dataPushService.analyse(tableMap.get(rowsData.getTableId()), "update",
                        rowsData.getRows().stream().map(Entry::getValue).collect(Collectors.toList()));
            }
            return;
        }
        if (data instanceof WriteRowsEventData) {
            WriteRowsEventData rowsData = (WriteRowsEventData) data;
            if (tableMap.containsKey(rowsData.getTableId())) {
                dataPushService.analyse(tableMap.get(rowsData.getTableId()), "insert", rowsData.getRows());
            }
            return;
        }
        if (data instanceof DeleteRowsEventData) {
            DeleteRowsEventData rowsData = (DeleteRowsEventData) data;
            if (tableMap.containsKey(rowsData.getTableId())) {
                dataPushService.analyse(tableMap.get(rowsData.getTableId()), "delete", rowsData.getRows());
            }
        }
    }

    private Map<String, SysDatabaseConfigView> buildTableConfigMap(SysDataSource dataSource,
            List<SysDatabaseConfig> configs) {
        Set<String> databases =
                configs.stream().map(SysDatabaseConfig::getDatabaseName).collect(Collectors.toSet());
        Set<String> tables = configs.stream().map(SysDatabaseConfig::getTableName).collect(Collectors.toSet());
        List<TableStruct> tableStructList = new JdbcTemplate(DataSourceBuilder.create()
                .url(StringExtUtils.format(appPropertiesConfig.getJdbcUrlTemplate(), dataSource.getHost(),
                        dataSource.getPort(), "mysql"))
                .username(dataSource.getUsername())
                .password(dataSource.getPassword())
                .driverClassName("com.mysql.cj.jdbc.Driver")
                .build()).query(StringExtUtils.format(appPropertiesConfig.getTableStructSql(),
                StringUtils.join(databases, "','"),
                StringUtils.join(tables, "','")), new BeanPropertyRowMapper<>(TableStruct.class));
        Map<String, List<TableStruct>> tableStructMap = tableStructList.stream()
                .collect(Collectors.groupingBy(item -> item.getTableSchema() + item.getTableName()));
        Map<String, SysDatabaseConfigView> resultMap = new HashMap<>(configs.size());
        for (SysDatabaseConfig config : configs) {
            String tableKey = config.getDatabaseName() + config.getTableName();
            List<TableStruct> structs = tableStructMap.get(tableKey);
            if (CollectionUtils.isNotEmpty(structs)) {
                structs.sort(Comparator.comparingInt(TableStruct::getOrdinalPosition));
                resultMap.put(tableKey, BeanExtUtils.convert(config, SysDatabaseConfigView::new)
                        .setTableStruct(structs));
            }
        }
        return resultMap;
    }

}
