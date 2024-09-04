<template>
  <div class="app-container">
    <el-row>
      <el-col :span="11">
        <sys-app @rowChange="rowChange"></sys-app>
      </el-col>
      <el-col :span="12" :offset="1">
        <app-database-config ref="refAppDatabaseConfig"></app-database-config>
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
  },
  data() {
    return {};
  },
  mounted() {
  },
  methods: {
    rowChange(row) {
      this.$refs.refAppDatabaseConfig.loadConfig(row);
    }
  },
}
</script>

<style scoped>

</style>
