package com.tuituidan.openhub.service;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.DeleteRowsEventData;
import com.github.shyiko.mysql.binlog.event.EventData;
import com.github.shyiko.mysql.binlog.event.TableMapEventData;
import com.github.shyiko.mysql.binlog.event.UpdateRowsEventData;
import com.github.shyiko.mysql.binlog.event.WriteRowsEventData;
import com.tuituidan.openhub.bean.entity.SysDataSource;
import com.tuituidan.openhub.consts.enums.StatusEnum;
import com.tuituidan.tresdin.util.StringExtUtils;
import com.tuituidan.tresdin.util.thread.CompletableUtils;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * DatasourceClient.
 *
 * @author tuituidan
 * @version 1.0
 * @date 2024/9/8
 */
@Slf4j
public abstract class DatasourceClient {

    private final SysDataSource dataSource;

    private BinaryLogClient binaryLogClient;

    private final JdbcTemplate jdbcTemplate;

    private static final String JDBC_URL = "jdbc:mysql://{}:{}/{}?useUnicode=true&characterEncoding=utf8"
            + "&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8";

    private static final String SQL_DATABASE = "select SCHEMA_NAME from information_schema.SCHEMATA where SCHEMA_NAME"
            + " not in ('mysql', 'information_schema', 'performance_schema', 'sys')";

    private static final String SQL_TABLE = "select TABLE_NAME from information_schema.TABLES where TABLE_SCHEMA ='{}'";

    private final Map<Long, TableMapEventData> configMap = new HashMap<>();

    /**
     * handler
     *
     * @param jdbcTemplate jdbcTemplate
     * @param tableEvent tableEvent
     * @param type type
     * @param rows rows
     */
    public abstract void handler(JdbcTemplate jdbcTemplate, TableMapEventData tableEvent, String type,
            List<Serializable[]> rows);

    /**
     * DatasourceClient
     *
     * @param dataSource dataSource
     */
    protected DatasourceClient(SysDataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(DataSourceBuilder.create()
                .url(StringExtUtils.format(JDBC_URL, dataSource.getHost(),
                        dataSource.getPort(), "mysql"))
                .username(dataSource.getUsername())
                .password(dataSource.getPassword())
                .driverClassName("com.mysql.cj.jdbc.Driver")
                .build());
        if (StatusEnum.OPEN.getCode().equals(dataSource.getStatus())) {
            start();
        }
    }

    /**
     * start
     */
    public void start() {
        CompletableUtils.runAsync(() -> {
            this.binaryLogClient = new BinaryLogClient(dataSource.getHost(),
                    dataSource.getPort(),
                    dataSource.getUsername(),
                    dataSource.getPassword());
            this.binaryLogClient.setServerId(dataSource.getServerId());
            this.binaryLogClient.registerEventListener(event -> analyse(event.getData()));
            try {
                binaryLogClient.connect();
            } catch (Exception ex) {
                log.error("数据监听启动失败", ex);
            }
        });

    }

    /**
     * stop
     */
    public void stop() {
        try {
            binaryLogClient.disconnect();
        } catch (IOException ex) {
            log.error("数据监听停止失败", ex);
            throw new IllegalMonitorStateException("数据监听停止失败");
        }
    }

    private void analyse(EventData data) {
        if (data instanceof TableMapEventData) {
            TableMapEventData tableEvent = (TableMapEventData) data;
            configMap.put(tableEvent.getTableId(), tableEvent);
            return;
        }
        if (data instanceof UpdateRowsEventData) {
            UpdateRowsEventData rowsData = (UpdateRowsEventData) data;
            if (configMap.containsKey(rowsData.getTableId())) {
                handler(jdbcTemplate, configMap.get(rowsData.getTableId()), "update",
                        rowsData.getRows().stream().map(Entry::getValue).collect(Collectors.toList()));
            }
            return;
        }
        if (data instanceof WriteRowsEventData) {
            WriteRowsEventData rowsData = (WriteRowsEventData) data;
            if (configMap.containsKey(rowsData.getTableId())) {
                handler(jdbcTemplate, configMap.get(rowsData.getTableId()), "insert", rowsData.getRows());
            }
            return;
        }
        if (data instanceof DeleteRowsEventData) {
            DeleteRowsEventData rowsData = (DeleteRowsEventData) data;
            if (configMap.containsKey(rowsData.getTableId())) {
                handler(jdbcTemplate, configMap.get(rowsData.getTableId()), "delete", rowsData.getRows());
            }
        }
    }

    /**
     * getDatabase
     *
     * @return List
     */
    public List<String> getDatabase() {
        return jdbcTemplate.queryForList(SQL_DATABASE, String.class);
    }

    /**
     * getDatabaseTables
     *
     * @return List
     */
    public List<String> getDatabaseTables(String database) {
        return jdbcTemplate.queryForList(StringExtUtils.format(SQL_TABLE, database), String.class);
    }

}
