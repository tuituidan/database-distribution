package com.tuituidan.openhub.service;

import com.tuituidan.openhub.bean.dto.SysAppParam;
import com.tuituidan.openhub.bean.entity.SysApp;
import com.tuituidan.openhub.mapper.SysAppMapper;
import com.tuituidan.tresdin.mybatis.QueryHelper;
import com.tuituidan.tresdin.mybatis.bean.PageParam;
import com.tuituidan.tresdin.page.PageData;
import com.tuituidan.tresdin.util.BeanExtUtils;
import java.util.List;
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

    /**
     * selectPage
     *
     * @param pageParam pageParam
     * @return PageData
     */
    public PageData<List<SysApp>> selectPage(PageParam pageParam) {
        return QueryHelper.queryPage(pageParam.getOffset(), pageParam.getLimit(), sysAppMapper::selectAll);
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
