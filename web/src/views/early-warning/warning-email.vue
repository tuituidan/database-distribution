<template>
  <div>
    <el-card shadow="never">
      <div slot="header" class="card-header">
        <span>预警邮箱配置</span>
        <el-button
          type="primary"
          plain
          icon="el-icon-document"
          size="mini"
          @click="saveHandler"
        >保存
        </el-button>
      </div>
      <el-form ref="form" :model="form" :rules="rules" label-width="120px" @submit.native.prevent>
        <el-form-item label="邮箱服务地址" prop="host">
          <el-input v-model="form.host" placeholder="请输入邮箱服务地址" maxlength="100" v-trim/>
        </el-form-item>
        <el-form-item label="邮箱服务协议" prop="host">
          <el-input v-model="form.protocol" placeholder="请输入邮箱服务地址" maxlength="100" v-trim/>
        </el-form-item>
        <el-form-item label="邮箱服务端口" prop="port">
          <el-input-number v-model="form.port"
                           :min="1"
                           step-strictly
                           :max="65535"
                           placeholder="请输入邮箱服务端口" maxlength="100" v-trim/>
        </el-form-item>
        <el-form-item label="邮箱账号" prop="username">
          <el-input v-model="form.username" placeholder="请输入邮箱账号" maxlength="100" v-trim/>
        </el-form-item>
        <el-form-item label="邮箱密码" prop="password">
          <el-input v-model="form.password" show-password placeholder="请输入邮箱密码" maxlength="100" v-trim/>
        </el-form-item>
        <el-form-item label="接收人" prop="receivers">
          <dynamic-tag add-text="请输入邮箱" v-model="form.receivers"></dynamic-tag>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
export default {
  name: "data-source-list",
  components: {
    'dynamic-tag': () => import('@/components/dynamic-tag'),
  },
  data() {
    return {
      // 遮罩层
      loading: false,
      form: {
        host: '',
        protocol: '',
        port: '',
        username: '',
        password: '',
        receivers: [],
      },
      rules: {
        host: [
          {required: true, message: "邮箱服务地址不能为空", trigger: "blur"}
        ],
        port: [
          {required: true, message: "邮箱服务端口不能为空", trigger: "blur"}
        ],
        username: [
          {required: true, message: "邮箱账号不能为空", trigger: "blur"}
        ],
        password: [
          {required: true, message: "邮箱密码不能为空", trigger: "blur"}
        ],
      },
    };
  },
  mounted() {
    this.loadConfig();
  },
  methods: {
    loadConfig() {
      this.loading = true;
      this.$http.get('/api/v1/email')
        .then(res => {
          this.form = res;
        })
        .finally(() => {
          this.loading = false;
        });
    },
    saveHandler() {
      this.loading = true;
      this.$http.post(`/api/v1/email`, this.form)
        .then(() => {
          this.$modal.msgSuccess('保存成功');
          this.loadConfig();
        })
        .finally(() => {
          this.loading = false;
        });
    },
  }
}
</script>

<style scoped lang="scss">
::v-deep .el-card__header {
  padding: 11px 15px;
}

::v-deep .el-card__body {
  padding: 10px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  align-items: center;
}
</style>
