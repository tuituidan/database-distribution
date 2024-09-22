<template>
  <div class="app-container home">
    <el-form :model="queryParams"
             ref="queryForm" size="small" :inline="true" label-width="68px">
      <el-form-item label="应用" prop="appId">
        <el-select v-model="queryParams.appId" clearable placeholder="请选择应用">
          <el-option
            v-for="item in appList"
            :key="item.id"
            :label="item.appName"
            :value="item.id">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="推送状态" prop="status">
        <el-select v-model="queryParams.status" clearable placeholder="请选择推送状态">
          <el-option
            v-for="item in statusOptions"
            :key="item.code"
            :label="item.name"
            :value="item.code">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="日志ID" prop="dataLogId">
        <el-input
          v-model="queryParams.dataLogId"
          placeholder="请输入日志ID"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
    <el-row :gutter="10" class="mb8 mt5">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          size="small"
          icon="el-icon-upload"
          @click="handlerPush"
        >重新推送
        </el-button>
      </el-col>
    </el-row>
    <el-table v-loading="loading"
              :data="table.data"
              ref="dataTable"
              @selection-change="selections = $refs.dataTable.selection"
              border stripe
              :default-sort="{prop: 'pushTime', order: 'descending'}">
      <el-table-column type="selection" width="50" align="center"/>
      <el-table-column label="序号" type="index" width="50" align="center" :index="table.index"/>
      <el-table-column label="日志ID" align="center" prop="dataLogId" show-overflow-tooltip/>
      <el-table-column label="应用名称" align="center" prop="appName" show-overflow-tooltip/>
      <el-table-column label="推送状态" align="center" prop="status" show-overflow-tooltip>
        <template slot-scope="scope">
          <el-tag v-if="scope.row.status === '01'" type="success" effect="plain">成功</el-tag>
          <el-tag v-else type="danger" effect="plain">失败</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="推送结果" align="center" prop="response" show-overflow-tooltip/>
      <el-table-column label="推送时间" align="center" prop="pushTime" show-overflow-tooltip/>
      <el-table-column label="推送耗时(毫秒)" align="center" prop="costTime" show-overflow-tooltip/>
      <el-table-column label="推送次数" align="center" prop="pushTimes" show-overflow-tooltip/>
    </el-table>

    <pagination
      v-show="table.total>0"
      :total="table.total"
      :page.sync="queryParams.pageIndex"
      :limit.sync="queryParams.limit"
      @pagination="pageChange"
    />
    <push-dialog ref="refPushDialog" @refresh="handleQuery"></push-dialog>
  </div>
</template>

<script>

export default {
  name: "Index",
  components: {
    'push-dialog': () => import('./push-dialog'),
  },
  data() {
    return {
      // 选中数组
      selections: [],
      // 遮罩层
      loading: false,
      // 查询参数
      queryParams: {
        pageIndex: 1,
        offset: 0,
        limit: 10,
        dataLogId: '',
        appId: '',
        status: '',
        sort: '-pushTime',
      },
      appList: [],
      statusOptions: [
        {
          code: '01',
          name: '成功',
        },
        {
          code: '02',
          name: '失败',
        }
      ],
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
        dataLogId: '',
        appId: '',
        status: '',
        sort: this.queryParams.sort
      };
      this.handleQuery();
    },
    handlerPush() {
      this.$refs.refPushDialog.open(this.selections)
    }
  }
};
</script>

