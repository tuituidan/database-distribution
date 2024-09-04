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
      :data="dataList">
      <el-table-column label="序号" type="index" width="50" align="center"/>
      <el-table-column label="数据库名" align="center" prop="databaseName" :show-overflow-tooltip="true"/>
      <el-table-column label="表名" align="center" prop="tableName" :show-overflow-tooltip="true"/>
      <el-table-column label="表说明" align="center" prop="tableComment" :show-overflow-tooltip="true"/>
      <el-table-column label="主键名" align="center" prop="primaryKey" :show-overflow-tooltip="true"/>
      <el-table-column label="增量字段" align="center" prop="incrementKey" :show-overflow-tooltip="true"/>
      <el-table-column label="增量字段类型" align="center" prop="incrementTypeText" :show-overflow-tooltip="true"/>
      <el-table-column label="操作" align="center" width="110" class-name="small-padding fixed-width">
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
      dataList: [],
      selectRow: '',
    };
  },
  mounted() {
  },
  methods: {
    loadConfig(row) {
      this.loading = true;
      this.$http.get(`/api/v1/database_config`, {params: {datasourceId: row.id}})
        .then(res => {
          this.dataList = res;
        })
        .finally(() => {
          this.loading = false;
        });
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
