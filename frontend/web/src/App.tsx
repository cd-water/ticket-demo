import { Routes, Route } from 'react-router'
import HomePage from '@/pages/HomePage'
import LoginPage from '@/pages/LoginPage'
import { ToastHost } from '@/components/Toast'

export default function App() {
  return (
    <>
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="*" element={<HomePage />} />
      </Routes>
      <ToastHost />
    </>
  )
}
