<template>
  <el-dialog :title="title" :visible.sync="show"
             :close-on-click-modal="false"
             width="660px" append-to-body>
    <el-form ref="form" :model="form" :rules="rules" label-width="140px" @submit.native.prevent>
      <el-form-item label="应用名称" prop="appName">
        <el-input v-model="form.appName" placeholder="请输入应用名称" maxlength="100" v-trim/>
      </el-form-item>
      <el-form-item label="应用标识" prop="appKey">
        <el-input v-model="form.appKey" :disabled="Boolean(this.form.id)" placeholder="请输入应用标识" maxlength="100" v-trim>
          <el-button slot="append" v-if="!Boolean(this.form.id)" @click="generateKey('appKey')">随机生成</el-button>
        </el-input>
      </el-form-item>
      <el-form-item label="应用秘钥" prop="appSecret">
        <el-input v-model="form.appSecret" disabled placeholder="请输入应用秘钥" maxlength="100" v-trim>
          <el-button slot="append" v-if="!Boolean(this.form.id)" @click="generateKey('appSecret')">随机生成</el-button>
        </el-input>
      </el-form-item>
      <el-form-item prop="url">
        <span slot="label">
          推送地址
          <el-tooltip content='推送数据地址需为POST接口'>
          <i class="el-icon-question"></i>
          </el-tooltip>
        </span>
        <el-input v-model="form.url" placeholder="请输入推送地址" maxlength="400" v-trim/>
      </el-form-item>
      <el-form-item prop="resultExp">
        <span slot="label">
          结果解析表达式
          <el-tooltip content='将返回的json数据通过此SpringEL表达式来解析，用于判断成功还是失败，如返回成功结果是{code:1,msg:"xxx"}，填写【#code==1】，当code为其他值时结果判定为失败，方便后续重推失败数据，如果不填写则以http请求状态来判断成功失败'>
          <i class="el-icon-question"></i>
          </el-tooltip>
        </span>
        <el-input v-model="form.resultExp" placeholder="请输入结果解析表达式" maxlength="400" v-trim/>
      </el-form-item>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button type="primary" @click="submitForm">确 定</el-button>
      <el-button @click="cancel">取 消</el-button>
    </div>
  </el-dialog>
</template>

<script>
import {uuid} from "@/utils";

export default {
  name: "sys-app-edit",
  data() {
    return {
      // 弹出层标题
      title: '新增应用',
      // 是否显示弹出层
      show: false,
      form: {
        appKey: '',
        appName: '',
        appSecret: '',
        url: '',
        resultExp: '',
      },
      rules: {
        appKey: [
          {required: true, message: "应用标识不能为空", trigger: "blur"}
        ],
        appName: [
          {required: true, message: "应用名称不能为空", trigger: "blur"}
        ],
        appSecret: [
          {required: true, message: "应用秘钥不能为空", trigger: "blur"}
        ],
        url: [
          {required: true, message: "推送地址不能为空", trigger: "blur"}
        ],
      }
    }
  },
  methods: {
    open(row) {
      this.resetForm();
      if (row) {
        this.form = {...row};
        this.title = '编辑应用';
      } else {
        this.form.appKey = uuid('');
        this.form.appSecret = uuid('');
        this.title = '新增应用';
      }
      this.show = true;
    },
    generateKey(key){
      this.form[key] = uuid('')
    },
    resetForm() {
      this.form = {
        appKey: '',
        appName: '',
        appSecret: '',
        url: '',
        resultExp: '',
      };
      this.$nextTick(() => {
        this.$refs.form.clearValidate();
      })
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          this.$http.save('/api/v1/sys_app', {...this.form})
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
