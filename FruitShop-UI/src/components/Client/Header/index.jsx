import classNames from 'classnames/bind'
import styles from './Header.module.scss'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faBars, faMagnifyingGlass } from '@fortawesome/free-solid-svg-icons'
import Button from '~/components/Button'
import { faPhoneVolume } from '@fortawesome/free-solid-svg-icons'

const cx = classNames.bind(styles)

function Header() {

  return (
    <header>
      <div className={cx('wrapper')}>
        <div className={cx('container')}>
          <div className={cx('icon-menu')}><FontAwesomeIcon icon={faBars}/>
          </div>
          <div className={cx('wrapper-logo')}>
            <img src="//theme.hstatic.net/200000377165/1001286359/14/logo_large.png?v=334" alt="Morning Fruit - Trái Cây Chất Lượng Cao" className={cx('img-logo')}/>
          </div>
          <div className={cx('search')}>
            <input type="text" className={cx('search-input')} placeholder='Tìm kiếm sản phẩm...'/>
            <button className={cx('search-icon')}>
              <FontAwesomeIcon icon={faMagnifyingGlass} />
            </button>
          </div>

          <div className={cx('action')}>
            <Button topIcon={<FontAwesomeIcon icon={faPhoneVolume}/>}>Hotline: 023456789</Button>
          </div>
        </div>
      </div>
    </header>
  )
}

export default Header