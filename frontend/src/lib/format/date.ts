const dateTimeFormatter = new Intl.DateTimeFormat(undefined, {
  dateStyle: 'medium',
  timeStyle: 'short',
})

const monthFormatter = new Intl.DateTimeFormat(undefined, {
  month: 'long',
  year: 'numeric',
})

function padDatePart(value: number) {
  return value.toString().padStart(2, '0')
}

export function toLocalDateInputValue(date: Date) {
  const year = date.getFullYear()
  const month = padDatePart(date.getMonth() + 1)
  const day = padDatePart(date.getDate())

  return `${year}-${month}-${day}`
}

export function getCurrentYearMonth(date: Date) {
  return {
    month: date.getMonth() + 1,
    year: date.getFullYear(),
  }
}

export function formatDateTime(value: string | Date) {
  return dateTimeFormatter.format(new Date(value))
}

export function formatMonth(value: Date) {
  return monthFormatter.format(value)
}
