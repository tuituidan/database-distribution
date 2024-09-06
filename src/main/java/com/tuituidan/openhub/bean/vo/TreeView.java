package com.tuituidan.openhub.bean.vo;

import com.tuituidan.tresdin.util.tree.TreeData;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * AppDatabaseConfigView.
 *
 * @author tuituidan
 * @version 1.0
 * @date 2024/9/5
 */
@Getter
@Setter
@Accessors(chain = true)
public class TreeView implements TreeData<TreeView> {

    private String id;

    private Integer sort;

    private String name;

    private String pid;

    private List<TreeView> children;

}
