package com.tuituidan.openhub.config.mybatis;

import com.alibaba.fastjson.JSON;
import com.tuituidan.openhub.bean.dto.SysAppHeaderParam;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

/**
 * AppHeaderTypeHandler.
 *
 * @author tuituidan
 * @version 1.0
 * @date 2024/12/26
 */
@Slf4j
public class AppHeaderTypeHandler extends BaseTypeHandler<List<SysAppHeaderParam>> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<SysAppHeaderParam> parameter,
            JdbcType jdbcType) throws SQLException {
        ps.setString(i, JSON.toJSONString(parameter));
    }

    @Override
    public List<SysAppHeaderParam> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return toJavaObject(rs.getString(columnName));
    }

    @Override
    public List<SysAppHeaderParam> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return toJavaObject(rs.getString(columnIndex));
    }

    @Override
    public List<SysAppHeaderParam> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return toJavaObject(cs.getString(columnIndex));
    }

    private List<SysAppHeaderParam> toJavaObject(String columnValue) {
        return JSON.parseArray(columnValue, SysAppHeaderParam.class);
    }

}
