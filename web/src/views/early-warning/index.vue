<template>
  <div class="app-container">
    <el-row>
      <el-col :span="13">
        <schedule-task></schedule-task>
      </el-col>
      <el-col :span="9" :offset="2">
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
