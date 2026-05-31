<template>
  <div class="app-container">
    <schedule-task></schedule-task>
    <warning-email style="width: 500px;margin-top: 20px"></warning-email>
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
