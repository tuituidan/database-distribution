<template>
  <div>
    <el-card shadow="never">
      <div slot="header" class="card-header">
        <span>接收数据表配置</span>
        <el-button
          type="primary"
          plain
          icon="el-icon-document"
          size="mini"
          :disabled="!this.appId"
          @click="saveHandler"
        >保存
        </el-button>
      </div>
      <el-tree
        ref="refTree"
        default-expand-all
        :data="treeList"
        :props="{label: 'name'}"
        node-key="id"
        highlight-current
        show-checkbox>
      </el-tree>
    </el-card>
  </div>
</template>

<script>
export default {
  name: "data-source-list",
  data() {
    return {
      // 遮罩层
      loading: false,
      treeList: [],
      appId: '',
    };
  },
  mounted() {
    this.loadTree();
  },
  methods: {
    loadTree() {
      this.loading = true;
      this.$http.get(`/api/v1/sys_app/database_config/tree`)
        .then(res => {
          this.treeList = res;
        })
        .finally(() => {
          this.loading = false;
        });
    },
    loadConfig(row) {
      if (row) {
        this.appId = row.id;
      }
      this.loading = true;
      console.log(this.appId);
      this.$http.get(`/api/v1/sys_app/${this.appId}/database_config`)
        .then(res => {
          this.$refs.refTree.setCheckedKeys(res);
        })
        .finally(() => {
          this.loading = false;
        });
    },
    saveHandler() {
      if (!this.appId) {
        return;
      }
      const keys = this.$refs.refTree.getCheckedKeys(true);
      this.loading = true;
      this.$http.post(`/api/v1/sys_app/${this.appId}/database_config`, keys)
        .then(() => {
          this.$modal.msgSuccess('保存成功');
          this.loadConfig();
        })
        .finally(() => {
          this.loading = false;
        });
    },
  }
}
</script>

<style scoped lang="scss">
::v-deep .el-card__header {
  padding: 11px 15px;
}

::v-deep .el-card__body {
  padding: 10px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  align-items: center;
}
</style>
