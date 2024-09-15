<template>
  <div class="dashboard-editor-container">
    <panel-group />
    <el-row>
      <el-col :span="12">
        <today-data-log-line></today-data-log-line>
      </el-col>
      <el-col :span="12">
        <today-push-log-line></today-push-log-line>
      </el-col>
      <el-col :span="12">
        <last-month-data-log-line></last-month-data-log-line>
      </el-col>
      <el-col :span="12">
        <last-month-push-log-line></last-month-push-log-line>
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  name: 'Index',
  components: {
    PanelGroup: ()=>import('./dashboard/PanelGroup'),
    'today-data-log-line': ()=>import('./dashboard/TodayDataLogLine'),
    'today-push-log-line': ()=>import('./dashboard/TodayPushLogLine'),
    'last-month-data-log-line': ()=>import('./dashboard/LastMonthDataLogLine'),
    'last-month-push-log-line': ()=>import('./dashboard/LastMonthPushLogLine'),
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
