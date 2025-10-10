import classNames from 'classnames/bind'
import styles from './Button.module.scss'
import { Link } from 'react-router-dom'

const cx = classNames.bind(styles)

function Button({ to, href, children, onClick, topIcon, small=false, large=false, rounded=false, ...passProps }) {


  const props = {
    onClick,
    ...passProps
  }

  let Comp = 'button'

  if (to) {
    props.to = to
    Comp = 'a'
  } else if (href) {
    props.href = href
    Comp = Link
  }

  const classes = cx('wrapper', {
    rounded,
    small,
    large
  })

  return (
    <Comp className={classes} {...passProps}>
      {topIcon && <span className={cx('icon')}>{topIcon}</span>}
      <span className={cx('title')}>{children}</span>
    </Comp>
  )
}

export default Button