import axios from 'axios'

const request = axios.create({
  baseURL: 'http://localhost:8005/api',
  timeout: 10000
})

export const meetingApi = {
  getAll: () => request.get('/meetings'),
  getById: (id) => request.get(`/meetings/${id}`),
  create: (data) => request.post('/meetings', data),
  update: (id, data, updateWholeSeries) => 
    request.put(`/meetings/${id}?updateWholeSeries=${updateWholeSeries}`, data),
  delete: (id) => request.delete(`/meetings/${id}`),
  checkConflict: (data) => request.post('/meetings/check-conflict', data),
  preempt: (newMeeting, preemptedMeetingId) => 
    request.post(`/meetings/preempt?preemptedMeetingId=${preemptedMeetingId}`, newMeeting),
  recommendRooms: (id) => request.post(`/meetings/${id}/recommend-rooms`),
  confirmAttendance: (id, confirmedCount) => 
    request.post(`/meetings/${id}/confirm-attendance?confirmedCount=${confirmedCount}`),
  getUpcoming: (minutes) => request.get(`/meetings/upcoming?minutes=${minutes}`),
  checkAndRelease: () => request.post('/meetings/check-release')
}

export const roomApi = {
  getAll: () => request.get('/rooms')
}

export const employeeApi = {
  getAll: () => request.get('/employees')
}
