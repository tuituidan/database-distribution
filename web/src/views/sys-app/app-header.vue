<template>
  <div>
    <el-row v-for="(item, index) in value" :key="item.type+index" :gutter="10"
            :class="index>0?'header-row':'header-row-first'">
      <el-col :span="6">
        <el-select v-model="item.type" placeholder="请选择类型" @change="typeChange(item)">
          <el-option
            v-for="it in types"
            :key="it.code"
            :label="it.name"
            :disabled="selectOptionsDisabled(it)"
            :value="it.code">
          </el-option>
        </el-select>
      </el-col>
      <el-col :span="8">
        <el-input v-model="item.key" :disabled="item.type==='basic'||item.type==='bearer'"
                  placeholder="请输入键名" maxlength="100" v-trim/>
      </el-col>
      <el-col :span="8">
        <el-input v-model="item.value" :disabled="item.type==='basic'||item.type==='bearer'"
                  placeholder="请输入键值" maxlength="100" v-trim/>
      </el-col>
      <el-col :span="1">
        <i v-if="index === value.length - 1" @click="addHandler"
           class="el-icon-circle-plus-outline header-icon header-icon-add"></i>
        <i v-else @click="removeHandler(index)" class="el-icon-remove-outline header-icon header-icon-remove"></i>
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  name: "app-header",
  props: {
    formData: {
      type: Object,
    },
    value: {
      type: Array,
    },
  },
  data() {
    return {
      types: [
        {
          code: 'key-value',
          name: '键值对',
        },
        {
          code: 'basic',
          name: 'Basic认证',
        },
        {
          code: 'bearer',
          name: 'Bearer认证',
        },
      ]
    }
  },
  methods: {
    selectOptionsDisabled(it) {
      return ['basic', 'bearer'].includes(it.code)
        && this.value.findIndex(h => ['basic', 'bearer'].includes(h.type)) >= 0;
    },
    typeChange(item) {
      if (item.type === 'basic' || item.type === 'bearer') {
        item.key = this.formData.appKey;
        item.value = this.formData.appSecret;
      } else {
        item.key = '';
        item.value = '';
      }
    },
    addHandler() {
      this.value.push({type: '', key: '', value: ''})
    },
    removeHandler(index) {
      this.value.splice(index, 1)
    },
  },
}
</script>

<style scoped>
.header-row {
  margin-left: 0 !important;
  margin-top: 8px;
}

.header-row-first {
  margin-left: 0 !important;
}

.header-icon {
  vertical-align: middle;
  font-size: 20px;
  cursor: pointer;
}

.header-icon-add {
  color: #67c23a;
}

.header-icon-remove {
  color: #f56c6c;
}
</style>
