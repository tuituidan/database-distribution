package com.tuituidan.openhub.service;

import com.tuituidan.openhub.bean.dto.SysAppDataRuleParam;
import com.tuituidan.openhub.bean.entity.SysAppDataRule;
import com.tuituidan.openhub.mapper.SysAppDataRuleMapper;
import com.tuituidan.tresdin.util.BeanExtUtils;
import java.util.List;
import javax.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * SysAppDataRuleService.
 *
 * @author tuituidan
 * @version 1.0
 * @date 2024/9/29
 */
@Service
public class SysAppDataRuleService {

    @Resource
    private SysAppDataRuleMapper sysAppDataRuleMapper;

    /**
     * selectAll
     *
     * @param appId appId
     * @param databaseConfigId databaseConfigId
     * @return List
     */
    public List<SysAppDataRule> selectList(Long appId, Long databaseConfigId) {
        return sysAppDataRuleMapper.select(new SysAppDataRule().setAppId(appId).setDatabaseConfigId(databaseConfigId));
    }

    /**
     * save
     *
     * @param id id
     * @param param param
     */
    public void save(Long id, SysAppDataRuleParam param) {
        SysAppDataRule saveItem = BeanExtUtils.convert(param, SysAppDataRule::new);
        if (id == null) {
            sysAppDataRuleMapper.insertSelective(saveItem);
            return;
        }
        saveItem.setId(id);
        sysAppDataRuleMapper.updateByPrimaryKeySelective(saveItem);
    }

    /**
     * delete
     *
     * @param id id
     */
    public void delete(Long[] id) {
        sysAppDataRuleMapper.deleteByIds(StringUtils.join(id, ","));
    }

}