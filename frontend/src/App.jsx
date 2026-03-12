import { Routes, Route } from 'react-router-dom'
import './App.css'

function App() {
  return (
    <div className="min-h-screen bg-slate-900 text-white flex flex-col items-center justify-center p-4">
      <h1 className="text-4xl font-bold mb-4 bg-gradient-to-r from-blue-400 to-purple-500 bg-clip-text text-transparent">
        PlanbookAI
      </h1>
      <p className="text-slate-400 mb-8 max-w-md text-center">
        Empowering teachers with AI-driven lesson plans, question banks, and automated grading.
      </p>
      
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 w-full max-w-4xl">
        <FeatureCard title="AI Lesson Plans" desc="Generate structured lesson plans in seconds." />
        <FeatureCard title="Question Bank" desc="A tiered repository for all your subjects." />
        <FeatureCard title="OCR Grading" desc="Auto-grade answer sheets using AI." />
      </div>

      <div className="mt-12">
        <Routes>
          <Route path="/" element={<div className="text-green-400">Welcome to PBA Portal</div>} />
          <Route path="/login" element={<div>Login Page</div>} />
        </Routes>
      </div>
    </div>
  )
}

function FeatureCard({ title, desc }) {
  return (
    <div className="bg-slate-800 p-6 rounded-xl border border-slate-700 hover:border-blue-500 transition-all cursor-default group">
      <h3 className="text-xl font-semibold mb-2 group-hover:text-blue-400 transition-colors">{title}</h3>
      <p className="text-slate-400">{desc}</p>
    </div>
  )
}

export default App
