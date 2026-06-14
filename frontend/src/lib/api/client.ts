import axios, { AxiosHeaders } from 'axios'

import { clearStoredAuthToken, getStoredAuthToken } from '../auth/tokenStorage'
import { isUnauthorizedApiError } from './errors'

const DEFAULT_API_BASE_URL = 'http://localhost:18080'

export const AUTH_UNAUTHORIZED_EVENT_NAME = 'retailcore-pos:auth-unauthorized'

const baseURL = import.meta.env.VITE_API_BASE_URL?.trim() || DEFAULT_API_BASE_URL

export const apiClient = axios.create({
  baseURL,
  headers: {
    'Content-Type': 'application/json',
  },
})

apiClient.interceptors.request.use((config) => {
  const token = getStoredAuthToken()

  if (!token) {
    return config
  }

  const headers = AxiosHeaders.from(config.headers)
  headers.set('Authorization', `Bearer ${token}`)

  return {
    ...config,
    headers,
  }
})

apiClient.interceptors.response.use(
  (response) => response,
  (error: unknown) => {
    if (isUnauthorizedApiError(error)) {
      clearStoredAuthToken()

      if (typeof window !== 'undefined') {
        window.dispatchEvent(new Event(AUTH_UNAUTHORIZED_EVENT_NAME))
      }
    }

    return Promise.reject(error)
  },
)
