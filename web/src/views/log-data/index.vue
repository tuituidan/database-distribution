<template>
  <div class="app-container home">
    <div>
      <div ref="lineChartRef" style="height: 200px;width: 100%;"></div>
    </div>
    <el-form :model="queryParams"
             ref="queryForm" size="small" :inline="true" label-width="68px">
      <el-form-item label="类名" prop="className">
        <el-input
          v-model="queryParams.className"
          placeholder="请输入参数类名"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="方法名" prop="methodName">
        <el-input
          v-model="queryParams.methodName"
          placeholder="请输入参数方法名"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-refresh"
          size="mini"
          @click="reloadData"
        >重新拉取数据
        </el-button>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="dataList"
              border stripe
              @sort-change="sortChange"
              :default-sort="{prop: 'startTime', order: 'descending'}">
      <el-table-column label="类名" align="center" prop="className" sortable show-overflow-tooltip/>
      <el-table-column label="方法名" align="center" prop="methodName" sortable show-overflow-tooltip/>
      <el-table-column label="总耗时" align="center" prop="time" sortable width="120"/>
      <el-table-column label="总次数" align="center" prop="counter" sortable width="120"/>
      <el-table-column label="平均耗时" align="center" prop="avg" sortable width="120"/>
      <el-table-column label="时间段" align="center" prop="startTime" sortable show-overflow-tooltip>
        <template slot-scope="scope">
          <div v-text="scope.row.startTime+'至'+scope.row.startTime"></div>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageIndex"
      :limit.sync="queryParams.limit"
      @pagination="pageChange"
    />
  </div>
</template>

<script>
import * as echarts from 'echarts';

export default {
  name: "Index",
  data() {
    return {
      // 遮罩层
      loading: false,
      echartsRef: {},
      // 总条数
      total: 0,
      // 查询参数
      queryParams: {
        pageIndex: 1,
        offset: 0,
        limit: 10,
        className: null,
        methodName: null,
        curDate: '',
        sort: '-startTime'
      },
      dataList: [],
      echartsOptions: {
        // ECharts 配置项
        title: {
          left: 'center',
          text: '本日接口调用情况'
        },
        tooltip: {
          trigger: 'axis',
          position: function (pt) {
            return [pt[0], '10%'];
          }
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: []
        },
        yAxis: {
          type: 'value',
          boundaryGap: [0, '100%']
        },
        series: [{
          name: '调用次数',
          type: 'line',
          symbol: 'none',
          sampling: 'lttb',
          itemStyle: {
            color: 'rgb(255, 70, 131)'
          },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              {
                offset: 0,
                color: 'rgb(255, 158, 68)'
              },
              {
                offset: 1,
                color: 'rgb(255, 70, 131)'
              }
            ])
          },
          data: []
        }]
      }
    };
  },
  mounted() {
    this.handleQuery();
    this.echartsRef = echarts.init(this.$refs.lineChartRef);
    this.echartsOptions.title.text = `${this.$route.params.curDate}日接口调用情况`;
    this.echartsRef.setOption(this.echartsOptions);
    this.refreshEcharts();
  },
  methods: {
    reloadData() {
      this.$http.post(`/api/v1/log/load?curDate=${this.$route.params.curDate}`).then(res => {
        this.$notify.success('成功');
        this.handleQuery();
        this.refreshEcharts();
      })
    },
    sortChange(sort) {
      if (sort.order === 'descending') {
        this.queryParams.sort = '-' + sort.prop;
      } else {
        this.queryParams.sort = sort.prop;
      }
      this.handleQuery();
    },
    pageChange(page){
      this.queryParams.pageIndex = page.page;
      this.queryParams.offset = this.queryParams.limit * (page.page - 1);
      this.handleQuery();
    },
    handleQuery() {
      this.queryParams.curDate = this.$route.params.curDate;
      this.$http.get('/api/v1/log/page', {params: this.queryParams}).then(res => {
        this.dataList = res.data;
        this.total = res.total;
      });
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.queryParams = {
        pageIndex: 1,
        pageSize: 10,
        className: '',
        methodName: '',
      };
      this.handleQuery();
    },
    refreshEcharts(){
      this.$http.get('/api/v1/log/statistic', {params: {curDate: this.$route.params.curDate}})
        .then(res=>{
          for (const re of res) {
            this.echartsOptions.xAxis.data.push(re.time+'点');
            this.echartsOptions.series[0].data.push(re.counter);
          }
          this.echartsRef.setOption(this.echartsOptions);
        })
    }
  }
};
</script>

<style scoped lang="scss">
.home {
  blockquote {
    padding: 10px 20px;
    margin: 0 0 20px;
    font-size: 17.5px;
    border-left: 5px solid #eee;
  }

  hr {
    margin-top: 20px;
    margin-bottom: 20px;
    border: 0;
    border-top: 1px solid #eee;
  }

  .col-item {
    margin-bottom: 20px;
  }

  ul {
    padding: 0;
    margin: 0;
  }

  font-family: "open sans", "Helvetica Neue", Helvetica, Arial, sans-serif;
  font-size: 13px;
  color: #676a6c;
  overflow-x: hidden;

  ul {
    list-style-type: none;
  }

  h4 {
    margin-top: 0px;
  }

  h2 {
    margin-top: 10px;
    font-size: 26px;
    font-weight: 100;
  }

  p {
    margin-top: 10px;

    b {
      font-weight: 700;
    }
  }

  .update-log {
    ol {
      display: block;
      list-style-type: decimal;
      margin-block-start: 1em;
      margin-block-end: 1em;
      margin-inline-start: 0;
      margin-inline-end: 0;
      padding-inline-start: 40px;
    }
  }
}
</style>

