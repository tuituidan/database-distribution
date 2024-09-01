<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script>

export default {
  name: "App",
  metaInfo() {
    return {
      title: this.$store.state.settings.title,
    }
  }
};
</script>
