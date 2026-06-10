const TOKEN_KEY = 'washpro_token'
const USER_INFO_KEY = 'washpro_user_info'

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
}

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setUserInfo(info: object): void {
  localStorage.setItem(USER_INFO_KEY, JSON.stringify(info))
}

export function getUserInfo(): object | null {
  const raw = localStorage.getItem(USER_INFO_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw)
  } catch {
    return null
  }
}

export function removeAuth(): void {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_INFO_KEY)
}

export function isLoggedIn(): boolean {
  return !!getToken()
}

export function getUserId(): number | null {
  const info = getUserInfo() as Record<string, unknown> | null
  if (info && typeof info.userId === 'number') {
    return info.userId
  }
  return null
}