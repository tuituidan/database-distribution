package com.tuituidan.openhub.translator;

import com.github.benmanes.caffeine.cache.Cache;
import com.tuituidan.openhub.bean.entity.SysApp;
import com.tuituidan.openhub.mapper.SysAppMapper;
import com.tuituidan.tresdin.datatranslate.bean.TranslationParameter;
import com.tuituidan.tresdin.datatranslate.translator.ITranslator;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * SysAppTranslator.
 *
 * @author tuituidan
 * @version 1.0
 * @date 2024/9/7
 */
@Component
@Slf4j
public class SysAppTranslator implements ITranslator<SysAppAnno> {

    @Resource
    private Cache<Long, SysApp> sysAppCache;

    @Resource
    private SysAppMapper sysAppMapper;

    @Override
    public String getFieldName(String fieldName) {
        return "appName";
    }

    @Override
    public String translate(TranslationParameter translationParameter) {
        Long value = (Long) translationParameter.getFieldValue();
        if (Objects.isNull(value)) {
            return StringUtils.EMPTY;
        }
        return Optional.ofNullable(sysAppCache.get(value, key -> sysAppMapper.selectByPrimaryKey(key)))
                .map(SysApp::getAppName).orElse(StringUtils.EMPTY);
    }

}
