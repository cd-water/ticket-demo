import type { FormItemRule } from 'element-plus'

/**
 * 与后端校验注解保持一致的通用表单规则。
 * 只放跨页面复用的规则；字段长度约束交给 el-input 的 maxlength（对应数据库列长度）。
 */

/** 后端 @Pattern：8-20 位且同时含字母和数字 */
export const PASSWORD_PATTERN = /^(?=.*[A-Za-z])(?=.*\d).{8,20}$/

/** t_admin.username：VARCHAR(32)，@NotBlank @Size(min = 5, max = 32) */
export const usernameRules: FormItemRule[] = [
  { required: true, message: '请输入用户名', trigger: 'blur' },
  { min: 5, max: 32, message: '用户名需5-32位', trigger: 'blur' },
]

/** 登录、新增管理员、重置密码三处的密码约束相同，只有空值提示不同 */
export function passwordRules(requiredMessage = '请输入密码'): FormItemRule[] {
  return [
    { required: true, message: requiredMessage, trigger: 'blur' },
    { pattern: PASSWORD_PATTERN, message: '密码需8-20位且含字母和数字', trigger: 'blur' },
  ]
}
