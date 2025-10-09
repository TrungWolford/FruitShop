import { Outlet } from 'react-router-dom'
import Header from '~/components/Client/Header'
import Footer from '~/components/Client/Footer'

function ClientLayout() {
  return (
    <div>
      <Header />
      <main style={{ minHeight: '80vh', padding: '20px' }}>
        <Outlet /> {/* Render child routes */}
      </main>
      <Footer />
    </div>
  )
}

export default ClientLayout