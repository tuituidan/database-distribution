package com.tuituidan.openhub.bean.vo;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.tuituidan.openhub.consts.Consts;
import com.tuituidan.openhub.translator.DatasourceAnno;
import com.tuituidan.tresdin.datatranslate.translator.dict.DictType;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * SysApp.
 *
 * @author tuituidan
 * @version 1.0
 * @date 2024/8/31
 */
@Getter
@Setter
@Accessors(chain = true)
public class SysDataLogView {

    private Long id;

    @DatasourceAnno
    private Long datasourceId;

    private Long databaseConfigId;

    private String databaseName;

    private String tableName;

    private String primaryKey;

    @DictType("2000000002")
    private String operType;

    private String dataLog;

    @JsonFormat(pattern = Consts.TIME_PATTERN)
    @JSONField(format = Consts.TIME_PATTERN)
    private LocalDateTime createTime;

    @JsonFormat(pattern = Consts.TIME_PATTERN)
    @JSONField(format = Consts.TIME_PATTERN)
    private LocalDateTime updateTime;

    private List<String> children = new ArrayList<>();
}
