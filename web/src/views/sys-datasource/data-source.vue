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
      :data="dataList">
      <el-table-column type="expand" label="展开">
        <template slot-scope="props">
          <div class="table-expand">
            <el-form :model="props.row" label-width="120px">
              <el-form-item label="数据源名">
                <span v-text="props.row.name"></span>
              </el-form-item>
              <el-form-item label="数据库地址">
                <span v-text="props.row.host"></span>
              </el-form-item>
              <el-form-item label="数据库端口">
                <span v-text="props.row.port"></span>
              </el-form-item>
              <el-form-item label="数据库用户名">
                <span v-text="props.row.username"></span>
              </el-form-item>
              <el-form-item label="服务ID">
                <span v-text="props.row.serverId"></span>
              </el-form-item>
              <el-form-item label="时区">
                <span v-text="props.row.timeZone"></span>
              </el-form-item>
            </el-form>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="选择" width="50" align="center">
        <template slot-scope="scope">
          <el-radio v-model="selectRow" :label="scope.row.id"><i></i></el-radio>
        </template>
      </el-table-column>
      <el-table-column label="数据源名" align="center" prop="name" :show-overflow-tooltip="true"/>
      <el-table-column label="地址" align="center" prop="host" :show-overflow-tooltip="true"/>
      <el-table-column label="端口" align="center" prop="port" :show-overflow-tooltip="true"/>
      <el-table-column label="用户名" align="center" prop="username" :show-overflow-tooltip="true"/>
      <el-table-column label="状态" align="center">
        <template slot-scope="scope">
          <el-switch
            v-model="scope.row.status"
            active-value="01"
            inactive-value="02"
            @change="handleStatusChange(scope.row)"
          ></el-switch>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="110" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click.stop="openEditDialog(scope.row)"
          >修改
          </el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click.stop="handleDelete(scope.row)"
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
    this.getList();
  },
  methods: {
    getList() {
      this.loading = true;
      this.$http.get(`/api/v1/datasource`)
        .then(res => {
          this.dataList = res;
          if (this.dataList.length > 0) {
            const firstItem = this.dataList[0];
            this.rowClickHandler(firstItem);
            this.currentRowChange(firstItem);
          }
        })
        .finally(() => {
          this.loading = false;
        });
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
      if (row && row.status === '01') {
        this.$modal.msgError("请先停用再进行修改");
        return;
      }
      this.$refs.refDataSourceEdit.open(row);
    },
    handleStatusChange(row) {
      let text = row.status === "01" ? "启用" : "停用";
      this.$modal.confirm('确认要' + text + '数据源【' + row.name + '】吗？')
        .then(() => {
          return this.$http.patch(`/api/v1/datasource/${row.id}/status/${row.status}`);
        })
        .then(() => {
          this.getList();
          this.$modal.msgSuccess(text + "成功");
        })
        .catch(function () {
          row.status = row.status === "01" ? "02" : "01";
        });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      if (row.status === '01') {
        this.$modal.msgError("请先停用再进行删除");
        return;
      }
      this.$modal.confirm(`是否确认删除【${row.name}】数据项？`)
        .then(() => {
          return this.$http.delete(`/api/v1/datasource/${row.id}`);
        }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {
      });
    },
  }
}
</script>

<style scoped lang="scss">
.table-expand {
  overflow-x: auto;
  .el-form {
    ::v-deep label {
      color: #99a9bf;
    }

    .el-form-item {
      margin-bottom: 0;
      .header-item {
        white-space: nowrap;
        padding-right: 10px;
      }
    }
  }
}
</style>
