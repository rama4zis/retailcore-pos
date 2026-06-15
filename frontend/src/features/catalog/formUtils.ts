export function nullableTrimmedText(value: string) {
  const trimmedValue = value.trim()
  return trimmedValue.length > 0 ? trimmedValue : null
}

export function requiredTrimmedText(value: string) {
  return value.trim()
}
