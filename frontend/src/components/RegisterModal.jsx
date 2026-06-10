import { useState } from 'react';
import { register, login } from '../api/api';
import { useAuth } from '../context/AuthContext';
import Modal from './Modal';

export default function RegisterModal({ isOpen, onClose }) {
  const [form, setForm] = useState({
    login: '', password: '', email: '', firstName: '', secondName: ''
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login: authLogin } = useAuth();

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await register(form);
      const loginData = await login(form.login, form.password);
      authLogin(loginData.token);
      onClose();
    } catch (err) {
      setError(err.message || 'Ошибка регистрации');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose}>
      <form onSubmit={handleSubmit} className="auth-form">
        <h2>Создать аккаунт</h2>
        <p className="sub">Присоединяйтесь к сообществу</p>
        <input type="text" name="login" placeholder="Логин" value={form.login} onChange={handleChange} required />
        <input type="password" name="password" placeholder="Пароль" value={form.password} onChange={handleChange} required />
        <input type="email" name="email" placeholder="Email" value={form.email} onChange={handleChange} required />
        <input type="text" name="firstName" placeholder="Имя" value={form.firstName} onChange={handleChange} required />
        <input type="text" name="secondName" placeholder="Фамилия" value={form.secondName} onChange={handleChange} required />
        {error && <div className="error-message">{error}</div>}
        <button type="submit" className="btn-primary" disabled={loading}>
          {loading ? 'Регистрация...' : 'Зарегистрироваться'}
        </button>
      </form>
    </Modal>
  );
}