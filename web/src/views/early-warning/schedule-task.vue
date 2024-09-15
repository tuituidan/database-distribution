<template>
  <div>
    <el-row type="flex" justify="space-between">
      <div></div>
      <el-row class="mb8 mt5">
        <el-col :span="1.5">
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
            @click="clickHandler('handler')"
          >执行
          </el-button>
        </el-col>
      </el-row>
    </el-row>
    <el-table
      stripe
      border
      ref="dataTable"
      v-loading="loading"
      @selection-change="selections = $refs.dataTable.selection"
      :data="dataList">
      <el-table-column type="selection" width="50" align="center"/>
      <el-table-column label="序号" type="index" width="50" align="center"/>
      <el-table-column label="任务名称" align="center" prop="name" :show-overflow-tooltip="true"/>
      <el-table-column label="cron表达式" align="center" prop="cron" :show-overflow-tooltip="true" width="130"/>
      <el-table-column label="cron描述" align="center" prop="desc" :show-overflow-tooltip="true"/>
      <el-table-column label="状态" align="center" prop="status" show-overflow-tooltip width="80">
        <template slot-scope="scope">
          <div>
            <el-tag v-if="scope.row.status === 'stop'" type="danger" effect="plain">已停止</el-tag>
            <el-tag v-else type="success" effect="plain">已启动</el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="下次执行时间" align="center" prop="nextRunTime" :show-overflow-tooltip="true" width="160"/>
    </el-table>
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
      this.$http.get(`/api/v1/tresdin/schedule/task/list`)
        .then(res => {
          this.dataList = res;
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
  }
}
</script>

<style scoped>

</style>
