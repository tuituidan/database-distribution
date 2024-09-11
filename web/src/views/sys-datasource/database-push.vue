<template>
  <el-dialog title="重新推送起点设置" :visible.sync="show"
             :close-on-click-modal="false"
             width="480px" append-to-body>
    <el-form ref="form" :model="form" :rules="rules" label-width="110px" @submit.native.prevent>
      <el-form-item v-if="incrementType ==='date'" label="起始时间" prop="incrementValue">
        <el-date-picker
          v-model="form.incrementValue"
          type="datetime"
          value-format="yyyy-MM-dd HH:mm:ss"
          placeholder="请选择起始时间">
        </el-date-picker>
      </el-form-item>
      <el-form-item v-else label="起始数值" prop="incrementValue">
        <el-input-number v-model="form.incrementValue" placeholder="请输入起始数值" v-trim/>
      </el-form-item>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button type="primary" @click="submitForm">推 送</el-button>
      <el-button @click="cancel">取 消</el-button>
    </div>
  </el-dialog>
</template>

<script>
export default {
  name: "database-push",
  data() {
    return {
      // 是否显示弹出层
      show: false,
      incrementType: 'date',
      form: {
        incrementValue: '',
      },
      rules: {
        incrementValue: [
          {required: true, message: "不能为空", trigger: "blur"}
        ],
      },
    }
  },
  methods: {
    open(source, rows) {
      this.incrementType = rows[0].incrementType;
      this.form = {
        ids: rows.map(item => item.id),
        datasourceId: source.id,
        incrementValue: source.lastStopTime,
      };
      this.$nextTick(() => {
        this.$refs.form.clearValidate();
      })
      this.show = true;
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (valid) {
          this.$http.post('/api/v1/database_config/handler', {...this.form})
            .then(() => {
              this.$modal.msgSuccess('推送成功');
              this.show = false;
            })
        }
      });
    },
    // 取消按钮
    cancel() {
      this.show = false;
    },
  }
}
</script>

<style scoped>

</style>
