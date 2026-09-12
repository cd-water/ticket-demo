import http from '@/api/http'
import type { HallSaveRequest, HallVO, SeatCellPayload, SeatGridVO } from '@/types/api'

export function listHalls() {
  return http.get<unknown, HallVO[]>('/admin/halls')
}

export function createHall(body: HallSaveRequest) {
  return http.post<unknown, void>('/admin/halls', body)
}

export function updateHall(id: number, body: HallSaveRequest) {
  return http.put<unknown, void>(`/admin/halls/${id}`, body)
}

export function deleteHall(id: number) {
  return http.delete<unknown, void>(`/admin/halls/${id}`)
}

export function getSeatGrid(id: number) {
  return http.get<unknown, SeatGridVO>(`/admin/halls/${id}/seats`)
}

export function saveSeatGrid(id: number, seats: SeatCellPayload[]) {
  return http.put<unknown, void>(`/admin/halls/${id}/seats`, { seats })
}
