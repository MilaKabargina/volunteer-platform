import { useEffect, useState } from 'react';
import Header from '../components/Header';
import { useAuth } from '../context/AuthContext';

export default function AdminDashboard() {
  const [pendingInitiatives, setPendingInitiatives] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const { user } = useAuth();

  useEffect(() => {
    loadPendingInitiatives();
  }, []);

  async function loadPendingInitiatives() {
    try {
      const response = await fetch('/api/v1/initiatives/pending', {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      });
      const data = await response.json();
      setPendingInitiatives(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  async function handleApprove(id) {
    try {
      await fetch(`/api/v1/initiatives/${id}/approve`, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
          'Content-Type': 'application/json'
        }
      });
      await loadPendingInitiatives();
    } catch (err) {
      setError(err.message);
    }
  }

  async function handleReject(id) {
    const reason = prompt('Причина отклонения:');
    if (reason === null) return;

    try {
      await fetch(`/api/v1/initiatives/${id}/reject`, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(reason)
      });
      await loadPendingInitiatives();
    } catch (err) {
      setError(err.message);
    }
  }

  if (!user?.roles?.includes('ROLE_ADMIN')) {
    return <div className="hero">Доступ запрещён</div>;
  }

  return (
    <>
      <Header />
      <div className="admin-container">
        <h1>Админ панель</h1>
        <h2>Модерация инициатив</h2>

        {error && <div className="error-message">{error}</div>}
        {loading && <p>Загрузка...</p>}

        {!loading && pendingInitiatives.length === 0 && (
          <div className="card" style={{ padding: '2rem', textAlign: 'center' }}>
            Нет инициатив на модерацию
          </div>
        )}

        {pendingInitiatives.map((initiative) => (
          <div key={initiative.id} className="admin-card">
            <div className="admin-card-header">
              <strong>{initiative.title}</strong>
              <span>Автор: {initiative.authorLogin}</span>
            </div>
            <div className="admin-card-body">
              <p><strong>Категория:</strong> {initiative.category}</p>
              <p><strong>Описание:</strong> {initiative.description}</p>
            </div>
            <div className="admin-card-actions">
              <button onClick={() => handleApprove(initiative.id)} className="btn-approve">
                Одобрить
              </button>
              <button onClick={() => handleReject(initiative.id)} className="btn-reject">
                Отклонить
              </button>
            </div>
          </div>
        ))}
      </div>
    </>
  );
}