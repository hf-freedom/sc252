<template>
  <div id="app">
    <el-container style="height: 100vh; border: 1px solid #eee;">
      <el-aside width="200px" style="background-color: rgb(238, 241, 246);">
        <el-menu router default-active="$route.path">
          <el-menu-item index="/">
            <i class="el-icon-s-home"></i>
            <span slot="title">首页</span>
          </el-menu-item>
          <el-menu-item index="/meetings">
            <i class="el-icon-s-order"></i>
            <span slot="title">会议列表</span>
          </el-menu-item>
          <el-menu-item index="/meetings/create">
            <i class="el-icon-plus"></i>
            <span slot="title">创建会议</span>
          </el-menu-item>
          <el-menu-item index="/rooms">
            <i class="el-icon-office-building"></i>
            <span slot="title">会议室</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-container>
        <el-header style="text-align: right; font-size: 12px;">
          <div style="text-align: left; font-size: 20px; font-weight: bold; color: #409EFF;">
            会议室预约系统
          </div>
        </el-header>
        <el-main>
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
export default {
  name: 'App'
}
</script>

<style>
#app {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
}
.el-header {
  background-color: #fff;
  border-bottom: 1px solid #eee;
}
</style>
