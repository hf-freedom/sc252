import Vue from 'vue'
import VueRouter from 'vue-router'
import Home from '../views/Home.vue'
import MeetingList from '../views/MeetingList.vue'
import MeetingCreate from '../views/MeetingCreate.vue'
import RoomList from '../views/RoomList.vue'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/meetings',
    name: 'MeetingList',
    component: MeetingList
  },
  {
    path: '/meetings/create',
    name: 'MeetingCreate',
    component: MeetingCreate
  },
  {
    path: '/rooms',
    name: 'RoomList',
    component: RoomList
  }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

export default router
