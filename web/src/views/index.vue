<template>
  <div class="dashboard-editor-container">
    <panel-group />
    <el-row>
      <el-col :span="12">
        <line-charts title="本日数据生成情况" url="/api/v1/home/data_log/today/line"></line-charts>
      </el-col>
      <el-col :span="12">
        <line-charts title="本日数据推送情况" url="/api/v1/home/push_log/today/line"></line-charts>
      </el-col>
      <el-col :span="12">
        <line-charts title="近30天数据生成情况" url="/api/v1/home/data_log/last_month/line"></line-charts>
      </el-col>
      <el-col :span="12">
        <line-charts title="近30天数据推送情况" url="/api/v1/home/push_log/last_month/line"></line-charts>
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  name: 'Index',
  components: {
    PanelGroup: ()=>import('./dashboard/PanelGroup'),
    'line-charts': ()=>import('./dashboard/LineCharts'),
  },
}
</script>

<style lang="scss" scoped>
.dashboard-editor-container {
  padding: 32px;
  background-color: rgb(240, 242, 245);
  position: relative;
}

</style>
