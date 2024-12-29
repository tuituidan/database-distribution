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
              <el-form-item label="应用名称">
                <span v-text="props.row.appName"></span>
              </el-form-item>
              <el-form-item label="应用标识">
                <span v-text="props.row.appKey"></span>
              </el-form-item>
              <el-form-item label="应用秘钥">
                <span v-text="props.row.appSecret"></span>
              </el-form-item>
              <el-form-item label="推送地址">
                <span v-text="props.row.url"></span>
              </el-form-item>
              <el-form-item label="请求头">
                <div v-for="(item, index) in props.row.headers" :key="item.type+index">
                  <span v-if="item.type==='key-value'" v-text="item.key+'='+item.value"></span>
                  <span v-else-if="item.type==='basic'" class="header-item" v-text="basicToken(item)"></span>
                  <span v-else-if="item.type==='bearer'" class="header-item">Authorization={bearer-jwt-token-30min-timeout}</span>
                </div>
              </el-form-item>
              <el-form-item label="结果解析表达式">
                <span v-text="props.row.resultExp"></span>
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
      <el-table-column label="应用标识" align="center" prop="appKey" :show-overflow-tooltip="true"/>
      <el-table-column label="应用名称" align="center" prop="appName" :show-overflow-tooltip="true"/>
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
    <sys-app-edit ref="refDataSourceEdit" @refresh="getList"></sys-app-edit>
  </div>
</template>

<script>
export default {
  name: "sys-app-list",
  components: {
    'sys-app-edit': () => import('./sys-app-edit')
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
    basicToken(item){
      return 'Authorization=Basic '+window.btoa(item.key+':'+item.value);
    },
    getList() {
      this.loading = true;
      this.$http.get(`/api/v1/sys_app`)
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
      this.$refs.refDataSourceEdit.open(row);
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      this.$modal.confirm(`是否确认删除【${row.name}】数据项？`)
        .then(() => {
          return this.$http.delete(`/api/v1/sys_app/${row.id}`);
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
