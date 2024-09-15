<template>
  <div class="has-logo" :style="{ backgroundColor: variables.menuBackground }">
    <logo :collapse="isCollapse"/>
    <el-scrollbar :class="settings.sideTheme" wrap-class="scrollbar-wrapper">
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :background-color="variables.menuBackground"
        :text-color="variables.menuColor"
        :unique-opened="true"
        :active-text-color="settings.theme"
        :collapse-transition="false"
        mode="vertical"
      >
        <sidebar-item
          v-for="(route, index) in sidebarRouters"
          :key="route.path  + index"
          :item="route"
          :base-path="route.path"
        />
      </el-menu>
    </el-scrollbar>
  </div>
</template>

<script>
import {mapGetters, mapState} from "vuex";
import Logo from "./Logo";
import SidebarItem from "./SidebarItem";
import variables from "@/assets/styles/variables.scss";

export default {
  components: {SidebarItem, Logo},
  computed: {
    ...mapState(["settings"]),
    ...mapGetters(["sidebar"]),
    activeMenu() {
      const route = this.$route;
      const {meta, path} = route;
      // if set path, the sidebar will highlight the path you set
      if (meta.activeMenu) {
        return meta.activeMenu;
      }
      return path;
    },
    variables() {
      return variables;
    },
    isCollapse() {
      return !this.sidebar.opened;
    }
  },
  data() {
    return {
      sidebarRouters: [
        {
          path: '',
          name: 'index',
          meta: {title: '首页', icon: 'dashboard'}
        },
        {
          path: '/log',
          name: 'log',
          meta: {title: '日志管理', icon: 'dashboard'},
          children: [
            {
              path: 'data',
              name: 'log-data',
              meta: {title: '数据日志', icon: 'dashboard'}
            },
            {
              path: 'push',
              name: 'log-push',
              meta: {title: '推送日志', icon: 'dashboard'}
            },
          ],
        },
        {
          path: '/system',
          name: 'system',
          meta: {title: '基础设置', icon: 'dashboard'},
          children: [
            {
              path: 'datasource',
              name: 'system-datasource',
              meta: {title: '数据源配置', icon: 'dashboard'}
            },
            {
              path: 'app',
              name: 'system-app',
              meta: {title: '应用配置', icon: 'dashboard'}
            },
            {
              path: 'early-warning',
              name: 'system-early-warning',
              meta: {title: '预警配置', icon: 'dashboard'}
            },
          ],
        },
      ]
    }
  },
  mounted() {
  },
  methods: {
  }
};
</script>
