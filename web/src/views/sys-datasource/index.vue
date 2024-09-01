<template>
  <div class="app-container">
    <el-row>
      <el-col :span="15">
        <data-source @rowChange="rowChange"></data-source>
      </el-col>
      <el-col :span="8" :offset="1">
        <data-source-config ref="refFile"></data-source-config>
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  name: "system-datasource",
  components: {
    'data-source': () => import('./data-source'),
    'data-source-config': () => import('./data-source-config'),
  },
  data() {
    return {};
  },
  mounted() {
  },
  methods: {
    rowChange(row){

    }
  },
}
</script>

<style scoped>

</style>
