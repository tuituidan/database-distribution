package com.tuituidan.openhub.bean.vo;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * DataConfigView.
 *
 * @author tuituidan
 * @version 1.0
 * @date 2024/11/10
 */
@Getter
@Setter
@Accessors(chain = true)
public class DataConfigView {

    private Long id;

    private Long datasourceId;

    private String databaseName;

    private String tableName;

    private String tableComment;

    private String[] primaryKey;

    private String incrementKey;

    private String incrementType;

    private String[] recordColumn;

    private String timeZone;

    private JdbcTemplate jdbcTemplate;

    private List<TableStruct> tableStruct;

    private List<SysAppView> appList;

}
