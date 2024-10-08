<template>
  <el-dialog :title="title" :visible.sync="show"
             :close-on-click-modal="false"
             width="600px" append-to-body>
    <el-form ref="form" :model="form" :rules="rules" label-width="120px" @submit.native.prevent>
      <el-form-item label="数据库" prop="databaseName">
        <el-select v-model="form.databaseName" placeholder="请选择数据库"
                   @change="loadTableOptions"
                   clearable
                   filterable>
          <el-option
            v-for="item in databaseOptions"
            :key="item"
            :label="item"
            :value="item">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="表名" prop="tableName">
        <el-select v-model="form.tableName" placeholder="请选择表名"
                   @change="loadColumnOptions"
                   clearable
                   filterable>
          <el-option
            v-for="item in tableOptions"
            :key="item"
            :label="item"
            :value="item">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="表说明" prop="tableComment">
        <el-input v-model="form.tableComment" placeholder="请输入表说明" maxlength="100" v-trim/>
      </el-form-item>
      <el-form-item label="主键名" prop="primaryKey">
        <el-select v-model="form.primaryKey" placeholder="请选择主键名"
                   clearable
                   multiple
                   filterable>
          <el-option
            v-for="item in columnOptions"
            :key="item"
            :label="item"
            :value="item">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="增量字段" prop="incrementKey">
        <el-select v-model="form.incrementKey" placeholder="请选择增量字段"
                   clearable
                   filterable>
          <el-option
            v-for="item in columnOptions"
            :key="item"
            :label="item"
            :value="item">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="增量字段类型" prop="incrementType">
        <el-select v-model="form.incrementType" placeholder="请选择增量字段类型">
          <el-option
            v-for="item in optionsIncrementType"
            :key="item.code"
            :label="item.name"
            :value="item.code">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="日志记录字段" prop="recordColumn">
        <span slot="label">
          日志记录字段
          <el-tooltip content='记录在数据日志中的字段，不配置记录所有字段，建议配置少量关键字段更易于查看日志，也减少存储'>
          <i class="el-icon-question"></i>
          </el-tooltip>
        </span>
        <el-select v-model="form.recordColumn" placeholder="请选择要记录到日志的字段"
                   clearable
                   multiple
                   filterable>
          <el-option
            v-for="item in columnOptions"
            :key="item"
            :label="item"
            :value="item">
          </el-option>
        </el-select>
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
      title: '新增数据库配置',
      // 是否显示弹出层
      show: false,
      optionsIncrementType: [
        {
          code: 'date',
          name: '时间'
        },
        {
          code: 'number',
          name: '数字'
        },
      ],
      datasourceId: '',
      form: {
        databaseName: '',
        tableName: '',
        tableComment: '',
        primaryKey: [],
        recordColumn: [],
        incrementKey: '',
        incrementType: '',
      },
      rules: {
        databaseName: [
          {required: true, message: "数据库名不能为空", trigger: "blur"}
        ],
        tableName: [
          {required: true, message: "表名不能为空", trigger: "blur"}
        ],
        primaryKey: [
          {required: true, message: "主键名不能为空", trigger: "blur"}
        ],
      },
      databaseOptions: [],
      tableOptions: [],
      columnOptions: [],
    }
  },
  methods: {
    open(datasourceId, row) {
      this.datasourceId = datasourceId;
      this.resetForm();
      this.loadDatabaseOptions();
      if (row) {
        this.form = {...row};
        this.loadTableOptions(this.form.databaseName);
        this.loadColumnOptions(this.form.tableName);
        this.title = '编辑数据库配置';
      } else {
        this.title = '新增数据库配置';
      }
      this.show = true;
    },
    loadDatabaseOptions() {
      this.$http.get(`/api/v1/datasource/${this.datasourceId}/database`)
        .then(res => {
          this.databaseOptions = res;
        });
    },
    loadTableOptions(database) {
      this.$http.get(`/api/v1/datasource/${this.datasourceId}/database/${database}`)
        .then(res => {
          this.tableOptions = res;
        });
    },
    loadColumnOptions(tableName) {
      this.$http.get(`/api/v1/datasource/${this.datasourceId}/database/${this.form.databaseName}/table/${tableName}`)
        .then(res => {
          this.columnOptions = res;
        });
    },
    resetForm() {
      this.form = {
        databaseName: '',
        tableName: '',
        tableComment: '',
        primaryKey: [],
        recordColumn: [],
        incrementKey: '',
        incrementType: '',
      };
      this.databaseOptions = [];
      this.tableOptions = [];
      this.columnOptions = [];
      this.$nextTick(() => {
        this.$refs.form.clearValidate();
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (valid) {
          this.$http.save('/api/v1/database_config', {...this.form, datasourceId: this.datasourceId})
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
