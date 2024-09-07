package com.tuituidan.openhub.translator;

import com.github.benmanes.caffeine.cache.Cache;
import com.tuituidan.openhub.bean.entity.SysDataSource;
import com.tuituidan.openhub.mapper.SysDataSourceMapper;
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
public class DatasourceTranslator implements ITranslator<DatasourceAnno> {

    @Resource
    private Cache<Long, SysDataSource> sysDataSourceCache;

    @Resource
    private SysDataSourceMapper sysDataSourceMapper;

    @Override
    public String getFieldName(String fieldName) {
        return "datasourceName";
    }

    @Override
    public String translate(TranslationParameter translationParameter) {
        Long value = (Long) translationParameter.getFieldValue();
        if (Objects.isNull(value)) {
            return StringUtils.EMPTY;
        }
        return Optional.ofNullable(sysDataSourceCache.get(value,
                        key -> sysDataSourceMapper.selectByPrimaryKey(key)))
                .map(SysDataSource::getName).orElse(StringUtils.EMPTY);
    }

}
