import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useState } from 'react';
import LoginModal from './LoginModal';
import RegisterModal from './RegisterModal';
import NotificationBell from './NotificationBell';

export default function Header({ minimal }) {
  const { user, logout, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const [loginOpen, setLoginOpen] = useState(false);
  const [registerOpen, setRegisterOpen] = useState(false);

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <header className="header">
      <div className="logo">
        <Link to="/">Volunteer</Link>
      </div>
      <nav>
        {isAuthenticated ? (
          <>
            <Link to="/all-initiatives">Все инициативы</Link>
            <Link to="/initiatives">Мои инициативы</Link>
            <Link to="/my-applications">Мои отклики</Link>

            <NotificationBell />

            <Link to="/profile">Профиль</Link>
            {user?.roles?.includes('ROLE_ADMIN') && (
              <Link to="/admin">Админ панель</Link>
            )}
            <button onClick={handleLogout} className="logout-nav">Выйти</button>
          </>
        ) : (
          !minimal && (
            <>
              <button onClick={() => setLoginOpen(true)}>Войти</button>
              <button onClick={() => setRegisterOpen(true)} className="register-nav">
                Регистрация
              </button>
            </>
          )
        )}
      </nav>
      <LoginModal isOpen={loginOpen} onClose={() => setLoginOpen(false)} />
      <RegisterModal isOpen={registerOpen} onClose={() => setRegisterOpen(false)} />
    </header>
  );
}