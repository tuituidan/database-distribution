<template>
  <div>
    <el-card shadow="never">
      <div slot="header" class="card-header">
        <span>定时任务管理</span>
        <div>
          <el-button
            type="success"
            plain
            size="small"
            icon="el-icon-caret-right"
            v-btn-multiple="selections"
            @click="clickHandler('start')"
          >启动
          </el-button>
          <el-button
            type="danger"
            plain
            size="small"
            icon="el-icon-switch-button"
            v-btn-multiple="selections"
            @click="clickHandler('stop')"
          >停止
          </el-button>
          <el-button
            type="primary"
            plain
            size="small"
            icon="el-icon-refresh"
            v-btn-multiple="selections"
            @click="clickHandler('restart')"
          >重启
          </el-button>
          <el-button
            type="primary"
            plain
            size="small"
            icon="el-icon-coffee-cup"
            v-btn-multiple="selections"
            @click="clickHandler('execute')"
          >执行
          </el-button>
        </div>
      </div>
      <el-table
        stripe
        border
        ref="dataTable"
        v-loading="loading"
        @selection-change="selections = $refs.dataTable.selection"
        @expand-change="expandChange"
        :data="dataList">
        <el-table-column type="selection" width="50" align="center"/>
        <el-table-column label="序号" type="index" width="50" align="center"/>
        <el-table-column type="expand" label="展开日志" width="80">
          <template slot-scope="props">
            <div style="padding: 10px 20px">
              <el-table :data="props.row.children" border :max-height="360" :header-cell-style="{backgroundColor: 'white'}">
                <el-table-column label="序号" type="index" width="50" align="center"/>
                <el-table-column align="center" property="startTime" label="执行时间"
                                 show-overflow-tooltip width="160"></el-table-column>
                <el-table-column align="center" property="costTimeDesc" label="执行耗时"
                                 show-overflow-tooltip width="160"></el-table-column>
                <el-table-column label="执行状态" align="center" prop="success" width="100">
                  <template slot-scope="scope">
                    <el-tag size="small" type="success" v-if="scope.row.success===true">成功</el-tag>
                    <el-tag size="small" type="danger" v-else-if="scope.row.success===false">失败</el-tag>
                    <el-tag size="small" type="warning" v-else>执行中</el-tag>
                  </template>
                </el-table-column>
                <el-table-column property="msg" label="执行结果" show-overflow-tooltip></el-table-column>
              </el-table>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="任务路径" align="center" prop="taskPath" :show-overflow-tooltip="true"/>
        <el-table-column label="任务名称" align="center" prop="taskName" :show-overflow-tooltip="true"/>
        <el-table-column label="cron表达式" align="center" prop="cron" :show-overflow-tooltip="true" width="130"/>
        <el-table-column label="cron描述" align="center" prop="cronDesc" :show-overflow-tooltip="true"/>
        <el-table-column label="状态" align="center" prop="status" show-overflow-tooltip width="80">
          <template slot-scope="scope">
            <div>
              <el-tag v-if="scope.row.status === 'STOP'" type="danger" effect="plain">已停止</el-tag>
              <el-tag v-else type="success" effect="plain">已启动</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="下次执行时间" align="center" prop="nextRunTime" :show-overflow-tooltip="true" width="160"/>
      </el-table>
    </el-card>

  </div>
</template>

<script>

import axios from "axios";

export default {
  name: "schedule-task-list",
  data() {
    return {
      // 遮罩层
      loading: false,
      dataList: [],
      selections: [],
    };
  },
  mounted() {
    this.getList();
  },
  methods: {
    getList() {
      this.loading = true;
      this.dataList = [];
      this.$http.get(`/api/v1/tresdin/schedule/task/list`)
        .then(res => {
          for (const item of res) {
            item.children = [];
            this.dataList.push(item);
          }
        })
        .finally(() => {
          this.loading = false;
        });
    },
    clickHandler(type) {
      if (!this.selections.length) {
        return;
      }
      const https = this.selections.map(item => this.$http
        .post(`/api/v1/tresdin/schedule/task/${item.id}/actions/${type}`));
      axios.all(https).then(() => {
        this.$modal.msgSuccess("操作成功");
        this.getList();
      })
    },
    expandChange(row, expandedRows) {
      if (!expandedRows.map(item => item.id).includes(row.id)) {
        // 控制收起时不加载数据
        return;
      }
      this.$http.get(`/api/v1/tresdin/schedule/task/${row.id}/log/list`)
        .then(res => {
          row.children = res;
        });
    },
  }
}
</script>


<style scoped lang="scss">
::v-deep .el-card__header {
  padding: 11px 15px;
}

::v-deep .el-card__body {
  padding: 10px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  align-items: center;
}
</style>
