import type { Component } from 'vue'
import { Odometer, Film, OfficeBuilding, User, Picture, Avatar } from '@element-plus/icons-vue'

export interface MenuItem {
  /** 路由 path（/ + key） */
  key: string
  title: string
  icon: Component
}

/** 侧边栏与路由表的唯一来源；新增一项必须在 router 的 VIEWS 里登记同名组件 */
export const MENUS = [
  { key: 'dashboard', title: '仪表盘', icon: Odometer },
  { key: 'movies', title: '电影管理', icon: Film },
  { key: 'cinemas', title: '影院管理', icon: OfficeBuilding },
  { key: 'banners', title: '轮播图管理', icon: Picture },
  { key: 'users', title: '用户管理', icon: User },
  { key: 'admins', title: '管理员管理', icon: Avatar },
] as const satisfies readonly MenuItem[]

export type MenuKey = (typeof MENUS)[number]['key']
