package com.tuituidan.openhub.service;

import com.tuituidan.openhub.bean.dto.SysAppParam;
import com.tuituidan.openhub.bean.entity.SysApp;
import com.tuituidan.openhub.bean.entity.SysAppDatabaseConfig;
import com.tuituidan.openhub.bean.entity.SysDataSource;
import com.tuituidan.openhub.bean.entity.SysDatabaseConfig;
import com.tuituidan.openhub.bean.vo.TreeView;
import com.tuituidan.openhub.mapper.SysAppDatabaseConfigMapper;
import com.tuituidan.openhub.mapper.SysAppMapper;
import com.tuituidan.openhub.mapper.SysDataSourceMapper;
import com.tuituidan.openhub.mapper.SysDatabaseConfigMapper;
import com.tuituidan.tresdin.consts.Separator;
import com.tuituidan.tresdin.util.BeanExtUtils;
import com.tuituidan.tresdin.util.StringExtUtils;
import com.tuituidan.tresdin.util.tree.TreeUtils;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * SysAppService.
 *
 * @author tuituidan
 * @version 1.0
 * @date 2024/8/31
 */
@Service
public class AppDatabaseConfigService implements ApplicationRunner {

    @Resource
    private SysAppMapper sysAppMapper;

    @Resource
    private SysDatabaseConfigMapper sysDatabaseConfigMapper;

    @Resource
    private SysDataSourceMapper sysDataSourceMapper;

    @Resource
    private SysAppDatabaseConfigMapper sysAppDatabaseConfigMapper;
    @Override
    public void run(ApplicationArguments args) throws Exception {

    }

    public List<Long> selectIds(Long appId){
        return sysAppDatabaseConfigMapper.select(new SysAppDatabaseConfig().setAppId(appId))
                .stream().map(SysAppDatabaseConfig::getDatabaseConfigId).collect(Collectors.toList());
    }
    /**
     * selectAll
     *
     * @return List
     */
    public List<TreeView> selectTree() {
        List<SysDatabaseConfig> databaseConfigs = sysDatabaseConfigMapper.selectAll();
        if (CollectionUtils.isEmpty(databaseConfigs)) {
            return Collections.emptyList();
        }

        String ids = StringUtils.join(databaseConfigs.stream().map(SysDatabaseConfig::getDatasourceId)
                .collect(Collectors.toSet()), Separator.COMMA);
        List<SysDataSource> dataSources = sysDataSourceMapper.selectByIds(ids);

        List<TreeView> resultList = dataSources.stream()
                .map(item -> new TreeView().setId(item.getId().toString())
                        .setName(StringExtUtils.format("{}({}:{})",
                                item.getName(), item.getHost(), item.getPort())))
                .collect(Collectors.toList());
        Map<String, TreeView> databaseNodeMap = new HashMap<>();
        for (SysDatabaseConfig config : databaseConfigs) {
            String dbNodeKey = config.getDatasourceId() + config.getDatabaseName();
            TreeView dbNode = databaseNodeMap.computeIfAbsent(dbNodeKey,
                    k -> {
                        TreeView parentNode = new TreeView().setId(StringExtUtils.getUuid())
                                .setName(config.getDatabaseName())
                                .setPid(config.getDatasourceId().toString());
                        resultList.add(parentNode);
                        return parentNode;
                    });
            resultList.add(new TreeView().setId(config.getId().toString())
                    .setPid(dbNode.getId()).setName(StringExtUtils.format("{}({})",
                            config.getTableComment(), config.getTableName())));
        }
        return TreeUtils.buildTree(resultList);
    }

    /**
     * save
     *
     * @param id id
     * @param param param
     */
    public void save(Long id, SysAppParam param) {
        SysApp exist = sysAppMapper.selectOne(new SysApp().setAppKey(param.getAppKey()));
        if (id == null) {
            Assert.isNull(exist, "已存在相同应用标识的应用");
            sysAppMapper.insertSelective(BeanExtUtils.convert(param, SysApp::new));
            return;
        }
        Assert.isTrue(id.equals(exist.getId()), "已存在相同应用标识的应用");
        BeanExtUtils.copyNotNullProperties(param, exist);
        sysAppMapper.updateByPrimaryKeySelective(exist);
    }

    /**
     * delete
     *
     * @param id id
     */
    public void delete(Long id) {
        sysAppMapper.deleteByPrimaryKey(id);
    }

}
