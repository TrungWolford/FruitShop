import React from 'react'

interface Custom {
  className?: string
  children?: any
}

const  Container: React.FC<Custom> = ({ className, children }) => {
  return (
    <div className={`pt-[68px] left-[130px] ml-[260px] pl-3 ${className}`}>{children}</div>
  )
}

export default Container