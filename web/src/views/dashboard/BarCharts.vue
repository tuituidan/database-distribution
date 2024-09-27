<template>
  <div>
    <div ref="barChartRef" style="height: 360px;width: 100%; padding: 20px"></div>
  </div>
</template>

<script>
import * as echarts from 'echarts';

export default {
  name: "BarCharts",
  props: {
    title: {
      type: String
    },
    url: {
      type: String
    }
  },
  data() {
    return {
      echartsRef: {},
      echartsOptions: {
        title: {
          text: this.title
        },
        tooltip: {
          trigger: 'axis'
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: [],
        },
        yAxis: {
          type: 'value'
        },
        series: []
      },
    }
  },
  mounted() {
    this.echartsRef = echarts.init(this.$refs.barChartRef);
    this.echartsRef.setOption(this.echartsOptions);
    this.loadData();
  },
  methods: {
    loadData() {
      this.echartsOptions.xAxis.data = [];
      this.echartsOptions.series = [];
      this.$http.get(this.url)
        .then(res => {
          const seriesData = [];
          for (const item of res) {
            this.echartsOptions.xAxis.data.push(item.xdata);
            seriesData.push(item.ydata);
          }
          this.echartsOptions.series.push({
            type: 'bar',
            data: seriesData
          });
          if (seriesData.length > 5) {
            this.echartsOptions.xAxis.axisLabel = {
              rotate: 30 // 文字倾斜 30 度
            }
          }
          this.echartsRef.setOption(this.echartsOptions);
        })
    }
  },
}
</script>

<style scoped>

</style>
