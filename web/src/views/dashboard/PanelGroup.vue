<template>
  <el-row :gutter="40" class="panel-group">
    <el-col :xs="12" :sm="8" :lg="6" class="card-panel-col">
      <div class="card-panel">
        <div class="card-panel-icon-wrapper icon-people">
          <svg-icon icon-class="druid" class-name="card-panel-icon"/>
        </div>
        <div class="card-panel-description">
          <div class="card-panel-text">
            数据日志
          </div>
          <div class="card-panel-num"><span v-text="dataLogCount"></span>条</div>
        </div>
      </div>
    </el-col>
    <el-col :xs="12" :sm="8" :lg="6" class="card-panel-col">
      <div class="card-panel">
        <div class="card-panel-icon-wrapper icon-message">
          <svg-icon icon-class="tree" class-name="card-panel-icon"/>
        </div>
        <div class="card-panel-description">
          <div class="card-panel-text">
            推送日志
          </div>
          <div class="card-panel-num"><span v-text="pushLogCount"></span>条</div>
        </div>
      </div>
    </el-col>
    <el-col :xs="12" :sm="8" :lg="6" class="card-panel-col">
      <div class="card-panel">
        <div class="card-panel-icon-wrapper icon-money">
          <svg-icon icon-class="checkbox" class-name="card-panel-icon"/>
        </div>
        <div class="card-panel-description">
          <div class="card-panel-text">
            本日推送成功
          </div>
          <div class="card-panel-num"><span v-text="pushSuccessLogCount"></span>条</div>
        </div>
      </div>
    </el-col>
    <el-col :xs="12" :sm="8" :lg="6" class="card-panel-col">
      <div class="card-panel">
        <div class="card-panel-icon-wrapper icon-shopping">
          <svg-icon icon-class="bug" class-name="card-panel-icon"/>
        </div>
        <div class="card-panel-description">
          <div class="card-panel-text">
            本日推送失败
          </div>
          <div class="card-panel-num"><span v-text="pushFailLogCount"></span>条</div>
        </div>
      </div>
    </el-col>
  </el-row>
</template>

<script>

export default {
  data() {
    return {
      dataLogCount: 0,
      pushLogCount: 0,
      pushSuccessLogCount: 0,
      pushFailLogCount: 0,
    }
  },
  mounted() {
    this.init();
  },
  methods: {
    init() {
      this.$http.get('/api/v1/home/data_log/count')
        .then(res => {
          this.dataLogCount = res;
        });
      this.$http.get('/api/v1/home/push_log/count')
        .then(res => {
          this.pushLogCount = res;
        });
      this.$http.get('/api/v1/home/push_log/01/count')
        .then(res => {
          this.pushSuccessLogCount = res;
        });
      this.$http.get('/api/v1/home/push_log/02/count')
        .then(res => {
          this.pushFailLogCount = res;
        })
    },
  }
}
</script>

<style lang="scss" scoped>
.panel-group {
  margin-top: 18px;

  .card-panel-col {
    margin-bottom: 32px;
  }

  .card-panel {
    height: 108px;
    cursor: pointer;
    font-size: 12px;
    position: relative;
    overflow: hidden;
    color: #666;
    background: #fff;
    box-shadow: 4px 4px 40px rgba(0, 0, 0, .05);
    border-color: rgba(0, 0, 0, .05);

    &:hover {
      .card-panel-icon-wrapper {
        color: #fff;
      }

      .icon-people {
        background: #40c9c6;
      }

      .icon-message {
        background: #36a3f7;
      }

      .icon-money {
        background: #34bfa3;
      }

      .icon-shopping {
        background: #f4516c;
      }
    }

    .icon-people {
      color: #40c9c6;
    }

    .icon-message {
      color: #36a3f7;
    }

    .icon-money {
      color: #34bfa3;
    }

    .icon-shopping {
      color: #f4516c;
    }

    .card-panel-icon-wrapper {
      float: left;
      margin: 14px 0 0 14px;
      padding: 16px;
      transition: all 0.38s ease-out;
      border-radius: 6px;
    }

    .card-panel-icon {
      float: left;
      font-size: 48px;
    }

    .card-panel-description {
      float: right;
      font-weight: bold;
      margin: 26px;
      margin-left: 0px;

      .card-panel-text {
        line-height: 18px;
        color: rgba(0, 0, 0, 0.45);
        font-size: 16px;
        margin-bottom: 12px;
      }

      .card-panel-num {
        font-size: 20px;
      }
    }
  }
}

@media (max-width: 550px) {
  .card-panel-description {
    display: none;
  }

  .card-panel-icon-wrapper {
    float: none !important;
    width: 100%;
    height: 100%;
    margin: 0 !important;

    .svg-icon {
      display: block;
      margin: 14px auto !important;
      float: none !important;
    }
  }
}
</style>
