import { useEffect, useState } from 'react'
import type { ToastType } from '@/components/toast'

interface ToastItem {
  id: number
  message: string
  type: ToastType
}

let seq = 0

/** 渲染于 App 根部；配合 components/toast.ts 的 showToast() 使用。 */
export function ToastHost() {
  const [items, setItems] = useState<ToastItem[]>([])

  useEffect(() => {
    function onToast(e: Event) {
      const { message, type } = (e as CustomEvent<{ message: string; type: ToastType }>).detail
      const id = ++seq
      setItems((prev) => [...prev, { id, message, type }])
      setTimeout(() => setItems((prev) => prev.filter((t) => t.id !== id)), 2400)
    }
    window.addEventListener('app-toast', onToast)
    return () => window.removeEventListener('app-toast', onToast)
  }, [])

  return (
    <div className="pointer-events-none fixed bottom-7 left-1/2 z-50 -translate-x-1/2">
      {items.map((t) => (
        <div
          key={t.id}
          className={`mt-2 rounded-xl px-6 py-3 text-sm text-white shadow-lg ${
            t.type === 'success' ? 'bg-[#2FA84F]' : t.type === 'error' ? 'bg-[#E5484D]' : 'bg-ink'
          }`}
        >
          {t.message}
        </div>
      ))}
    </div>
  )
}
