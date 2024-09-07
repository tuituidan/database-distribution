package com.tuituidan.openhub.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.tuituidan.openhub.bean.dto.SysAppParam;
import com.tuituidan.openhub.bean.entity.SysApp;
import com.tuituidan.openhub.mapper.SysAppMapper;
import com.tuituidan.tresdin.util.BeanExtUtils;
import java.util.List;
import java.util.Objects;
import javax.annotation.Resource;
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
public class SysAppService {

    @Resource
    private SysAppMapper sysAppMapper;

    @Resource
    private Cache<Long, SysApp> sysAppCache;

    /**
     * selectAll
     *
     * @return List
     */
    public List<SysApp> selectAll() {
        return sysAppMapper.selectAll();
    }

    /**
     * save
     *
     * @param id id
     * @param param param
     */
    public void save(Long id, SysAppParam param) {
        checkUnique(id, param);
        SysApp saveItem = BeanExtUtils.convert(param, SysApp::new);
        if (id == null) {
            sysAppMapper.insertSelective(saveItem);
            return;
        }
        saveItem.setId(id);
        sysAppMapper.updateByPrimaryKeySelective(saveItem);
        sysAppCache.invalidate(id);
    }

    private void checkUnique(Long id, SysAppParam param) {
        SysApp exist = sysAppMapper.selectOne(new SysApp().setAppKey(param.getAppKey()));
        Assert.isTrue(Objects.isNull(exist) || Objects.equals(id, exist.getId()),
                "已存在相同应用标识的应用");
    }

    /**
     * delete
     *
     * @param id id
     */
    public void delete(Long id) {
        sysAppMapper.deleteByPrimaryKey(id);
        sysAppCache.invalidate(id);
    }

}
