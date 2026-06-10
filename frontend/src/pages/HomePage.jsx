import { useAuth } from '../context/AuthContext';
import { useState } from 'react';
import Header from '../components/Header';
import LoginModal from '../components/LoginModal';
import RegisterModal from '../components/RegisterModal';

const heroWithBgStyle = {
  backgroundImage: "url('/photo.jpg')",
  backgroundSize: "cover",
  backgroundPosition: "center",
  backgroundRepeat: "no-repeat",
  position: "relative",
};

const overlayStyle = {
  position: "absolute",
  top: 0,
  left: 0,
  right: 0,
  bottom: 0,
  background: "rgba(0, 0, 0, 0.45)",
  zIndex: 1,
};

const contentStyle = {
  position: "relative",
  zIndex: 2,
};

export default function HomePage() {
  const { isAuthenticated, user } = useAuth();
  const [loginOpen, setLoginOpen] = useState(false);
  const [registerOpen, setRegisterOpen] = useState(false);

  if (isAuthenticated) {
    return (
      <>
        <Header />
        <div className="hero" style={heroWithBgStyle}>
          <div style={overlayStyle} />
          <div className="hero-content" style={contentStyle}>
            <h1>Приветствуем, <span className="highlight">{user?.login}</span>!</h1>
            <p>Твоя волонтёрская история начинается. Создавай инициативы и вдохновляй других!</p>
            <div className="hero-buttons">
              <a href="/initiatives" className="btn-primary">Мои инициативы</a>
              <a href="/profile" className="btn-secondary">Профиль</a>
            </div>
          </div>
        </div>
      </>
    );
  }

  return (
    <>
      <Header minimal />
      <div className="hero" style={heroWithBgStyle}>
        <div style={overlayStyle} />
        <div className="hero-content" style={contentStyle}>
          <h1>Твоё доброе дело<br />начинается <span className="highlight">здесь</span></h1>
          <p>Присоединяйся к сообществу волонтёров. Создавай и поддерживай инициативы, получай рейтинг и статусы.</p>
          <div className="hero-buttons">
            <button onClick={() => setRegisterOpen(true)} className="btn-primary">Создать аккаунт</button>
            <button onClick={() => setLoginOpen(true)} className="btn-secondary">Войти</button>
          </div>
        </div>
      </div>
      <LoginModal isOpen={loginOpen} onClose={() => setLoginOpen(false)} />
      <RegisterModal isOpen={registerOpen} onClose={() => setRegisterOpen(false)} />
    </>
  );
}