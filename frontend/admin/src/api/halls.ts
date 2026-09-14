import http from '@/api/http'
import type { HallSaveRequest, HallVO, SeatCellRequest, SeatGridVO } from '@/types/api'

export function listHallsByCinema(cinemaId: number) {
  return http.get<unknown, HallVO[]>(`/admin/cinemas/${cinemaId}/halls`)
}

export function saveHall(body: HallSaveRequest) {
  return http.post<unknown, void>('/admin/halls/save', body)
}

export function getSeatGrid(id: number) {
  return http.get<unknown, SeatGridVO>(`/admin/halls/${id}/seats`)
}

export function saveSeatGrid(id: number, seats: SeatCellRequest[]) {
  return http.post<unknown, void>(`/admin/halls/${id}/seats`, seats)
}
