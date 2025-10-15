import React, { useEffect } from 'react'
import TopNavigation from '../../components/TopNavigation'
import Footer from '../../components/Footer'
import CategoryMainPage from '../../components/CategoryMainPage'
import MainBanner from '../../components/MainBanner'
import ProductSection from '../../components/ProductSection'

const Home: React.FC = () => {
  // Set page title for home page
  useEffect(() => {
    document.title = 'BookCity - Hệ thống nhà sách trực tuyến'
  }, [])

  return (
    <div className="bg-[#f2f3f5]">
      <TopNavigation />
      
      <main className="max-w-8xl mx-auto px-16 pt-0 pb-8">
        {/* Main content with 2.5:7.5 layout */}
        <div className="grid grid-cols-10 gap-6">
          {/* Left sidebar - 2.5 columns */}
          <div className="col-span-2">
            <div className="shadow-md">
              <CategoryMainPage />
            </div>
          </div>
          
          {/* Main content - 7.5 columns */}
          <div className="col-span-8">
            {/* Banner Section */}
            <div className="mb-6 shadow-md">
              <MainBanner />
            </div>
            

          </div>
        </div>

        {/* Product Section */}
        <div className="mt-6">
          <ProductSection />
        </div>
      </main>

      <Footer />
    </div>
  )
}

export default Home
