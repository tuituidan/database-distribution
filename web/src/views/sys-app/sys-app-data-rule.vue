<template>
  <div>
    <div class="table-title" v-text="tableNode.name?tableNode.name:'请选择数据表进行配置'"></div>
    <el-row type="flex" justify="space-between">
      <div></div>
      <el-row class="mb8 mt5" :gutter="10">
        <el-col :span="1.5">
          <el-button
            type="success"
            plain
            icon="el-icon-plus"
            size="small"
            :disabled="!Boolean(tableNode.id)"
            @click="openEditDialog()"
          >新增
          </el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button
            type="primary"
            plain
            size="small"
            icon="el-icon-edit"
            v-btn-single="selections"
            @click="openEditDialog(selections[0])"
          >修改
          </el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button
            type="danger"
            plain
            size="small"
            icon="el-icon-delete"
            v-btn-multiple="selections"
            @click="handleDelete"
          >删除
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
      <el-table-column label="过滤规则类型" align="center" prop="ruleTypeText" :show-overflow-tooltip="true"/>
      <el-table-column label="过滤规则表达式" align="center" prop="ruleExp" :show-overflow-tooltip="true"/>
    </el-table>
    <sys-app-data-rule-edit ref="refAppDataRuleEdit" @refresh="getList"></sys-app-data-rule-edit>
  </div>
</template>

<script>
export default {
  name: "sys-app-list",
  components: {
    'sys-app-data-rule-edit': () => import('./sys-app-data-rule-edit')
  },
  data() {
    return {
      // 选中数组
      selections: [],
      // 遮罩层
      loading: false,
      dataList: [],
      tableNode: {},
    };
  },
  methods: {
    loadRule(node) {
      this.tableNode = node;
      this.getList();
    },
    getList() {
      this.loading = true;
      this.$http.get(`/api/v1/sys_app_data_rule/app/${this.tableNode.appId}/database_config/${this.tableNode.id}`)
        .then(res => {
          this.dataList = res;
        })
        .finally(() => {
          this.loading = false;
        });
    },
    /** 修改按钮操作 */
    openEditDialog(row) {
      if (!row) {
        row = {
          appId: this.tableNode.appId,
          databaseConfigId: this.tableNode.id,
        };
      }
      this.$refs.refAppDataRuleEdit.open(row);
    },
    /** 删除按钮操作 */
    handleDelete() {
      if(!this.selections.length){
        return;
      }
      const ids = this.selections.map(it => it.id);
      this.$modal.confirm(`是否确认删除选择的数据项？`)
        .then(() => {
          return this.$http.delete(`/api/v1/sys_app_data_rule/${ids}`);
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
.table-title {
  font-size: 14px;
  color: #606266;
  margin-bottom: 15px;
  text-align: center;
}
</style>
