<template>
  <el-dialog title="数据重推" :visible.sync="show"
             :close-on-click-modal="false"
             @close="onCloseDialog"
             width="600px" append-to-body>
    <p v-if="noticeText">
      <el-tag>
        <i class="el-icon-question"></i><span style="margin-left: 10px" v-text="noticeText"></span>
      </el-tag>
    </p>
    <p>
      <el-progress :text-inside="true" :stroke-width="24" :percentage="percentage" status="success"></el-progress>
    </p>
    <p>
      <el-tag
        v-text="'总共'+dataList.length+'条，已成功'+successNum+'条，失败'+failNum+'条'"
        type="">
      </el-tag>
    </p>
    <div slot="footer" class="dialog-footer">
      <el-button v-if="showPushBtn" type="primary" @click="submitForm">推 送</el-button>
      <el-button @click="cancel" v-text="showPushBtn ? '取 消' : '关 闭'">取 消</el-button>
    </div>
  </el-dialog>
</template>

<script>
export default {
  name: "push-dialog",
  data() {
    return {
      noticeText: '',
      // 是否显示弹出层
      show: false,
      showPushBtn: false,
      percentage: 0,
      dataList: [],
      successNum: 0,
      failNum: 0,
    }
  },
  methods: {
    open(rows) {
      this.successNum = 0;
      this.failNum = 0;
      this.percentage = 0;
      if (rows.length) {
        this.showPushBtn = true;
        this.dataList = rows.map(item => item.id);
        this.noticeText = `当前选择数据${rows.length}条，点击推送进行推送`;
      } else {
        this.noticeText = '未选择任何数据，将推送所有推送失败的数据';
        this.$http.get('/api/v1/push_log/fail_count')
          .then(res => {
            this.dataList = res;
            if (this.dataList.length) {
              this.showPushBtn = true;
            }
          });
      }
      this.show = true;
    },
    async submitForm() {
      const total = this.dataList.length;
      this.successNum = 0;
      this.failNum = 0;
      this.showPushBtn = false;
      for (let i = 1; i <= total; i++) {
        await this.$http.post(`/api/v1/push_log/${this.dataList[i - 1]}`)
          .then(() => {
            this.successNum++;
          })
          .catch(err => {
            this.failNum++;
            console.log(err);
          })
          .finally(() => {
            this.percentage = Number(((i / total) * 100).toFixed(2));
          });
      }
    },
    // 取消按钮
    cancel() {
      this.show = false;
    },
    onCloseDialog() {
      this.$emit('refresh');
    },
  }
}
</script>

<style scoped>

</style>
