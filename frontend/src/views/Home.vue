<template>
  <div class="home">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="box-card">
          <div class="card-title">
            <i class="el-icon-s-order" style="color: #409EFF; font-size: 30px;"></i>
            <span>总会议数</span>
          </div>
          <div class="card-value">{{ meetings.length }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="box-card">
          <div class="card-title">
            <i class="el-icon-time" style="color: #67C23A; font-size: 30px;"></i>
            <span>即将开始</span>
          </div>
          <div class="card-value">{{ upcomingMeetings.length }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="box-card">
          <div class="card-title">
            <i class="el-icon-office-building" style="color: #E6A23C; font-size: 30px;"></i>
            <span>会议室</span>
          </div>
          <div class="card-value">{{ rooms.length }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="box-card">
          <div class="card-title">
            <i class="el-icon-user" style="color: #F56C6C; font-size: 30px;"></i>
            <span>员工数</span>
          </div>
          <div class="card-value">{{ employees.length }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row style="margin-top: 20px;" v-if="needRebookMeetings.length > 0">
      <el-col :span="24">
        <el-card>
          <div slot="header" class="clearfix">
            <span style="color: #E6A23C;"><i class="el-icon-warning"></i> 需要重新预约的会议 ({{ needRebookMeetings.length }})</span>
            <el-button style="float: right; padding: 3px 0;" type="warning" size="small" @click="$router.push('/meetings')">
              去处理
            </el-button>
          </div>
          <el-table :data="needRebookMeetings" style="width: 100%" size="small">
            <el-table-column prop="title" label="会议主题"></el-table-column>
            <el-table-column prop="organizerName" label="组织者"></el-table-column>
            <el-table-column prop="startTime" label="原开始时间" width="180">
              <template slot-scope="scope">
                {{ formatTime(scope.row.startTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="expectedAttendees" label="参会人数" width="100"></el-table-column>
            <el-table-column label="操作" width="120">
              <template slot-scope="scope">
                <el-button size="mini" type="primary" @click="$router.push({path: '/meetings/create', query: {rebookId: scope.row.id}})">
                  重新预约
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-row style="margin-top: 20px;">
      <el-col :span="24">
        <el-card>
          <div slot="header" class="clearfix">
            <span>即将开始的会议</span>
          </div>
          <el-table :data="upcomingMeetings" style="width: 100%">
            <el-table-column prop="title" label="会议主题" min-width="150"></el-table-column>
            <el-table-column prop="roomName" label="会议室" width="100"></el-table-column>
            <el-table-column label="参会人数" width="100" align="center">
              <template slot-scope="scope">
                <el-tag :type="getAttendanceTagType(scope.row)" size="small">
                  {{ scope.row.confirmedAttendees || 0 }}/{{ scope.row.expectedAttendees }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="确认状态" width="80" align="center">
              <template slot-scope="scope">
                <el-tag :type="scope.row.attendanceConfirmed ? 'success' : 'warning'" size="small">
                  {{ scope.row.attendanceConfirmed ? '已确认' : '待确认' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="startTime" label="开始时间" width="180">
              <template slot-scope="scope">
                <div>
                  <div>{{ formatTime(scope.row.startTime) }}</div>
                  <div v-if="isMeetingSoon(scope.row)" style="color: #E6A23C; font-size: 12px;">
                    <i class="el-icon-time"></i> 即将开始
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="释放状态" width="100" align="center">
              <template slot-scope="scope">
                <el-tag v-if="shouldShowReleaseWarning(scope.row)" type="danger" size="small">
                  <i class="el-icon-warning"></i> 待释放
                </el-tag>
                <el-tag v-else type="info" size="small">正常</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="预约状态" width="90">
              <template slot-scope="scope">
                <el-tag :type="getStatusType(scope.row.status)" size="small">{{ getStatusText(scope.row.status) }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-row style="margin-top: 20px;">
      <el-col :span="24">
        <el-card>
          <div slot="header" class="clearfix">
            <span>会议室列表</span>
          </div>
          <el-row :gutter="20">
            <el-col :span="8" v-for="room in rooms" :key="room.id">
              <el-card shadow="hover">
                <h3>{{ room.name }}</h3>
                <p>容量: {{ room.capacity }}人</p>
                <p>位置: {{ room.location }}</p>
                <p>设备: {{ room.equipment }}</p>
              </el-card>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { meetingApi, roomApi, employeeApi } from '../api'

export default {
  name: 'HomePage',
  data() {
    return {
      meetings: [],
      upcomingMeetings: [],
      rooms: [],
      employees: []
    }
  },
  computed: {
    needRebookMeetings() {
      return this.meetings.filter(m => m.status === 'NEEDS_REBOOKING')
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    async loadData() {
      try {
        const [meetingsRes, upcomingRes, roomsRes, employeesRes] = await Promise.all([
          meetingApi.getAll(),
          meetingApi.getUpcoming(60),
          roomApi.getAll(),
          employeeApi.getAll()
        ])
        this.meetings = meetingsRes.data.data || []
        this.upcomingMeetings = upcomingRes.data.data || []
        this.rooms = roomsRes.data.data || []
        this.employees = employeesRes.data.data || []
      } catch (error) {
        console.error('加载数据失败', error)
      }
    },
    formatTime(time) {
      return new Date(time).toLocaleString('zh-CN')
    },
    getStatusType(status) {
      const map = {
        'SCHEDULED': 'primary',
        'CONFIRMED': 'success',
        'CANCELLED': 'info',
        'PREEMPTED': 'warning',
        'NEEDS_REBOOKING': 'danger'
      }
      return map[status] || 'info'
    },
    getAttendanceTagType(meeting) {
      const confirmed = meeting.confirmedAttendees || 0
      const expected = meeting.expectedAttendees
      const ratio = confirmed / expected
      if (ratio >= 1) return 'success'
      if (ratio >= 0.5) return ''
      return 'danger'
    },
    isMeetingSoon(meeting) {
      if (meeting.status === 'CANCELLED' || meeting.status === 'COMPLETED') {
        return false
      }
      const now = new Date()
      const startTime = new Date(meeting.startTime)
      const diffMinutes = (startTime - now) / (1000 * 60)
      return diffMinutes > 0 && diffMinutes <= 30
    },
    shouldShowReleaseWarning(meeting) {
      if (meeting.status !== 'SCHEDULED' && meeting.status !== 'CONFIRMED') {
        return false
      }
      const confirmed = meeting.confirmedAttendees || 0
      const expected = meeting.expectedAttendees
      const hasEnoughAttendance = confirmed >= Math.floor(expected / 2)
      const isConfirmed = meeting.attendanceConfirmed
      
      return this.isMeetingSoon(meeting) && (!isConfirmed || !hasEnoughAttendance)
    },
    getStatusText(status) {
      const map = {
        'SCHEDULED': '已预约',
        'CONFIRMED': '已确认',
        'CANCELLED': '已取消',
        'PREEMPTED': '被抢占',
        'NEEDS_REBOOKING': '需重新预约',
        'COMPLETED': '已完成'
      }
      return map[status] || status
    }
  }
}
</script>

<style scoped>
.home {
  text-align: left;
}
.box-card {
  text-align: center;
}
.card-title {
  margin-bottom: 10px;
}
.card-title span {
  margin-left: 10px;
  font-size: 16px;
}
.card-value {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
}
</style>
