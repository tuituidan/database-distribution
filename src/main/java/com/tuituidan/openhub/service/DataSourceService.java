package com.tuituidan.openhub.service;

import com.tuituidan.openhub.bean.dto.SysDataSourceParam;
import com.tuituidan.openhub.bean.entity.SysDataSource;
import com.tuituidan.openhub.mapper.SysDataSourceMapper;
import com.tuituidan.tresdin.util.BeanExtUtils;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * DataSourceService.
 *
 * @author tuituidan
 * @version 1.0
 * @date 2024/8/31
 */
@Service
public class DataSourceService {

    @Resource
    private SysDataSourceMapper sysDataSourceMapper;

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
        SysDataSource search = new SysDataSource();
        BeanExtUtils.copyProperties(param, search, "host", "port", "username");
        SysDataSource exist = sysDataSourceMapper.selectOne(search);
        if (id == null) {
            Assert.isNull(exist, "已存在相同数据源（数据源地址、端口、数据库账号一致）");
            sysDataSourceMapper.insertSelective(BeanExtUtils.convert(param, SysDataSource::new));
            return;
        }
        Assert.isTrue(id.equals(exist.getId()), "已存在相同数据源（数据源地址、端口、数据库账号一致）");
        BeanExtUtils.copyNotNullProperties(param, exist);
        sysDataSourceMapper.updateByPrimaryKeySelective(exist);
    }

    /**
     * delete
     *
     * @param id id
     */
    public void delete(Long id) {
        sysDataSourceMapper.deleteByPrimaryKey(id);
    }

}
