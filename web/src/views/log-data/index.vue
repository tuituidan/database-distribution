<template>
  <div class="app-container home">
    <el-form :model="queryParams"
             ref="queryForm" size="small" :inline="true" label-width="68px">
      <el-form-item label="数据库" prop="databaseName">
        <el-input
          v-model="queryParams.databaseName"
          placeholder="请输入数据库"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="表名" prop="tableName">
        <el-input
          v-model="queryParams.tableName"
          placeholder="请输入表名"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="table.data"
              border stripe
              @expand-change="expandChange"
              :default-sort="{prop: 'createTime', order: 'descending'}">
      <el-table-column type="expand" label="展开">
        <template slot-scope="props">
          <div style="padding: 10px 20px">
            <el-table :data="props.row.children" border :header-cell-style="{backgroundColor: 'white'}">
              <el-table-column label="序号" type="index" width="50" align="center"/>
              <el-table-column label="应用名称" align="center" prop="appName" show-overflow-tooltip/>
              <el-table-column label="推送状态" align="center" prop="status" show-overflow-tooltip/>
              <el-table-column label="推送结果" align="center" prop="response" show-overflow-tooltip/>
              <el-table-column label="推送时间" align="center" prop="pushTime" show-overflow-tooltip/>
              <el-table-column label="推送耗时(毫秒)" align="center" prop="costTime" show-overflow-tooltip/>
            </el-table>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="序号" type="index" width="50" align="center" :index="table.index"/>
      <el-table-column label="日志ID" align="center" prop="id" show-overflow-tooltip/>
      <el-table-column label="数据源" align="center" prop="datasourceName" show-overflow-tooltip/>
      <el-table-column label="数据库" align="center" prop="databaseName" show-overflow-tooltip/>
      <el-table-column label="表名" align="center" prop="tableName" show-overflow-tooltip/>
      <el-table-column label="操作类型" align="center" prop="operTypeText" show-overflow-tooltip/>
      <el-table-column label="数据" align="center" prop="dataLog" show-overflow-tooltip/>
      <el-table-column label="消息时间" align="center" prop="createTime" show-overflow-tooltip/>
    </el-table>

    <pagination
      v-show="table.total>0"
      :total="table.total"
      :page.sync="queryParams.pageIndex"
      :limit.sync="queryParams.limit"
      @pagination="pageChange"
    />
  </div>
</template>

<script>

export default {
  name: "Index",
  data() {
    return {
      // 遮罩层
      loading: false,
      // 查询参数
      queryParams: {
        pageIndex: 1,
        offset: 0,
        limit: 10,
        databaseName: '',
        tableName: '',
        sort: '-createTime',
      },
      table: {
        total: 0,
        index: 1,
        data: []
      },
    };
  },
  mounted() {
    this.handleQuery();
  },
  methods: {
    pageChange(page) {
      this.queryParams.pageIndex = page.page;
      this.queryParams.offset = this.queryParams.limit * (page.page - 1);
      this.handleQuery();
    },
    handleQuery() {
      this.loading = true;
      this.$http.get('/api/v1/data_log', {params: this.queryParams})
        .then(res => {
          this.table = res;
        })
        .finally(() => {
          this.loading = false;
        });
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.queryParams = {
        pageIndex: 1,
        offset: 0,
        limit: 10,
        databaseName: '',
        tableName: '',
        sort: this.queryParams.sort
      };
      this.handleQuery();
    },
    expandChange(row) {
      this.$http.get(`/api/v1/data_log/${row.id}/push_log`)
        .then(res => {
          row.children = res;
        });
    },
  }
};
</script>

