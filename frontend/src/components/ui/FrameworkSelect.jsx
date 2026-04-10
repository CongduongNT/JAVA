import React, { useState, useEffect } from 'react';
import { ChevronDown, BookOpen, Loader2 } from 'lucide-react';
import frameworkApi from '../../services/frameworkApi';

/**
 * FrameworkSelect – Dropdown chọn Curriculum Framework cho Teacher.
 * 
 * Props:
 * - value: ID của framework đã chọn
 * - onChange: function(id, framework) - callback khi chọn framework
 * - placeholder: text hiển thị khi chưa chọn
 * - disabled: boolean
 * - className: custom classes
 */
const FrameworkSelect = ({ 
  value, 
  onChange, 
  placeholder = 'Chọn chương trình khung...',
  disabled = false,
  className = ''
}) => {
  const [frameworks, setFrameworks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isOpen, setIsOpen] = useState(false);
  const [selectedFramework, setSelectedFramework] = useState(null);

  useEffect(() => {
    fetchFrameworks();
  }, []);

  useEffect(() => {
    if (value && frameworks.length > 0) {
      const found = frameworks.find(f => f.id.toString() === value.toString());
      setSelectedFramework(found || null);
    } else {
      setSelectedFramework(null);
    }
  }, [value, frameworks]);

  const fetchFrameworks = async () => {
    try {
      setLoading(true);
      const res = await frameworkApi.getPublishedFrameworks();
      setFrameworks(res.data || []);
    } catch (err) {
      console.error('Failed to fetch frameworks:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleSelect = (framework) => {
    setSelectedFramework(framework);
    onChange?.(framework.id, framework);
    setIsOpen(false);
  };

  // Group frameworks by subject for better organization
  const groupedFrameworks = frameworks.reduce((acc, framework) => {
    const subject = framework.subject || 'Khác';
    if (!acc[subject]) {
      acc[subject] = [];
    }
    acc[subject].push(framework);
    return acc;
  }, {});

  if (loading) {
    return (
      <div className={`relative ${className}`}>
        <div className="w-full px-4 py-2.5 border border-slate-300 rounded-lg bg-slate-50 flex items-center gap-2 text-slate-500">
          <Loader2 size={18} className="animate-spin" />
          <span className="text-sm">Đang tải...</span>
        </div>
      </div>
    );
  }

  if (frameworks.length === 0) {
    return (
      <div className={`relative ${className}`}>
        <div className="w-full px-4 py-2.5 border border-slate-300 rounded-lg bg-slate-50 text-slate-500 text-sm">
          Chưa có framework nào được publish
        </div>
      </div>
    );
  }

  return (
    <div className={`relative ${className}`}>
      {/* Trigger */}
      <button
        type="button"
        onClick={() => !disabled && setIsOpen(!isOpen)}
        disabled={disabled}
        className={`w-full px-4 py-2.5 border rounded-lg flex items-center justify-between gap-2 transition-all ${
          disabled 
            ? 'bg-slate-100 text-slate-400 cursor-not-allowed border-slate-200' 
            : 'bg-white border-slate-300 hover:border-indigo-500 text-slate-700'
        }`}
      >
        <div className="flex items-center gap-2 overflow-hidden">
          <BookOpen size={18} className="text-indigo-500 shrink-0" />
          <span className="truncate text-sm">
            {selectedFramework ? selectedFramework.title : placeholder}
          </span>
        </div>
        <ChevronDown 
          size={18} 
          className={`text-slate-400 shrink-0 transition-transform ${isOpen ? 'rotate-180' : ''}`}
        />
      </button>

      {/* Dropdown */}
      {isOpen && (
        <>
          {/* Overlay to close dropdown when clicking outside */}
          <div 
            className="fixed inset-0 z-40"
            onClick={() => setIsOpen(false)}
          />
          
          {/* Dropdown Menu */}
          <div className="absolute z-50 w-full mt-1 bg-white border border-slate-200 rounded-lg shadow-lg max-h-80 overflow-auto">
            {Object.entries(groupedFrameworks).map(([subject, subjectFrameworks]) => (
              <div key={subject}>
                <div className="px-3 py-1.5 bg-slate-50 text-xs font-semibold text-slate-500 uppercase tracking-wider">
                  {subject}
                </div>
                {subjectFrameworks.map((framework) => (
                  <button
                    key={framework.id}
                    type="button"
                    onClick={() => handleSelect(framework)}
                    className={`w-full px-4 py-2.5 text-left hover:bg-indigo-50 transition-colors flex items-center gap-2 ${
                      selectedFramework?.id === framework.id 
                        ? 'bg-indigo-50 text-indigo-700' 
                        : 'text-slate-700'
                    }`}
                  >
                    <div className="flex-1 min-w-0">
                      <p className="text-sm font-medium truncate">{framework.title}</p>
                      {framework.gradeLevel && (
                        <p className="text-xs text-slate-500">Khối: {framework.gradeLevel}</p>
                      )}
                    </div>
                    {selectedFramework?.id === framework.id && (
                      <div className="w-2 h-2 rounded-full bg-indigo-500 shrink-0" />
                    )}
                  </button>
                ))}
              </div>
            ))}
          </div>
        </>
      )}
    </div>
  );
};

export default FrameworkSelect;
