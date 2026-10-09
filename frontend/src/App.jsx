import { BrowserRouter } from 'react-router-dom'
import './App.css'
import Navbar from './components/Navbar'
import Footer from './components/Footer'
import AppRoutes from './routes/AppRoutes'
import { AuthProvider } from './context/AuthContext'

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <div className="app-shell">
          <Navbar />
          <main className="app-main">
            <AppRoutes />
          </main>
          <Footer />
        </div>
      </AuthProvider>
    </BrowserRouter>
  )
}

export default App
