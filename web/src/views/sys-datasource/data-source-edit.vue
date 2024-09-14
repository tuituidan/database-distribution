<template>
  <el-dialog :title="title" :visible.sync="show"
             :close-on-click-modal="false"
             width="480px" append-to-body>
    <el-form ref="form" :model="form" :rules="rules" label-width="110px" @submit.native.prevent>
      <el-form-item label="数据源名" prop="name">
        <el-input v-model="form.name" placeholder="请输入数据源名" maxlength="100" v-trim/>
      </el-form-item>
      <el-form-item label="数据库地址" prop="host">
        <el-input v-model="form.host" placeholder="请输入数据库地址" maxlength="100" v-trim/>
      </el-form-item>
      <el-form-item label="数据库端口" prop="port">
        <el-input-number v-model="form.port"
                         :min="1"
                         step-strictly
                         :max="65535"
                         placeholder="请输入数据库端口" maxlength="100" v-trim/>
      </el-form-item>
      <el-form-item label="数据库用户名" prop="username">
        <el-input v-model="form.username" placeholder="请输入数据库用户名" maxlength="100" v-trim/>
      </el-form-item>
      <el-form-item label="数据库密码" prop="password">
        <el-input v-model="form.password" show-password placeholder="请输入数据库密码" maxlength="100" v-trim/>
      </el-form-item>
      <el-form-item prop="serverId">
        <span slot="label">
          服务ID
          <el-tooltip content='数据从节点ID，与其他从节点不重复即可'>
          <i class="el-icon-question"></i>
          </el-tooltip>
        </span>
        <el-input-number v-model="form.serverId"
                  :min="1"
                  step-strictly
                  :max="65535"
                  placeholder="请输入服务ID" maxlength="100" v-trim/>
      </el-form-item>
      <el-form-item label="时区" prop="timeZone">
        <span slot="label">
          时区
          <el-tooltip content='如果时间字段读取有误，通过设置时区转换'>
          <i class="el-icon-question"></i>
          </el-tooltip>
        </span>
        <el-input v-model="form.timeZone" placeholder="请输入时区" maxlength="100" v-trim/>
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
  name: "project-edit",
  data() {
    return {
      // 弹出层标题
      title: '新增数据源',
      // 是否显示弹出层
      show: false,
      form: {
        name: '',
        host: '',
        port: '',
        username: '',
        password: '',
        serverId: '',
      },
      rules: {
        name: [
          {required: true, message: "数据源名不能为空", trigger: "blur"}
        ],
        host: [
          {required: true, message: "数据库地址不能为空", trigger: "blur"}
        ],
        port: [
          {required: true, message: "数据库端口不能为空", trigger: "blur"}
        ],
        username: [
          {required: true, message: "数据库用户名不能为空", trigger: "blur"}
        ],
        password: [
          {required: true, message: "数据库密码不能为空", trigger: "blur"}
        ],
        serverId: [
          {required: true, message: "服务ID不能为空", trigger: "blur"}
        ],
      },
    }
  },
  methods: {
    open(row) {
      this.resetForm();
      if(row){
        this.form = {...row};
        this.title = '编辑数据源';
      }else{
        this.title = '新增数据源';
      }
      this.show = true;
    },
    resetForm() {
      this.form = {
        name: '',
        host: '',
        port: '',
        username: '',
        password: '',
        serverId: '',
      };
      this.$nextTick(() => {
        this.$refs.form.clearValidate();
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (valid) {
          this.$http.save('/api/v1/datasource', {...this.form})
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
