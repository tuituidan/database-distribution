<template>
  <div class="app-container">
    <el-row :gutter="10">
      <el-col :span="8">
        <sys-app @rowChange="rowChange"></sys-app>
      </el-col>
      <el-col :span="8">
        <app-database-config ref="refAppDatabaseConfig" @node-change="nodeChange"></app-database-config>
      </el-col>
      <el-col :span="8">
        <sys-app-data-rule ref="refAppDataRule"></sys-app-data-rule>
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  name: "system-datasource",
  components: {
    'sys-app': () => import('./sys-app'),
    'app-database-config': () => import('./app-database-config'),
    'sys-app-data-rule': () => import('./sys-app-data-rule'),
  },
  data() {
    return {};
  },
  mounted() {
  },
  methods: {
    rowChange(row) {
      this.$refs.refAppDatabaseConfig.loadConfig(row);
    },
    nodeChange(node){
      this.$refs.refAppDataRule.loadRule(node);
    },
  },
}
</script>

<style scoped>

</style>
