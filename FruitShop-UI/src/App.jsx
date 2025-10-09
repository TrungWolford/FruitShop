import { Route, Routes } from 'react-router-dom'
import ClientLayout from '~/layout/ClientLayout'
import Home from '~/pages/Client/Home/Home'
import Product from '~/pages/Client/Products/Product'
import Cart from '~/pages/Client/Cart/Cart'

function App() {
  return (
    <Routes>
      <Route path="/" element={<ClientLayout />}>
        <Route index element={<Home />} />
        <Route path="product" element={<Product />} />
        <Route path="cart" element={<Cart />} />
      </Route>
    </Routes>
  )
}


export default App
