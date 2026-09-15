import { Routes, Route, Navigate } from 'react-router'
import HomePage from '@/pages/HomePage'
import MoviesPage from '@/pages/MoviesPage'
import MovieDetailPage from '@/pages/MovieDetailPage'
import CinemasPage from '@/pages/CinemasPage'
import CinemaDetailPage from '@/pages/CinemaDetailPage'
import MyPage from '@/pages/MyPage'
import LoginPage from '@/pages/LoginPage'
import { ToastHost } from '@/components/Toast'

export default function App() {
  return (
    <>
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/movies" element={<MoviesPage />} />
        <Route path="/movies/:id" element={<MovieDetailPage />} />
        <Route path="/cinemas" element={<CinemasPage />} />
        <Route path="/cinemas/:id" element={<CinemaDetailPage />} />
        <Route path="/me" element={<MyPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
      <ToastHost />
    </>
  )
}