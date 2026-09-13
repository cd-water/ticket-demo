import http from '@/api/http'
import type { HallSaveRequest, HallVO, SeatCellRequest, SeatGridVO } from '@/types/api'

export function listHalls() {
  return http.get<unknown, HallVO[]>('/admin/halls')
}

export function saveHall(body: HallSaveRequest) {
  return http.post<unknown, void>('/admin/halls/save', body)
}

export function deleteHall(id: number) {
  return http.post<unknown, void>(`/admin/halls/${id}/delete`)
}

export function getSeatGrid(id: number) {
  return http.get<unknown, SeatGridVO>(`/admin/halls/${id}/seats`)
}

export function saveSeatGrid(id: number, seats: SeatCellRequest[]) {
  return http.post<unknown, void>(`/admin/halls/${id}/seats`, { seats })
}
