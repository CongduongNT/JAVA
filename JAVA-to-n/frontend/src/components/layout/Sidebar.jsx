import React from 'react';
import { NavLink } from 'react-router-dom';
import { LayoutDashboard, BookCopy, FileText, Bot } from 'lucide-react';

const Sidebar = () => {
  const navItems = [
    { name: 'Dashboard', icon: LayoutDashboard, path: '/dashboard' },
    { name: 'Question Bank', icon: BookCopy, path: '/question-bank' },
    { name: 'Exam Generator', icon: FileText, path: '/exam-generator' },
    { name: 'OCR Grading', icon: Bot, path: '/ocr-grading' },
  ];

  return (
    <aside className="w-64 flex-shrink-0 bg-white border-r border-slate-200 flex flex-col">
      <div className="h-16 flex items-center justify-center border-b border-slate-200">
        <h1 className="text-2xl font-semibold font-display text-blue-600">PlanbookAI</h1>
      </div>
      <nav className="flex-1 px-4 py-4">
        <ul>
          {navItems.map((item) => (
            <li key={item.name}>
              <NavLink to={item.path} className={({ isActive }) => `flex items-center px-4 py-2.5 text-sm font-medium rounded-lg transition-colors ${isActive ? 'bg-blue-50 text-blue-600' : 'text-slate-600 hover:bg-slate-100'}`}>
                <item.icon className="w-5 h-5 mr-3" />
                {item.name}
              </NavLink>
            </li>
          ))}
        </ul>
      </nav>
    </aside>
  );
};

export default Sidebar;
