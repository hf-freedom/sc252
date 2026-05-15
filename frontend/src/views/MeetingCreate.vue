<template>
  <div class="meeting-create">
    <el-alert v-if="needRebookMeetings.length > 0" 
              title="有会议被抢占，需要重新预约" 
              type="warning" 
              show-icon
              :closable="false"
              style="margin-bottom: 20px;">
      <div slot="title">
        <span>有 <b style="color: #E6A23C;">{{ needRebookMeetings.length }}</b> 个会议被抢占，需要重新预约</span>
      </div>
      <div style="margin-top: 10px;">
        <el-table :data="needRebookMeetings" size="small" style="width: 100%" border>
          <el-table-column prop="title" label="会议主题" min-width="120"></el-table-column>
          <el-table-column prop="organizerName" label="组织者" width="100"></el-table-column>
          <el-table-column prop="expectedAttendees" label="人数" width="70">
            <template slot-scope="scope">{{ scope.row.expectedAttendees }}人</template>
          </el-table-column>
          <el-table-column label="时间" width="200">
            <template slot-scope="scope">
              <div style="font-size: 12px;">
                <div>开始: {{ formatTime(scope.row.startTime) }}</div>
                <div>结束: {{ formatTime(scope.row.endTime) }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template slot-scope="scope">
              <el-button size="mini" type="primary" icon="el-icon-refresh-right" @click="quickRebook(scope.row)">
                快速重新预约
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-alert>
    
    <el-card>
      <div slot="header">
        <span v-if="isEditMode">编辑会议</span>
        <span v-else>创建会议</span>
      </div>
      
      <el-alert v-if="isEditMode && isRecurrenceMeeting" 
                title="这是周期会议" 
                type="info" 
                show-icon
                style="margin-bottom: 20px;">
        <div slot="title">
          <span>这是一个周期会议，请选择更新范围</span>
        </div>
        <el-radio-group v-model="updateScope" style="margin-top: 10px;">
          <el-radio label="single" border>仅更新本次会议</el-radio>
          <el-radio label="whole" border>更新整组所有会议</el-radio>
        </el-radio-group>
      </el-alert>
      <el-form :model="form" label-width="120px" ref="form">
        <el-form-item label="会议主题" prop="title" required>
          <el-input v-model="form.title" placeholder="请输入会议主题"></el-input>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="组织者" prop="organizerId" required>
              <el-select v-model="form.organizerId" placeholder="请选择组织者">
                <el-option v-for="emp in employees" :key="emp.id" :label="emp.name" :value="emp.id"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="参会人数" prop="expectedAttendees" required>
              <el-input-number v-model="form.expectedAttendees" :min="1"></el-input-number>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="会议室" prop="roomId" required>
              <el-select v-model="form.roomId" placeholder="请选择会议室">
                <el-option v-for="room in rooms" :key="room.id" :label="`${room.name} (${room.capacity}人)`" :value="room.id"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="优先级" prop="priority" required>
              <el-select v-model="form.priority" placeholder="请选择优先级">
                <el-option label="低" value="LOW"></el-option>
                <el-option label="普通" value="NORMAL"></el-option>
                <el-option label="高" value="HIGH"></el-option>
                <el-option label="紧急" value="URGENT"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime" required>
              <el-date-picker v-model="form.startTime" type="datetime" placeholder="选择开始时间" style="width: 100%"></el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" prop="endTime" required>
              <el-date-picker v-model="form.endTime" type="datetime" placeholder="选择结束时间" style="width: 100%"></el-date-picker>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="参会人员" prop="attendeeIds" required>
          <el-select v-model="form.attendeeIds" multiple placeholder="请选择参会人员" style="width: 100%">
            <el-option v-for="emp in employees" :key="emp.id" :label="emp.name" :value="emp.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="周期会议">
          <el-checkbox v-model="isRecurrence">是</el-checkbox>
        </el-form-item>
        <el-row v-if="isRecurrence" :gutter="20">
          <el-col :span="12">
            <el-form-item label="重复类型">
              <el-select v-model="form.recurrenceType" placeholder="请选择">
                <el-option label="每天" value="DAILY"></el-option>
                <el-option label="每周" value="WEEKLY"></el-option>
                <el-option label="每月" value="MONTHLY"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束日期">
              <el-date-picker v-model="form.recurrenceEndDate" type="date" placeholder="选择结束日期" style="width: 100%"></el-date-picker>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="会议描述">
          <el-input type="textarea" v-model="form.description" placeholder="请输入会议描述"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="checkConflict">检查冲突</el-button>
          <el-button type="success" @click="submitForm">
            <span v-if="isEditMode">保存修改</span>
            <span v-else>创建会议</span>
          </el-button>
          <el-button v-if="isEditMode" @click="$router.push('/meetings')">取消编辑</el-button>
          <el-button v-else @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-dialog title="冲突检查结果" :visible.sync="conflictDialogVisible" width="65%">
      <div v-if="conflictResult.hasConflict">
        <el-alert :title="conflictResult.canPreempt ? '检测到冲突，但您可以发起抢占' : '检测到冲突，无法创建会议'" 
                  :type="conflictResult.canPreempt ? 'warning' : 'error'" 
                  show-icon>
        </el-alert>
        
        <el-divider content-position="left">
          <span style="font-weight: bold;">会议室冲突</span>
        </el-divider>
        <div v-if="conflictResult.conflictingMeetings && conflictResult.conflictingMeetings.length > 0">
          <el-table :data="conflictResult.conflictingMeetings" style="width: 100%" border>
            <el-table-column prop="title" label="会议主题" min-width="150"></el-table-column>
            <el-table-column prop="organizerName" label="组织者" width="100"></el-table-column>
            <el-table-column prop="roomName" label="会议室" width="100"></el-table-column>
            <el-table-column label="时间" width="200">
              <template slot-scope="scope">
                <div style="font-size: 12px;">
                  <div>开始: {{ formatTime(scope.row.startTime) }}</div>
                  <div>结束: {{ formatTime(scope.row.endTime) }}</div>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="priority" label="优先级" width="100">
              <template slot-scope="scope">
                <el-tag :type="getPriorityTagType(scope.row.priority)" size="small">
                  {{ getPriorityText(scope.row.priority) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" v-if="conflictResult.canPreempt">
              <template slot-scope="scope">
                <el-button size="small" type="danger" icon="el-icon-lock" @click="preemptMeeting(scope.row)">
                  发起抢占
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <div v-else>
          <el-empty description="无会议室冲突" :image-size="80"></el-empty>
        </div>

        <el-divider content-position="left" v-if="conflictResult.conflictingAttendees && conflictResult.conflictingAttendees.length > 0">
          <span style="font-weight: bold;">参会人冲突</span>
        </el-divider>
        <div v-if="conflictResult.conflictingAttendees && conflictResult.conflictingAttendees.length > 0" style="padding: 10px 0;">
          <el-tag v-for="name in conflictResult.conflictingAttendees" :key="name" type="warning" style="margin: 5px;">
            <i class="el-icon-user"></i> {{ name }}
          </el-tag>
        </div>

        <el-divider v-if="conflictResult.canPreempt"></el-divider>
        <el-alert v-if="conflictResult.canPreempt" type="success" show-icon>
          <template slot="title">
            <span>当前会议优先级: <el-tag type="success" size="small">{{ getPriorityText(form.priority) }}</el-tag></span>
          </template>
          您的会议优先级高于冲突的会议，可以点击「发起抢占」按钮抢占该会议室。被抢占的会议将进入重新预约流程。
        </el-alert>
      </div>
      <div v-else>
        <el-alert title="无冲突" type="success" description="可以正常创建会议" show-icon></el-alert>
        <div style="text-align: center; padding: 20px;">
          <el-button type="primary" @click="forceCreateMeeting">立即创建会议</el-button>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="conflictDialogVisible = false">关闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { meetingApi, roomApi, employeeApi } from '../api'

export default {
  name: 'MeetingCreatePage',
  data() {
    return {
      form: {
        id: '',
        title: '',
        organizerId: '',
        organizerName: '',
        roomId: '',
        roomName: '',
        startTime: '',
        endTime: '',
        expectedAttendees: 5,
        attendeeIds: [],
        attendeeNames: [],
        priority: 'NORMAL',
        description: '',
        recurrenceType: 'NONE',
        recurrenceEndDate: '',
        recurrenceGroupId: ''
      },
      isRecurrence: false,
      rooms: [],
      employees: [],
      allMeetings: [],
      conflictDialogVisible: false,
      conflictResult: {},
      isEditMode: false,
      editingMeetingId: '',
      updateScope: 'single',
      isRecurrenceMeeting: false
    }
  },
  computed: {
    needRebookMeetings() {
      return this.allMeetings.filter(m => m.status === 'NEEDS_REBOOKING')
    }
  },
  async mounted() {
    await this.loadData()
    
    if (this.$route.query.editId) {
      this.isEditMode = true
      this.editingMeetingId = this.$route.query.editId
      const meeting = this.allMeetings.find(m => m.id === this.$route.query.editId)
      if (meeting) {
        this.isRecurrenceMeeting = meeting.recurrenceType && meeting.recurrenceType !== 'NONE'
        this.form = { ...meeting }
        this.isRecurrence = this.isRecurrenceMeeting
        if (meeting.startTime) {
          this.form.startTime = new Date(meeting.startTime)
        }
        if (meeting.endTime) {
          this.form.endTime = new Date(meeting.endTime)
        }
        if (meeting.recurrenceEndDate) {
          this.form.recurrenceEndDate = new Date(meeting.recurrenceEndDate)
        }
      }
    }
    
    if (this.$route.query.rebookId) {
      const meeting = this.allMeetings.find(m => m.id === this.$route.query.rebookId)
      if (meeting) {
        this.quickRebook(meeting)
      }
    }
  },
  methods: {
    async loadData() {
      try {
        const [roomsRes, employeesRes, meetingsRes] = await Promise.all([
          roomApi.getAll(),
          employeeApi.getAll(),
          meetingApi.getAll()
        ])
        this.rooms = roomsRes.data.data || []
        this.employees = employeesRes.data.data || []
        this.allMeetings = meetingsRes.data.data || []
      } catch (error) {
        this.$message.error('加载数据失败')
      }
    },
    quickRebook(meeting) {
      this.form = {
        title: meeting.title,
        organizerId: meeting.organizerId,
        organizerName: meeting.organizerName,
        roomId: '',
        roomName: '',
        startTime: meeting.startTime,
        endTime: meeting.endTime,
        expectedAttendees: meeting.expectedAttendees,
        attendeeIds: meeting.attendeeIds || [],
        attendeeNames: meeting.attendeeNames || [],
        priority: meeting.priority,
        description: meeting.description || '',
        recurrenceType: meeting.recurrenceType || 'WEEKLY',
        recurrenceEndDate: meeting.recurrenceEndDate || ''
      }
      this.$message.info('已填充会议信息，请选择新会议室并提交')
    },
    getPriorityText(priority) {
      const map = { 'LOW': '低', 'NORMAL': '普通', 'HIGH': '高', 'URGENT': '紧急' }
      return map[priority] || priority
    },
    getPriorityTagType(priority) {
      const map = { 'LOW': 'info', 'NORMAL': '', 'HIGH': 'warning', 'URGENT': 'danger' }
      return map[priority] || ''
    },
    formatTime(time) {
      if (!time) return ''
      return new Date(time).toLocaleString('zh-CN')
    },
    async forceCreateMeeting() {
      const meetingData = this.prepareMeetingData()
      try {
        await meetingApi.create(meetingData)
        this.$message.success('创建成功')
        this.conflictDialogVisible = false
        await this.loadData()
        this.$router.push('/meetings')
      } catch (error) {
        this.$message.error(error.response?.data?.message || '创建失败')
      }
    },
    async checkConflict() {
      const meetingData = this.prepareMeetingData()
      try {
        const res = await meetingApi.checkConflict(meetingData)
        this.conflictResult = res.data.data || {}
        this.conflictDialogVisible = true
      } catch (error) {
        this.$message.error('检查冲突失败')
      }
    },
    prepareMeetingData() {
      const organizer = this.employees.find(e => e.id === this.form.organizerId)
      const room = this.rooms.find(r => r.id === this.form.roomId)
      const attendeeNames = this.form.attendeeIds.map(id => {
        const emp = this.employees.find(e => e.id === id)
        return emp ? emp.name : id
      })
      
      return {
        ...this.form,
        organizerName: organizer ? organizer.name : '',
        roomName: room ? room.name : '',
        attendeeNames: attendeeNames
      }
    },
    async submitForm() {
      if (!this.form.title || !this.form.organizerId || !this.form.roomId) {
        this.$message.error('请填写必填项')
        return
      }
      
      const meetingData = this.prepareMeetingData()
      
      try {
        if (!this.isEditMode) {
          const conflictRes = await meetingApi.checkConflict(meetingData)
          const conflictResult = conflictRes.data.data || {}
          
          if (conflictResult.hasConflict) {
            this.conflictResult = conflictResult
            this.conflictDialogVisible = true
            if (conflictResult.canPreempt) {
              this.$message.warning('检测到会议室冲突，但您的会议优先级更高，可以发起抢占')
            } else {
              this.$message.error('存在冲突，无法创建会议')
            }
            return
          }
          
          await meetingApi.create(meetingData)
          this.$message.success('创建成功')
        } else {
          const updateWholeSeries = this.isRecurrenceMeeting && this.updateScope === 'whole'
          await meetingApi.update(this.editingMeetingId, meetingData, updateWholeSeries)
          this.$message.success(updateWholeSeries ? '整组会议更新成功' : '会议更新成功')
        }
        
        await this.loadData()
        this.$router.push('/meetings')
      } catch (error) {
        this.$message.error(error.response?.data?.message || (this.isEditMode ? '更新失败' : '创建失败'))
      }
    },
    async preemptMeeting(meeting) {
      this.$confirm(`确认要抢占该会议室吗?\n将取消会议: ${meeting.title}`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const meetingData = this.prepareMeetingData()
          await meetingApi.preempt(meetingData, meeting.id)
          this.$message.success('抢占成功')
          this.conflictDialogVisible = false
          await this.loadData()
          this.$router.push('/meetings')
        } catch (error) {
          this.$message.error(error.response?.data?.message || '抢占失败')
        }
      }).catch(() => {})
    },
    resetForm() {
      this.$refs.form.resetFields()
    }
  }
}
</script>

<style scoped>
.meeting-create {
  text-align: left;
}
</style>
