<template>
  <div class="app-container home">
    <el-form :model="queryParams"
             ref="queryForm" size="small" :inline="true" label-width="68px">
      <el-form-item label="应用" prop="appId">
        <el-select v-model="queryParams.appId" placeholder="请选择应用">
          <el-option
            v-for="item in appList"
            :key="item.id"
            :label="item.appName"
            :value="item.id">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="table.data"
              border stripe
              :default-sort="{prop: 'pushTime', order: 'descending'}">
      <el-table-column label="应用名称" align="center" prop="appName" show-overflow-tooltip/>
      <el-table-column label="推送状态" align="center" prop="status" show-overflow-tooltip/>
      <el-table-column label="推送结果" align="center" prop="response" show-overflow-tooltip/>
      <el-table-column label="推送时间" align="center" prop="pushTime" show-overflow-tooltip/>
      <el-table-column label="推送耗时" align="center" prop="costTime" show-overflow-tooltip/>
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
        sort: '-pushTime',
      },
      appList: [],
      table: {
        total: 0,
        index: 1,
        data: []
      },
    };
  },
  mounted() {
    this.handleQuery();
    this.getAppList();
  },
  methods: {
    getAppList() {
      this.$http.get(`/api/v1/sys_app`)
        .then(res => {
          this.appList = res;
        });
    },
    pageChange(page) {
      this.queryParams.pageIndex = page.page;
      this.queryParams.offset = this.queryParams.limit * (page.page - 1);
      this.handleQuery();
    },
    handleQuery() {
      this.loading = true;
      this.$http.get('/api/v1/push_log', {params: this.queryParams})
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
  }
};
</script>

