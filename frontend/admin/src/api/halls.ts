import http from '@/api/http'
import type { HallSaveRequest, HallVO, SeatCellPayload, SeatGridVO } from '@/types/api'

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

export function saveSeatGrid(id: number, seats: SeatCellPayload[]) {
  return http.post<unknown, void>(`/admin/halls/${id}/seats`, { seats })
}
