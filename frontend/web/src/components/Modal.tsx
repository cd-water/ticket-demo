import type { ReactNode } from 'react'

interface ModalProps {
  open: boolean
  title: string
  width?: number
  onClose: () => void
  children: ReactNode
}

/** 通用遮罩弹窗：点击遮罩关闭，容器内点击不冒泡 */
export function Modal({ open, title, width = 400, onClose, children }: ModalProps) {
  if (!open) return null
  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/30 p-6"
      onClick={onClose}
    >
      <div
        className="rounded-[20px] bg-card p-7 shadow-[var(--shadow-card)]"
        style={{ width, maxWidth: '100%' }}
        onClick={(e) => e.stopPropagation()}
      >
        <h2 className="mb-4 font-[family-name:var(--font-serif-cn)] text-lg font-extrabold">
          {title}
        </h2>
        {children}
      </div>
    </div>
  )
}