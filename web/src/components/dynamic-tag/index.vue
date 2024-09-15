<template>
  <div>
    <el-tag
      :key="tag"
      v-for="tag in value"
      closable
      :disable-transitions="false"
      @close="handleClose(tag)">
      {{ tag }}
    </el-tag>
    <el-input
      class="input-new-tag"
      v-if="inputVisible"
      v-model="inputValue"
      ref="saveTagInput"
      @keyup.enter.native="handleInputConfirm"
      @blur="handleInputConfirm"
    >
    </el-input>
    <el-button v-else class="button-new-tag" size="small" @click="showInput">+ {{ addText }}</el-button>
  </div>
</template>

<script>
export default {
  name: "dynamic-tag-index",
  props: {
    addText: {
      type: String,
      required: true
    },
    value: {
      type: Array,
      default() {
        return [];
      }
    },
  },
  data() {
    return {
      inputVisible: false,
      inputValue: ''
    };
  },
  methods: {
    handleClose(tag) {
      this.value.splice(this.value.indexOf(tag), 1);
    },

    showInput() {
      this.inputVisible = true;
      this.$nextTick(_ => {
        this.$refs.saveTagInput.$refs.input.focus();
      });
    },

    handleInputConfirm() {
      let inputValue = this.inputValue;
      if (inputValue) {
        this.value.push(inputValue);
      }
      this.inputVisible = false;
      this.inputValue = '';
      this.$emit('input', this.value)
    }
  }
}
</script>

<style scoped>
.el-tag + .el-tag {
  margin-left: 10px;
}

.button-new-tag {
  margin-left: 10px;
  height: 32px;
  line-height: 30px;
  padding-top: 0;
  padding-bottom: 0;
}

.input-new-tag {
  width: 160px;
  margin-left: 10px;
  vertical-align: bottom;
}
</style>
