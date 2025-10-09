import { Link } from 'react-router-dom'

function Sidebar() {
  return (
    <nav>
      <Link to="/">Home</Link>
      <Link to="/product">Product</Link>
    </nav>
  )
}

export default Sidebar