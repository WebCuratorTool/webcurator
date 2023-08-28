import type { Target } from "./types/target"
export type UserToken = string
export type Targets = { targets: [] }

export async function login(username: string, password: string) {
  const request = {
    method: "POST",
    body: new URLSearchParams({ username, password })
  }
  const response = await fetch('auth', request)
  const token: UserToken = await response.text()
  return token
}

export async function getTargets(token:string) {
  const headers = { Authorization: 'Bearer ' + token }
  const response = await fetch('/api/targets', { headers })
  const targets: Targets = await response.json()
  return targets.targets
}

export async function getTarget(token:string, id:string) {
  const headers = { Authorization: 'Bearer ' + token }
  const response = await fetch(`/api/targets/${id}`, { headers })
  const target: Target = await response.json()
  return target
}