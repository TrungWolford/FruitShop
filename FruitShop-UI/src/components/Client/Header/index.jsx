import { Link } from 'react-router-dom'
import className from 'classnames/bind'
import styles from './Header.module.scss'

const cx = className.bind(styles)

function Header() {
  return (
    <header>
      <div className={cx('wrapper')}>
        <div className={cx('container')}>
          <a href="">Contact</a>
        </div>
      </div>
    </header>
  )
}

export default Header