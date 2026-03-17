import React, { useState } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { logout } from '../../features/auth/authSlice';
import { ChevronDown, LogOut, User as UserIcon } from 'lucide-react';

const Header = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { user } = useSelector((state) => state.auth);
  const [dropdownOpen, setDropdownOpen] = useState(false);

  const handleLogout = () => {
    dispatch(logout());
    navigate('/login');
  };

  return (
    <header className="h-16 bg-white border-b border-slate-200 flex items-center justify-end px-6">
      <div className="relative">
        <button
          onClick={() => setDropdownOpen(!dropdownOpen)}
          className="flex items-center space-x-2"
        >
          <div className="w-9 h-9 rounded-full bg-blue-500 flex items-center justify-center text-white font-bold">
            {user?.fullName?.charAt(0).toUpperCase() || 'A'}
          </div>
          <span className="text-sm font-medium text-slate-700 hidden md:inline">{user?.fullName}</span>
          <ChevronDown className="w-4 h-4 text-slate-500" />
        </button>

        {dropdownOpen && (
          <div className="absolute right-0 mt-2 w-56 bg-white rounded-lg shadow-lg border border-slate-200 py-1 z-10">
            <div className="px-4 py-2 border-b">
              <p className="text-sm font-semibold text-slate-800">{user?.fullName}</p>
              <p className="text-xs text-slate-500 truncate">{user?.email}</p>
            </div>
            <a href="#" className="flex items-center w-full px-4 py-2 text-sm text-slate-700 hover:bg-slate-100">
              <UserIcon className="w-4 h-4 mr-2" />
              My Profile
            </a>
            <button
              onClick={handleLogout}
              className="flex items-center w-full px-4 py-2 text-sm text-red-600 hover:bg-slate-100"
            >
              <LogOut className="w-4 h-4 mr-2" />
              Logout
            </button>
          </div>
        )}
      </div>
    </header>
  );
};

export default Header;
