<template>
  <div class="meeting-list">
    <el-card>
      <div slot="header" class="clearfix">
        <span>会议列表</span>
        <div style="float: right;">
          <el-button style="padding: 3px 0; margin-right: 10px;" type="warning" size="small" @click="checkAndRelease">
            <i class="el-icon-check"></i> 检查并释放会议室
          </el-button>
          <el-button style="padding: 3px 0;" type="primary" @click="$router.push('/meetings/create')">
            <i class="el-icon-plus"></i> 创建会议
          </el-button>
        </div>
      </div>
      <el-table :data="meetings" style="width: 100%">
        <el-table-column label="会议主题" min-width="180">
          <template slot-scope="scope">
            <div>
              <span>{{ scope.row.title }}</span>
              <el-tag v-if="scope.row.recurrenceType && scope.row.recurrenceType !== 'NONE'" 
                      size="mini" 
                      type="info" 
                      style="margin-left: 8px;">
                <i class="el-icon-refresh"></i> {{ getRecurrenceText(scope.row.recurrenceType) }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="organizerName" label="组织者" width="100"></el-table-column>
        <el-table-column prop="roomName" label="会议室" width="100"></el-table-column>
        <el-table-column label="参会人数" width="130" align="center">
          <template slot-scope="scope">
            <el-tag :type="getAttendanceTagType(scope.row)" size="small">
              {{ scope.row.confirmedAttendees || 0 }} / {{ scope.row.expectedAttendees }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="参会确认" width="100" align="center">
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
                <i class="el-icon-time"></i> {{ getTimeUntilStart(scope.row) }}
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="endTime" label="结束时间" width="180">
          <template slot-scope="scope">
            {{ formatTime(scope.row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="80">
          <template slot-scope="scope">
            <el-tag :type="getPriorityType(scope.row.priority)" size="small">{{ getPriorityText(scope.row.priority) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="释放状态" width="120" align="center">
          <template slot-scope="scope">
            <div v-if="shouldShowReleaseWarning(scope.row)">
              <el-tag type="danger" size="small">
                <i class="el-icon-warning"></i> 人数不足
              </el-tag>
              <div style="font-size: 11px; color: #F56C6C; margin-top: 4px;">
                即将自动释放
              </div>
            </div>
            <el-tag v-else type="info" size="small">正常</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="预约状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)" size="small">{{ getStatusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="400">
          <template slot-scope="scope">
            <template v-if="scope.row.status === 'NEEDS_REBOOKING'">
              <el-button size="mini" type="warning" icon="el-icon-refresh" @click="recommendAndRebook(scope.row)">
                重新推荐并预约
              </el-button>
            </template>
            <template v-else-if="scope.row.status !== 'CANCELLED' && scope.row.status !== 'COMPLETED'">
              <el-button size="mini" @click="editMeeting(scope.row)" type="info">
                <i class="el-icon-edit"></i> 编辑
              </el-button>
              <el-button size="mini" @click="recommendRooms(scope.row)" type="success">推荐会议室</el-button>
              <el-button size="mini" @click="confirmAttendance(scope.row)" type="primary">确认人数</el-button>
              <el-button size="mini" @click="showCancelDialog(scope.row)" type="danger">取消</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog :title="isRebookMode ? '重新推荐会议室并预约' : '推荐会议室'" :visible.sync="recommendDialogVisible" width="60%">
      <div v-if="isRebookMode && rebookMeeting" style="margin-bottom: 20px;">
        <el-alert title="会议被抢占，需要重新预约" type="warning" show-icon>
          <template slot="title">
            <span>「{{ rebookMeeting.title }}」被抢占，请选择新会议室重新预约</span>
          </template>
        </el-alert>
      </div>
      <el-table :data="recommendedRooms" style="width: 100%" @row-click="selectRoom" highlight-current-row>
        <el-table-column width="50">
          <template slot-scope="scope">
            <el-radio v-model="selectedRoomId" :label="scope.row.id"></el-radio>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="会议室名称"></el-table-column>
        <el-table-column prop="capacity" label="容量">
          <template slot-scope="scope">{{ scope.row.capacity }}人</template>
        </el-table-column>
        <el-table-column prop="location" label="位置"></el-table-column>
        <el-table-column prop="equipment" label="设备"></el-table-column>
      </el-table>
      <el-empty v-if="recommendedRooms.length === 0" description="暂无可用会议室，请调整会议时间或参会人数"></el-empty>
      <span slot="footer" class="dialog-footer">
        <el-button @click="recommendDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmRebook" v-if="isRebookMode && recommendedRooms.length > 0" :disabled="!selectedRoomId">
          确认重新预约
        </el-button>
      </span>
    </el-dialog>

    <el-dialog title="取消会议" :visible.sync="cancelDialogVisible" width="450px">
      <div v-if="currentMeeting">
        <el-alert v-if="currentMeeting.recurrenceType && currentMeeting.recurrenceType !== 'NONE'"
                  title="这是周期会议"
                  type="warning"
                  show-icon
                  style="margin-bottom: 20px;">
          <span>「{{ currentMeeting.title }}」是周期会议，请选择取消范围</span>
        </el-alert>
        <div style="padding: 20px 0;">
          <p>确定要取消会议「{{ currentMeeting.title }}」吗？</p>
        </div>
        <el-radio-group v-if="currentMeeting.recurrenceType && currentMeeting.recurrenceType !== 'NONE'"
                        v-model="cancelScope"
                        style="width: 100%;">
          <el-radio label="single" border style="width: 100%; margin-bottom: 10px; padding: 10px;">
            仅取消本次
          </el-radio>
          <el-radio label="whole" border style="width: 100%; padding: 10px;">
            取消整组所有会议
          </el-radio>
        </el-radio-group>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="cancelDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="cancelMeeting(cancelScope === 'whole')">确认取消</el-button>
      </span>
    </el-dialog>

    <el-dialog title="确认参会人数" :visible.sync="attendanceDialogVisible" width="450px">
      <el-alert v-if="currentMeeting && isMeetingSoon(currentMeeting)" 
                title="会议即将开始" 
                :type="shouldShowReleaseWarning(currentMeeting) ? 'error' : 'warning'"
                :description="shouldShowReleaseWarning(currentMeeting) ? '当前确认人数不足预期的50%，会议室将被自动释放！' : '请尽快确认参会人数'"
                show-icon
                style="margin-bottom: 20px;">
      </el-alert>
      
      <div v-if="currentMeeting" style="margin-bottom: 20px; padding: 15px; background: #f5f7fa; border-radius: 4px;">
        <div style="margin-bottom: 8px;"><strong>会议：</strong>{{ currentMeeting.title }}</div>
        <div style="margin-bottom: 8px;"><strong>时间：</strong>{{ formatTime(currentMeeting.startTime) }} - {{ formatTime(currentMeeting.endTime) }}</div>
        <div style="margin-bottom: 8px;">
          <strong>预期参会人数：</strong>
          <el-tag type="primary" size="small">{{ currentMeeting.expectedAttendees }}人</el-tag>
        </div>
        <div>
          <strong>最低确认人数(50%)：</strong>
          <el-tag type="danger" size="small">{{ Math.floor(currentMeeting.expectedAttendees / 2) }}人</el-tag>
        </div>
      </div>
      
      <el-form label-width="100px">
        <el-form-item label="确认参会人数">
          <el-input-number v-model="confirmedCount" :min="0" :max="currentMeeting?.expectedAttendees || 999" size="large"></el-input-number>
        </el-form-item>
        <el-form-item label="人数验证">
          <span v-if="isConfirmedCountEnough()" style="color: #67C23A;">
            <i class="el-icon-success"></i> 人数满足要求
          </span>
          <span v-else style="color: #F56C6C;">
            <i class="el-icon-error"></i> 人数不足预期的50%，会议室将被释放！
          </span>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="attendanceDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAttendance">确认提交</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { meetingApi } from '../api'

export default {
  name: 'MeetingListPage',
  data() {
    return {
      meetings: [],
      recommendDialogVisible: false,
      attendanceDialogVisible: false,
      cancelDialogVisible: false,
      recommendedRooms: [],
      currentMeeting: null,
      confirmedCount: 0,
      isRebookMode: false,
      rebookMeeting: null,
      selectedRoomId: null,
      refreshTimer: null,
      cancelScope: 'single'
    }
  },
  mounted() {
    this.loadMeetings()
    this.startAutoRefresh()
  },
  beforeDestroy() {
    this.stopAutoRefresh()
  },
  methods: {
    startAutoRefresh() {
      this.refreshTimer = setInterval(() => {
        this.loadMeetings()
      }, 30000)
    },
    stopAutoRefresh() {
      if (this.refreshTimer) {
        clearInterval(this.refreshTimer)
        this.refreshTimer = null
      }
    },
    async loadMeetings() {
      try {
        const res = await meetingApi.getAll()
        this.meetings = res.data.data || []
      } catch (error) {
        this.$message.error('加载会议列表失败')
      }
    },
    formatTime(time) {
      return new Date(time).toLocaleString('zh-CN')
    },
    getRecurrenceText(type) {
      const map = {
        'DAILY': '每天',
        'WEEKLY': '每周',
        'MONTHLY': '每月'
      }
      return map[type] || type
    },
    getPriorityType(priority) {
      const map = {
        'LOW': 'info',
        'NORMAL': '',
        'HIGH': 'warning',
        'URGENT': 'danger'
      }
      return map[priority] || ''
    },
    getPriorityText(priority) {
      const map = {
        'LOW': '低',
        'NORMAL': '普通',
        'HIGH': '高',
        'URGENT': '紧急'
      }
      return map[priority] || priority
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
    getTimeUntilStart(meeting) {
      const now = new Date()
      const startTime = new Date(meeting.startTime)
      const diffMinutes = Math.floor((startTime - now) / (1000 * 60))
      if (diffMinutes < 60) {
        return `还有 ${diffMinutes} 分钟开始`
      }
      const hours = Math.floor(diffMinutes / 60)
      const mins = diffMinutes % 60
      return `还有 ${hours}小时${mins}分钟开始`
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
    isConfirmedCountEnough() {
      if (!this.currentMeeting) return false
      const expected = this.currentMeeting.expectedAttendees
      return this.confirmedCount >= Math.floor(expected / 2)
    },
    async recommendRooms(meeting) {
      try {
        const res = await meetingApi.recommendRooms(meeting.id)
        this.recommendedRooms = res.data.data || []
        this.isRebookMode = false
        this.rebookMeeting = null
        this.selectedRoomId = null
        this.recommendDialogVisible = true
        if (this.recommendedRooms.length === 0) {
          this.$message.info('暂无可用会议室')
        }
      } catch (error) {
        this.$message.error('获取推荐会议室失败')
      }
    },
    async recommendAndRebook(meeting) {
      try {
        const res = await meetingApi.recommendRooms(meeting.id)
        this.recommendedRooms = res.data.data || []
        this.isRebookMode = true
        this.rebookMeeting = meeting
        this.selectedRoomId = null
        this.recommendDialogVisible = true
        if (this.recommendedRooms.length === 0) {
          this.$message.warning('暂无可用会议室，请调整会议时间或人数')
        }
      } catch (error) {
        this.$message.error('获取推荐会议室失败')
      }
    },
    selectRoom(room) {
      this.selectedRoomId = room.id
    },
    async confirmRebook() {
      if (!this.selectedRoomId || !this.rebookMeeting) return
      
      const selectedRoom = this.recommendedRooms.find(r => r.id === this.selectedRoomId)
      this.$confirm(`确认将会议「${this.rebookMeeting.title}」预约到「${selectedRoom.name}」?`, '确认重新预约', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const updatedMeeting = { ...this.rebookMeeting, roomId: this.selectedRoomId, roomName: selectedRoom.name, status: 'SCHEDULED' }
          await meetingApi.update(this.rebookMeeting.id, updatedMeeting, false)
          this.$message.success('重新预约成功')
          this.recommendDialogVisible = false
          this.loadMeetings()
        } catch (error) {
          this.$message.error(error.response?.data?.message || '重新预约失败')
        }
      }).catch(() => {})
    },
    async checkAndRelease() {
      try {
        await meetingApi.checkAndRelease()
        this.$message.success('已检查并释放人数不足的会议室')
        this.loadMeetings()
      } catch (error) {
        this.$message.error('检查释放失败')
      }
    },
    confirmAttendance(meeting) {
      this.currentMeeting = meeting
      this.confirmedCount = meeting.confirmedAttendees || 0
      this.attendanceDialogVisible = true
    },
    async submitAttendance() {
      try {
        await meetingApi.confirmAttendance(this.currentMeeting.id, this.confirmedCount)
        this.$message.success('确认成功')
        this.attendanceDialogVisible = false
        this.loadMeetings()
      } catch (error) {
        this.$message.error('确认失败')
      }
    },
    editMeeting(meeting) {
      this.$router.push({
        path: '/meetings/create',
        query: {
          editId: meeting.id,
          isRecurrence: meeting.recurrenceType && meeting.recurrenceType !== 'NONE'
        }
      })
    },
    showCancelDialog(meeting) {
      this.currentMeeting = meeting
      this.cancelDialogVisible = true
    },
    async cancelMeeting(updateWholeSeries) {
      try {
        if (updateWholeSeries && this.currentMeeting.recurrenceGroupId) {
          const meetingsToCancel = this.meetings.filter(
            m => m.recurrenceGroupId === this.currentMeeting.recurrenceGroupId
          )
          for (const m of meetingsToCancel) {
            await meetingApi.delete(m.id)
          }
        } else {
          await meetingApi.delete(this.currentMeeting.id)
        }
        this.$message.success('取消成功')
        this.cancelDialogVisible = false
        this.loadMeetings()
      } catch (error) {
        this.$message.error('取消失败')
      }
    }
  }
}
</script>

<style scoped>
.meeting-list {
  text-align: left;
}
</style>
