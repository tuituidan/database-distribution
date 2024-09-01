<template>
  <div>
    <el-row type="flex" justify="space-between">
      <div></div>
      <el-row class="mb8 mt5">
        <el-col :span="1.5">
          <el-button
            type="success"
            plain
            icon="el-icon-plus"
            size="small"
            @click="openEditDialog()"
          >新增
          </el-button>
        </el-col>
      </el-row>
    </el-row>
    <el-table
      stripe
      border
      highlight-current-row
      ref="dataTable"
      v-loading="loading"
      @row-click="rowClickHandler"
      @current-change="currentRowChange"
      :data="table.data">
      <el-table-column label="序号" type="index" width="50" align="center" :index="table.index"/>
      <el-table-column label="选择" width="50" align="center">
        <template slot-scope="scope">
          <el-radio v-model="selectRow" :label="scope.row.id"><i></i></el-radio>
        </template>
      </el-table-column>
      <el-table-column label="数据源名" align="center" prop="name" :show-overflow-tooltip="true"/>
      <el-table-column label="数据库地址" align="center" prop="host" :show-overflow-tooltip="true"/>
      <el-table-column label="数据库端口" align="center" prop="port" :show-overflow-tooltip="true"/>
      <el-table-column label="数据库名" align="center" prop="database" :show-overflow-tooltip="true"/>
      <el-table-column label="数据库用户名" align="center" prop="username" :show-overflow-tooltip="true"/>
      <el-table-column label="数据库密码" align="center" prop="password" :show-overflow-tooltip="true"/>
      <el-table-column label="服务ID" align="center" prop="serverId" :show-overflow-tooltip="true"/>
      <el-table-column label="操作" align="center" width="100" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="openEditDialog(scope.row)"
          >修改
          </el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
          >删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination
      :pager-count="5"
      layout="total,sizes,prev,pager,next"
      v-show="table.total>0"
      :total="table.total"
      :page.sync="queryParam.pageIndex"
      :limit.sync="queryParam.limit"
      @pagination="pageChange"
    />
    <data-source-edit ref="refDataSourceEdit" @refresh="getList"></data-source-edit>
  </div>
</template>

<script>
export default {
  name: "data-source-list",
  components: {
    'data-source-edit': () => import('./data-source-edit')
  },
  data() {
    return {
      // 遮罩层
      loading: false,
      // 查询参数
      queryParam: {
        pageIndex: 1,
        offset: 0,
        limit: 10,
      },
      table: {
        total: 0,
        index: 1,
        data: []
      },
      selectRow: '',
    };
  },
  mounted() {
    //this.getList();
  },
  methods: {
    getList() {
      this.loading = true;
      this.$http.get(`/api/v1/project`, {params: this.queryParam})
        .then(res => {
          this.table = res;
          this.loading = false;
          if (this.table.data.length > 0) {
            const firstItem = this.table.data[0];
            this.rowClickHandler(firstItem);
            this.currentRowChange(firstItem);
          }
        });
    },
    pageChange(page) {
      this.queryParam.pageIndex = page.page;
      this.queryParam.offset = this.queryParam.limit * (page.page - 1);
      this.getList();
    },
    rowClickHandler(row) {
      this.selectRow = row.id;
    },
    currentRowChange(row) {
      if (row) {
        this.$emit('rowChange', row);
      }
    },
    /** 修改按钮操作 */
    openEditDialog(row) {
      this.$refs.refDataSourceEdit.open(row);
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      this.$modal.confirm(`是否确认删除【${row.roleName}】数据项？`)
        .then(() => {
          return this.$http.delete(`/api/v1/project/${row.id}`);
        }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {
      });
    },
  }
}
</script>

<style scoped>

</style>
