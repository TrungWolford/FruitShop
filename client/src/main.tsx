import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter as Router } from 'react-router-dom'
import { Provider } from 'react-redux'
import { store } from './store'
import AuthInitializer from './components/AuthInitializer'
import { Toaster } from 'sonner'
import './index.css'
import App from './App.tsx'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <Provider store={store}>
      <AuthInitializer />
      <Router>
        <App />
        <Toaster 
          position="top-right" 
          richColors 
          duration={3000}
        />
      </Router>
    </Provider>
  </StrictMode>,
)
