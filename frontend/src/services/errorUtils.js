// The backend joins validation errors as "field: message; field: message".
// This turns that string into a { field: message } map for form highlighting.
export function toFieldErrors(detail) {
  const fieldErrors = {}
  if (!detail || typeof detail !== 'string') {
    return fieldErrors
  }
  detail.split(';').forEach((part) => {
    const separator = part.indexOf(':')
    if (separator > 0) {
      const field = part.slice(0, separator).trim()
      const message = part.slice(separator + 1).trim()
      if (field && message) {
        fieldErrors[field] = message
      }
    }
  })
  return fieldErrors
}
