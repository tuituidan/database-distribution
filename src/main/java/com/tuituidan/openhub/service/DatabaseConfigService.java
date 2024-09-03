package com.tuituidan.openhub.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.shyiko.mysql.binlog.event.TableMapEventData;
import com.tuituidan.openhub.bean.dto.SysDatabaseConfigParam;
import com.tuituidan.openhub.bean.entity.SysDatabaseConfig;
import com.tuituidan.openhub.bean.vo.SysDatabaseConfigView;
import com.tuituidan.openhub.bean.vo.TableStruct;
import com.tuituidan.openhub.config.AppPropertiesConfig;
import com.tuituidan.openhub.mapper.SysDatabaseConfigMapper;
import com.tuituidan.tresdin.util.BeanExtUtils;
import com.tuituidan.tresdin.util.StringExtUtils;
import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * DatabaseConfigService.
 *
 * @author tuituidan
 * @version 1.0
 * @date 2024/9/3
 */
@Service
public class DatabaseConfigService implements ApplicationRunner {

    @Resource
    private Cache<String, SysDatabaseConfigView> databaseConfigViewCache;

    @Resource
    private Cache<String, SysDatabaseConfig> databaseConfigCache;

    @Resource
    private AppPropertiesConfig appPropertiesConfig;

    @Resource
    private SysDatabaseConfigMapper sysDatabaseConfigMapper;

    @Resource
    private DataPushService dataPushService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<SysDatabaseConfig> configs = sysDatabaseConfigMapper.selectAll();
        for (SysDatabaseConfig config : configs) {
            databaseConfigCache.put(config.getDatabaseName() + config.getTableName(), config);
        }
    }

    /**
     * save
     *
     * @param id id
     * @param param param
     */
    public void save(Long id, SysDatabaseConfigParam param) {
        SysDatabaseConfig search = new SysDatabaseConfig();
        BeanExtUtils.copyProperties(param, search, "datasourceId", "databaseName", "tableName");
        SysDatabaseConfig exist = sysDatabaseConfigMapper.selectOne(search);
        if (id == null) {
            Assert.isNull(exist, "已存在相同配置（数据源、数据库名、表名一致）");
            sysDatabaseConfigMapper.insertSelective(BeanExtUtils.convert(param, SysDatabaseConfig::new));
        } else {
            Assert.isTrue(id.equals(exist.getId()), "已存在相同配置（数据源、数据库名、表名一致）");
            BeanExtUtils.copyNotNullProperties(param, exist);
            sysDatabaseConfigMapper.updateByPrimaryKeySelective(exist);
        }
        String cacheKey = param.getDatabaseName() + param.getTableName();
        databaseConfigCache.invalidate(cacheKey);
        databaseConfigViewCache.invalidate(cacheKey);
    }

    /**
     * delete
     *
     * @param id id
     */
    public void delete(Long id) {
        SysDatabaseConfig config = sysDatabaseConfigMapper.selectByPrimaryKey(id);
        Assert.notNull(config, "配置不存在");
        sysDatabaseConfigMapper.deleteByPrimaryKey(id);
        String cacheKey = config.getDatabaseName() + config.getTableName();
        databaseConfigCache.invalidate(cacheKey);
        databaseConfigViewCache.invalidate(cacheKey);
    }

    /**
     * analyse
     *
     * @param jdbcTemplate jdbcTemplate
     * @param tableEvent tableEvent
     * @param type type
     * @param rows rows
     */
    public void analyse(JdbcTemplate jdbcTemplate, TableMapEventData tableEvent, String type,
            List<Serializable[]> rows) {
        String cacheKey = tableEvent.getDatabase() + tableEvent.getTable();
        SysDatabaseConfig config = databaseConfigCache.getIfPresent(cacheKey);
        if (config == null) {
            return;
        }
        dataPushService.analyse(databaseConfigViewCache.get(cacheKey,
                k -> getDatabaseConfigView(jdbcTemplate, config)), type, rows);
    }

    private SysDatabaseConfigView getDatabaseConfigView(JdbcTemplate jdbcTemplate, SysDatabaseConfig config) {
        SysDatabaseConfigView configView = BeanExtUtils.convert(config, SysDatabaseConfigView::new);
        configView.setTableStruct(jdbcTemplate.query(
                StringExtUtils.format(appPropertiesConfig.getTableStructSql(),
                        config.getDatabaseName(), config.getTableName()),
                new BeanPropertyRowMapper<>(TableStruct.class)));
        configView.getTableStruct().sort(Comparator.comparingInt(TableStruct::getOrdinalPosition));
        return configView;
    }

}
