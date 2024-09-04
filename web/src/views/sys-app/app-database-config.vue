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
        >保存
        </el-button>
      </div>
      <el-tree
        ref="categoryTree"
        :data="categories"
        :props="{label: 'name'}"
        node-key="id"
        highlight-current
        :default-checked-keys="roleDetail.categoryIds"
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
      categories: [],
      roleDetail: {
        categoryIds: [],
      },
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
