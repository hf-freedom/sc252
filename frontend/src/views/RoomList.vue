<template>
  <div class="room-list">
    <el-card>
      <div slot="header">会议室列表</div>
      <el-row :gutter="20">
        <el-col :span="8" v-for="room in rooms" :key="room.id">
          <el-card shadow="hover" class="room-card">
            <div class="room-header">
              <i class="el-icon-office-building"></i>
              <h3>{{ room.name }}</h3>
            </div>
            <el-divider></el-divider>
            <div class="room-info">
              <p><i class="el-icon-user"></i> 容量: <b>{{ room.capacity }}人</b></p>
              <p><i class="el-icon-location-outline"></i> 位置: {{ room.location }}</p>
              <p><i class="el-icon-setting"></i> 设备: {{ room.equipment }}</p>
            </div>
            <div class="room-status">
              <el-tag type="success">可用</el-tag>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script>
import { roomApi } from '../api'

export default {
  name: 'RoomListPage',
  data() {
    return {
      rooms: []
    }
  },
  mounted() {
    this.loadRooms()
  },
  methods: {
    async loadRooms() {
      try {
        const res = await roomApi.getAll()
        this.rooms = res.data.data || []
      } catch (error) {
        this.$message.error('加载会议室列表失败')
      }
    }
  }
}
</script>

<style scoped>
.room-list {
  text-align: left;
}
.room-card {
  margin-bottom: 20px;
}
.room-header {
  display: flex;
  align-items: center;
}
.room-header i {
  font-size: 24px;
  color: #409EFF;
  margin-right: 10px;
}
.room-header h3 {
  margin: 0;
  font-size: 18px;
}
.room-info p {
  margin: 8px 0;
  color: #606266;
}
.room-info i {
  margin-right: 5px;
}
.room-status {
  text-align: right;
  margin-top: 10px;
}
</style>
