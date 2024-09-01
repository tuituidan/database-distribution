<template>
  <el-dialog :title="title" :visible.sync="show"
             :close-on-click-modal="false"
             width="600px" append-to-body>
    <el-form ref="form" :model="form" :rules="rules" label-width="80px" @submit.native.prevent>
      <el-form-item label="数据源名" prop="name">
        <el-input v-model="form.name" placeholder="请输入数据源名" maxlength="100" v-trim/>
      </el-form-item>
      <el-form-item label="数据库地址" prop="host">
        <el-input v-model="form.host" placeholder="请输入数据库地址" maxlength="100" v-trim/>
      </el-form-item>
      <el-form-item label="数据库端口" prop="port">
        <el-input v-model="form.port" placeholder="请输入数据库端口" maxlength="100" v-trim/>
      </el-form-item>
      <el-form-item label="数据库名" prop="database">
        <el-input v-model="form.database" placeholder="请输入数据库名" maxlength="30" v-trim/>
      </el-form-item>
      <el-form-item label="数据库用户名" prop="username">
        <el-input v-model="form.username" placeholder="请输入数据库用户名" maxlength="30" v-trim/>
      </el-form-item>
      <el-form-item label="数据库密码" prop="password">
        <el-input v-model="form.password" placeholder="请输入数据库密码" maxlength="100" v-trim/>
      </el-form-item>
      <el-form-item label="服务ID" prop="serverId">
        <el-input v-model="form.serverId" placeholder="请输入服务ID" maxlength="30" v-trim/>
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
      title: '项目新增',
      // 是否显示弹出层
      show: false,
      form: {
        name: '',
        host: '',
        port: '',
        database: '',
        username: '',
        password: '',
        serverId: '',
      },
      rules: {
        projectName: [
          {required: true, message: "项目名称不能为空", trigger: "blur"}
        ],
        projectKey: [
          {required: true, message: "项目标识不能为空", trigger: "blur"}
        ],
      }
    }
  },
  methods: {
    open() {
      this.resetForm();
      this.show = true;
    },
    resetForm() {
      this.form = {
        name: '',
        host: '',
        port: '',
        database: '',
        username: '',
        password: '',
        serverId: '',
      };
      this.$nextTick(() => {
        this.$refs.form.clearValidate();
      })
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          this.$http.save('/api/v1/project', {...this.form})
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
