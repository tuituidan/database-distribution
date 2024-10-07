<template>
  <el-dialog :title="title" :visible.sync="show"
             :close-on-click-modal="false"
             width="660px" append-to-body>
    <el-form ref="form" :model="form" :rules="rules" label-width="150px" @submit.native.prevent>
      <el-form-item label="过滤规则类型" prop="ruleType">
        <el-radio-group v-model="form.ruleType">
          <el-radio
            v-for="item in optionsRuleType"
            :key="item.id"
            :label="item.id"
          >{{ item.name }}
            <el-tooltip
              :content="item.id==='01'?'通过表达式直接对数据进行过滤':'从数据库执行sql来过滤结果'">
              <i class="el-icon-question"></i>
            </el-tooltip>
          </el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item prop="ruleExp">
        <span slot="label">
          过滤规则表达式
          <el-tooltip
            content='支持SpEL表达式'>
          <i class="el-icon-question"></i>
          </el-tooltip>
        </span>
        <el-input v-model="form.ruleExp" placeholder="请输入过滤规则表达式" maxlength="400" v-trim/>
      </el-form-item>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button type="primary" @click="submitForm">确 定</el-button>
      <el-button @click="cancel">取 消</el-button>
    </div>
  </el-dialog>
</template>

<script>

export default {
  name: "sys-app-edit",
  data() {
    return {
      // 弹出层标题
      title: '新增过滤规则',
      // 是否显示弹出层
      show: false,
      optionsRuleType: [],
      form: {
        ruleType: '',
        ruleExp: '',
      },
      rules: {
        ruleType: [
          {required: true, message: "过滤规则类型不能为空", trigger: "blur"}
        ],
        ruleExp: [
          {required: true, message: "过滤规则表达式不能为空", trigger: "blur"}
        ],
      }
    }
  },
  mounted() {
    this.loadRuleTyles();
  },
  methods: {
    loadRuleTyles() {
      this.$http.get('/api/v1/data-dict/type/2000000005/dict')
        .then(res => {
          this.optionsRuleType = res;
        })
    },
    open(row) {
      this.form = {...row};
      this.resetForm();
      if (row.id) {
        this.title = '编辑过滤规则';
      } else {
        this.title = '新增过滤规则';
      }
      this.show = true;
    },
    resetForm() {
      this.$nextTick(() => {
        this.$refs.form.clearValidate();
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (valid) {
          console.log({...this.form})
          this.$http.save('/api/v1/sys_app_data_rule', {...this.form})
            .then(() => {
              this.$modal.msgSuccess('保存成功');
              this.show = false;
              this.$emit('refresh');
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
