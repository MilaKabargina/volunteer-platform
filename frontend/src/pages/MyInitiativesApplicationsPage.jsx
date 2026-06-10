import { useEffect, useState } from 'react';
import Header from '../components/Header';
import { updateApplicationStatus } from '../api/api';
import { useAuth } from '../context/AuthContext';

export default function MyInitiativesApplicationsPage() {
  const [applications, setApplications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const { user } = useAuth();

  useEffect(() => {
    loadApplications();
  }, []);

  async function loadApplications() {
    try {
      setLoading(true);
      const response = await fetch('/api/v1/applications/my-initiatives', {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      });
      const data = await response.json();
      setApplications(data);
    } catch (err) {
      setError(err.message || 'Ошибка загрузки заявок');
    } finally {
      setLoading(false);
    }
  }

  async function handleStatusChange(id, newStatus) {
    try {
      await updateApplicationStatus(id, newStatus);
      setSuccess(`Заявка ${newStatus === 'APPROVED' ? 'одобрена' : 'отклонена'}`);
      setTimeout(() => setSuccess(''), 3000);
      await loadApplications();
    } catch (err) {
      setError(err.message || 'Ошибка изменения статуса');
      setTimeout(() => setError(''), 3000);
    }
  }

  async function handleThank(id) {
    if (!window.confirm('Поблагодарить участника? Рейтинг участника повысится')) return;
    try {
      const response = await fetch(`/api/v1/applications/${id}/thank`, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
          'Content-Type': 'application/json'
        }
      });
      if (response.ok) {
        setSuccess('Участник поблагодарен! Рейтинг повышен');
        setTimeout(() => setSuccess(''), 3000);
        await loadApplications();
      } else {
        const errorData = await response.json();
        setError(errorData.message || 'Ошибка благодарности');
        setTimeout(() => setError(''), 3000);
      }
    } catch (err) {
      setError(err.message || 'Ошибка благодарности');
      setTimeout(() => setError(''), 3000);
    }
  }

  const getStatusColor = (status) => {
    switch (status) {
      case 'APPROVED': return '#5a8a30';
      case 'REJECTED': return '#d48a5a';
      case 'COMPLETED': return '#2b7de9';
      default: return '#f39c12';
    }
  };

  const getStatusText = (status) => {
    switch (status) {
      case 'APPROVED': return 'Одобрена';
      case 'REJECTED': return 'Отклонена';
      case 'COMPLETED': return 'Завершена';
      default: return 'На рассмотрении';
    }
  };

  return (
    <>
      <Header />
      <div className="initiatives-container">
        <h1 style={{ marginBottom: '1rem', color: 'var(--text-primary)' }}>
          Заявки на мои инициативы
        </h1>

        {error && (
          <div className="error-message" style={{ marginBottom: '1rem' }}>
            {error}
          </div>
        )}
        {success && (
          <div className="success-message" style={{ marginBottom: '1rem' }}>
            {success}
          </div>
        )}

        {loading ? (
          <div className="card" style={{ padding: '2rem', textAlign: 'center' }}>
            Загрузка...
          </div>
        ) : applications.length === 0 ? (
          <div className="card" style={{ padding: '2rem', textAlign: 'center' }}>
            Нет заявок на ваши инициативы
          </div>
        ) : (
          applications.map((app) => (
            <div key={app.id} className="initiative-card">
              <h3>Инициатива: {app.initiativeTitle}</h3>
              <div className="initiative-meta">
                От: {app.applicantLogin} |
                Статус: <span style={{ color: getStatusColor(app.status) }}>{getStatusText(app.status)}</span>
              </div>
              <p><strong>Сообщение:</strong> {app.message}</p>

              <div className="initiative-actions">
                {app.status === 'PENDING' && (
                  <>
                    <button
                      onClick={() => handleStatusChange(app.id, 'APPROVED')}
                      className="btn-small btn-edit"
                    >
                      Одобрить
                    </button>
                    <button
                      onClick={() => handleStatusChange(app.id, 'REJECTED')}
                      className="btn-small btn-delete"
                    >
                      Отклонить
                    </button>
                  </>
                )}
                {app.status === 'APPROVED' && (
                  <button
                    onClick={() => handleThank(app.id)}
                    className="btn-small"
                    style={{ background: '#f39c12', color: 'white' }}
                  >
                    Поблагодарить
                  </button>
                )}
                {app.status !== 'PENDING' && app.status !== 'APPROVED' && (
                  <span className="btn-small" style={{ background: 'rgba(0,0,0,0.1)', color: '#888' }}>
                    {app.status === 'APPROVED' ? 'Участник одобрен' :
                     app.status === 'COMPLETED' ? 'Участник отблагодарен' : 'Участник отклонён'}
                  </span>
                )}
              </div>
            </div>
          ))
        )}
      </div>
    </>
  );
}