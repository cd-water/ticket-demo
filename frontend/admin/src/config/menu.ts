import type { Component } from 'vue'
import { Odometer, Film, OfficeBuilding, User, Picture, Avatar } from '@element-plus/icons-vue'

export interface MenuItem {
  /** 路由 path（/ + key） */
  key: string
  title: string
  icon: Component
}

export const MENUS: MenuItem[] = [
  { key: 'dashboard', title: '仪表盘', icon: Odometer },
  { key: 'movies', title: '电影管理', icon: Film },
  { key: 'cinemas', title: '影院管理', icon: OfficeBuilding },
  { key: 'banners', title: '轮播图管理', icon: Picture },
  { key: 'users', title: '用户管理', icon: User },
  { key: 'admins', title: '管理员管理', icon: Avatar },
]
