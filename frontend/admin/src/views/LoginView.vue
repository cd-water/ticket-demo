<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { adminLogin } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'
import { passwordRules, usernameRules } from '@/utils/rules'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

const formRef = ref<FormInstance>()
const loading = ref(false)
const form = reactive({ username: '', password: '' })

const rules: FormRules = {
  username: usernameRules,
  password: passwordRules(),
}

async function doLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const data = await adminLogin(form.username, form.password)
    auth.setSession(data.token, data.admin)
    ElMessage.success(`欢迎，${data.admin.username}`)
    const redirect = (route.query.redirect as string) || '/dashboard'
    await router.push(redirect)
  } catch {
    /* 错误提示已由 http.ts 统一弹出 */
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login">
    <div class="login-card">
      <div class="brand">电影票务系统<span class="dot"></span>管理后台</div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        size="large"
        @submit.prevent="doLogin"
      >
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" autocomplete="username" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            show-password
            autocomplete="current-password"
          />
        </el-form-item>
        <button type="submit" class="login-btn" :disabled="loading">
          {{ loading ? '登录中…' : '登 录' }}
        </button>
      </el-form>

      <div class="tear" aria-hidden="true"></div>
    </div>
  </div>
</template>

<style scoped>
.login {
  min-height: 100dvh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: radial-gradient(1000px 600px at 70% -10%, #3a3200 0%, var(--bg) 60%);
  padding: 24px;
}

.login-card {
  --card-pad-x: 40px;
  width: 460px;
  max-width: 100%;
  background: var(--panel);
  border: 1px solid var(--line);
  border-radius: 22px;
  padding: 44px var(--card-pad-x) 36px;
}

.brand {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  font-family: var(--font-serif);
  font-size: 24px;
  font-weight: 900;
  letter-spacing: 1px;
  /* 标题与表单的间距 */
  margin-bottom: 34px;
}

.brand .dot {
  width: 14px;
  height: 14px;
  border-radius: 4px;
  background: var(--brand);
  box-shadow: 0 0 18px rgba(255, 195, 0, 0.6);
}

.login-card :deep(.el-form-item) {
  margin-bottom: 18px;
}

.login-card :deep(.el-input__wrapper) {
  background: var(--bg);
  box-shadow: 0 0 0 1px var(--line) inset;
  border-radius: 10px;
  padding: 4px 14px;
}

.login-card :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--brand) inset;
}

.login-card :deep(.el-input__inner) {
  color: var(--ink);
  height: 40px;
  caret-color: var(--brand);
}

.login-card :deep(.el-input__inner::placeholder) {
  color: var(--ink-2);
}

/* 密码可见性图标颜色 */
.login-card :deep(.el-input__suffix .el-icon) {
  color: var(--ink-2);
}

.login-btn {
  width: 100%;
  padding: 13px;
  border: none;
  border-radius: 10px;
  background: var(--brand);
  color: var(--on-brand);
  font-family: inherit;
  font-size: 15px;
  font-weight: 800;
  letter-spacing: 6px;
  cursor: pointer;
  transition: background 0.15s;
}

.login-btn:hover {
  background: var(--brand-deep);
  color: #fff;
}

.login-btn:disabled {
  opacity: 0.7;
  cursor: wait;
}

/* 撕票线：票根打孔 + 两端缺口（卡片收尾） */
.tear {
  position: relative;
  border-top: 2px dashed var(--line);
  margin: 28px calc(var(--card-pad-x) * -1) -16px;
}

@media (max-width: 480px) {
  .login-card {
    --card-pad-x: 24px;
    padding-top: 32px;
    padding-bottom: 28px;
  }
}

.tear::before,
.tear::after {
  content: '';
  position: absolute;
  top: -11px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: var(--bg);
}

.tear::before {
  left: -11px;
}

.tear::after {
  right: -11px;
}
</style>
