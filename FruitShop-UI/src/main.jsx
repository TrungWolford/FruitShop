import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import App from './App.jsx'
import { BrowserRouter } from 'react-router-dom'
import GlobalStyles from '~/components/GlobalStyles'

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <GlobalStyles>
      <BrowserRouter>
        <App />
      </BrowserRouter>
    </GlobalStyles>
  </StrictMode>
)
