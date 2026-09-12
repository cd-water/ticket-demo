/** 轻量命令式 toast：showToast() 可在任意非组件模块（如 axios 拦截器）调用，<ToastHost/> 渲染。 */
export type ToastType = 'success' | 'error' | 'info'

export function showToast(message: string, type: ToastType = 'info') {
  window.dispatchEvent(new CustomEvent('app-toast', { detail: { message, type } }))
}
