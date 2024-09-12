<template>
  <div>
    <el-row type="flex" justify="space-between">
      <div></div>
      <el-row :gutter="10" class="mb8 mt5">
        <el-col :span="1.5">
          <el-button
            type="primary"
            plain
            size="small"
            icon="el-icon-upload"
            v-btn-multiple="selections"
            @click="handlePush"
          >增量重推
          </el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button
            type="success"
            plain
            icon="el-icon-plus"
            size="small"
            :disabled="!this.source.id"
            @click="openEditDialog()"
          >新增
          </el-button>
        </el-col>
      </el-row>
    </el-row>
    <el-table
      stripe
      border
      ref="dataTable"
      :header-cell-class-name="headerCellClass"
      v-loading="loading"
      @selection-change="selections = $refs.dataTable.selection"
      :data="dataList">
      <el-table-column label="" type="selection" width="50" align="center" :selectable="tableSelectable"/>
      <el-table-column label="数据库名" align="center" prop="databaseName" :show-overflow-tooltip="true"/>
      <el-table-column label="表名" align="center" prop="tableName" :show-overflow-tooltip="true"/>
      <el-table-column label="表说明" align="center" prop="tableComment" :show-overflow-tooltip="true"/>
      <el-table-column label="主键名" align="center" prop="primaryKey" :show-overflow-tooltip="true">
        <template slot-scope="scope">
          <span v-text="Array.isArray(scope.row.primaryKey) && scope.row.primaryKey.join(';')"></span>
        </template>
      </el-table-column>
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
    <database-config-edit ref="refDatabaseConfigEdit" @refresh="loadConfig"></database-config-edit>
    <database-push ref="refDatabasePush"></database-push>
  </div>
</template>

<script>
export default {
  name: "data-source-list",
  components: {
    'database-config-edit': () => import('./database-config-edit'),
    'database-push': () => import('./database-push'),
  },
  data() {
    return {
      // 选中数组
      selections: [],
      // 遮罩层
      loading: false,
      dataList: [],
      selectRow: '',
      source: {},
    };
  },
  mounted() {
  },
  methods: {
    tableSelectable(row) {
      if (!this.selections.length) {
        return true;
      }
      return this.selections[0].incrementType === row.incrementType
    },
    headerCellClass(row) {
      if (row.columnIndex === 0 && row.rowIndex === 0) {
        return 'table-cell-hidden';
      }
    },
    loadConfig(row) {
      if (row) {
        this.source = row;
      }
      this.loading = true;
      this.$http.get(`/api/v1/database_config`, {params: {datasourceId: this.source.id}})
        .then(res => {
          this.dataList = res;
        })
        .finally(() => {
          this.loading = false;
        });
    },
    /** 修改按钮操作 */
    openEditDialog(row) {
      this.$refs.refDatabaseConfigEdit.open(this.source.id, row);
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      this.$modal.confirm(`是否确认删除【${row.databaseName}-${row.tableName}】数据项？`)
        .then(() => {
          return this.$http.delete(`/api/v1/database_config/${row.id}`);
        }).then(() => {
        this.loadConfig();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {
      });
    },
    handlePush() {
      if (!this.selections.length) {
        return;
      }
      this.$refs.refDatabasePush.open(this.source, this.selections)
    },
  }
}
</script>

<style scoped lang="scss">
.el-table {
  ::v-deep .table-cell-hidden {
    .el-checkbox {
      display: none;
    }
  }
}
</style>
