import axios from 'axios'

export interface FieldErrorResponse {
  field: string
  message: string
}

export interface ApiErrorResponse {
  timestamp: string
  status: number
  error: string
  message: string
  fieldErrors: FieldErrorResponse[]
}

export type ApiFieldErrorMap = Record<string, string>

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null && !Array.isArray(value)
}

function isFieldErrorResponse(value: unknown): value is FieldErrorResponse {
  if (!isRecord(value)) {
    return false
  }

  return typeof value.field === 'string' && typeof value.message === 'string'
}

export function isApiErrorResponse(value: unknown): value is ApiErrorResponse {
  if (!isRecord(value)) {
    return false
  }

  return (
    typeof value.timestamp === 'string' &&
    typeof value.status === 'number' &&
    typeof value.error === 'string' &&
    typeof value.message === 'string' &&
    Array.isArray(value.fieldErrors) &&
    value.fieldErrors.every(isFieldErrorResponse)
  )
}

export function getApiErrorResponse(error: unknown) {
  if (isApiErrorResponse(error)) {
    return error
  }

  if (axios.isAxiosError<unknown>(error)) {
    const responseData = error.response?.data

    if (isApiErrorResponse(responseData)) {
      return responseData
    }
  }

  return null
}

export function getApiErrorStatus(error: unknown) {
  const apiError = getApiErrorResponse(error)

  if (apiError) {
    return apiError.status
  }

  if (axios.isAxiosError<unknown>(error)) {
    return error.response?.status ?? null
  }

  return null
}

export function getApiErrorMessage(
  error: unknown,
  fallbackMessage = 'Something went wrong. Try again.',
) {
  const apiError = getApiErrorResponse(error)

  if (apiError?.message) {
    return apiError.message
  }

  if (axios.isAxiosError<unknown>(error) && !error.response) {
    return 'Network error. Check that the backend is running and CORS is configured.'
  }

  if (error instanceof Error && error.message) {
    return error.message
  }

  return fallbackMessage
}

export function getApiFieldErrors(error: unknown): ApiFieldErrorMap {
  const apiError = getApiErrorResponse(error)

  if (!apiError) {
    return {}
  }

  return apiError.fieldErrors.reduce<ApiFieldErrorMap>(
    (fieldErrors, fieldError) => ({
      ...fieldErrors,
      [fieldError.field]: fieldError.message,
    }),
    {},
  )
}

export function hasApiFieldErrors(error: unknown) {
  return Object.keys(getApiFieldErrors(error)).length > 0
}

export function isUnauthorizedApiError(error: unknown) {
  return getApiErrorStatus(error) === 401
}

export function isForbiddenApiError(error: unknown) {
  return getApiErrorStatus(error) === 403
}
