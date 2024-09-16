<template>
  <div>
    <div ref="lineChartRef" style="height: 260px;width: 100%;"></div>
  </div>
</template>

<script>
import * as echarts from 'echarts';

export default {
  name: "LineCharts",
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
        legend: {
          data: []
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        toolbox: {
          feature: {
            saveAsImage: {}
          }
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: []
        },
        yAxis: {
          type: 'value'
        },
        series: []
      },
    }
  },
  mounted() {
    this.echartsRef = echarts.init(this.$refs.lineChartRef);
    this.echartsRef.setOption(this.echartsOptions);
    this.loadData();
  },
  methods: {
    loadData() {
      this.echartsOptions.legend.data = [];
      this.echartsOptions.xAxis.data = [];
      this.echartsOptions.series = [];
      this.$http.get(this.url)
        .then(res => {
          for (const item of res[Object.keys(res)[0]]) {
            this.echartsOptions.xAxis.data.push(item.xdata);
          }
          for (const key in res) {
            this.echartsOptions.legend.data.push(key);
            const values = [];
            for (const item of res[key]) {
              values.push(item.ydata);
            }
            this.echartsOptions.series.push({
              name: key,
              type: 'line',
              stack: 'Total',
              data: values
            });
          }
          this.echartsRef.setOption(this.echartsOptions);
        })
    }
  },
}
</script>

<style scoped>

</style>
