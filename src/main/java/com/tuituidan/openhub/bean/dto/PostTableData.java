package com.tuituidan.openhub.bean.dto;

import com.alibaba.fastjson2.JSONObject;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * PostTableData.
 *
 * @author tuituidan
 * @version 1.0
 * @date 2024/9/1
 */
@Getter
@Setter
@Accessors(chain = true)
public class PostTableData {

    private String database;

    private String table;

    private String[] primaryKey;

    private String type;

    private List<String> columns;

    private List<JSONObject> dataList;

}
