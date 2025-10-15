// Simple toast hook - will use browser alert for now
export const toast = ({
  title,
  description,
  variant = 'default'
}: {
  title?: string
  description?: string
  variant?: 'default' | 'destructive' | 'success'
}) => {
  const message = title ? `${title}: ${description || ''}` : description || ''
  
  if (variant === 'destructive') {
    alert(`❌ ${message}`)
  } else if (variant === 'success') {
    alert(`✅ ${message}`)
  } else {
    alert(`ℹ️ ${message}`)
  }
}
