// 模拟登录和注册的API函数
export function loginAPI(data) {
  // 这里返回一个Promise模拟API调用
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      // 模拟验证逻辑
      if (data.username && data.password) {
        resolve({
          code: 200,
          message: '登录成功',
          data: {
            token: 'mock-jwt-token-' + Date.now(),
            user: {
              id: 1,
              username: data.username,
              email: 'user@example.com',
              avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=' + data.username
            }
          }
        })
      } else {
        reject(new Error('用户名或密码不能为空'))
      }
    }, 1000) // 模拟网络延迟
  })
}

export function registerAPI(data) {
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      if (data.username && data.email && data.password) {
        resolve({
          code: 200,
          message: '注册成功',
          data: null
        })
      } else {
        reject(new Error('请填写完整的注册信息'))
      }
    }, 1000)
  })
}

export function forgetPasswordAPI(data) {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        code: 200,
        message: '重置密码链接已发送到您的邮箱',
        data: null
      })
    }, 1000)
  })
}

export function logoutAPI() {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        code: 200,
        message: '登出成功',
        data: null
      })
    }, 500)
  })
}

export function getUserInfoAPI() {
  return new Promise((resolve) => {
    setTimeout(() => {
      const token = localStorage.getItem('token')
      if (token) {
        resolve({
          code: 200,
          data: {
            id: 1,
            username: 'demo_user',
            email: 'demo@example.com',
            avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=demo'
          }
        })
      } else {
        resolve({
          code: 401,
          message: '未授权'
        })
      }
    }, 500)
  })
}