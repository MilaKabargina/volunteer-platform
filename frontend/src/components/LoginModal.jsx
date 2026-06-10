import { useState } from 'react';
import { login } from '../api/api';
import { useAuth } from '../context/AuthContext';
import Modal from './Modal';

export default function LoginModal({ isOpen, onClose }) {
  const [form, setForm] = useState({ login: '', password: '' });
  const [error, setError] = useState('');
  const { login: authLogin } = useAuth();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      const data = await login(form.login, form.password);
      authLogin(data.token);
      onClose();
    } catch (err) {
      setError(err.message || 'Ошибка входа');
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose}>
      <form onSubmit={handleSubmit} className="auth-form">
        <h2>Добро пожаловать</h2>
        <p className="sub">Войдите в аккаунт</p>
        <input
          type="text"
          placeholder="Логин"
          value={form.login}
          onChange={(e) => setForm({ ...form, login: e.target.value })}
          required
        />
        <input
          type="password"
          placeholder="Пароль"
          value={form.password}
          onChange={(e) => setForm({ ...form, password: e.target.value })}
          required
        />
        {error && <div className="error-message">{error}</div>}
        <button type="submit" className="btn-primary">Войти</button>
      </form>
    </Modal>
  );
}