<template>
  <div class="app-container">
    <el-row>
      <el-col :span="10">
        <data-source @rowChange="rowChange"></data-source>
      </el-col>
      <el-col :span="13" :offset="1">
        <database-config ref="refDatabaseConfig"></database-config>
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  name: "system-datasource",
  components: {
    'data-source': () => import('./data-source'),
    'database-config': () => import('./database-config'),
  },
  data() {
    return {};
  },
  mounted() {
  },
  methods: {
    rowChange(row) {
      this.$refs.refDatabaseConfig.loadConfig(row);
    }
  },
}
</script>

<style scoped>

</style>
