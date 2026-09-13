import type { Component } from 'vue'
import {
  Odometer,
  Film,
  OfficeBuilding,
  User,
  Avatar,
  Picture,
  Tickets,
  VideoCamera,
  Calendar,
} from '@element-plus/icons-vue'

export interface MenuItem {
  /** 路由 path（/ + key） */
  key: string
  title: string
  icon: Component
}

/** 平台管理员菜单（role=0） */
export const SUPER_MENUS: MenuItem[] = [
  { key: 'dashboard', title: '仪表盘', icon: Odometer },
  { key: 'movies', title: '电影管理', icon: Film },
  { key: 'cinemas', title: '影院管理', icon: OfficeBuilding },
  { key: 'users', title: '用户管理', icon: User },
  { key: 'banners', title: '轮播图管理', icon: Picture },
  { key: 'admins', title: '管理员管理', icon: Avatar },
]

/** 影院管理员菜单（role=1） */
export const CINEMA_MENUS: MenuItem[] = [
  { key: 'dashboard', title: '仪表盘', icon: Odometer },
  { key: 'halls', title: '影厅管理', icon: VideoCamera },
  { key: 'screenings', title: '排场管理', icon: Calendar },
  { key: 'orders', title: '订单管理', icon: Tickets },
]

/** 全部菜单（路由表用，按 key 去重合并） */
export const ALL_MENUS: MenuItem[] = [
  ...SUPER_MENUS,
  ...CINEMA_MENUS.filter((m) => !SUPER_MENUS.some((s) => s.key === m.key)),
]

/** 由后端返回的 admin.role 决定可见菜单 */
export function menusForRole(role?: number): MenuItem[] {
  return role === 1 ? CINEMA_MENUS : SUPER_MENUS
}
