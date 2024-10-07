<template>
  <div class="app-container">
    <el-row :gutter="10">
      <el-col :span="14">
        <schedule-task></schedule-task>
      </el-col>
      <el-col :span="10">
        <warning-email></warning-email>
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  name: "early-warning-index",
  components: {
    'schedule-task': () => import('./schedule-task'),
    'warning-email': () => import('./warning-email'),
  },
  data() {
    return {};
  },
  mounted() {
  },
  methods: {
  },
}
</script>

<style scoped>

</style>
