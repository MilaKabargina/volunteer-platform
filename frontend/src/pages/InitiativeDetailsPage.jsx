import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import Header from '../components/Header';
import { getInitiativeById, getMyApplications, createApplication, updateApplicationStatus } from '../api/api';
import { useAuth } from '../context/AuthContext';

export default function InitiativeDetailsPage() {
  const { id } = useParams();
  const [initiative, setInitiative] = useState(null);
  const [applications, setApplications] = useState([]);
  const [myApplications, setMyApplications] = useState([]);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(true);
  const [applying, setApplying] = useState(false);
  const { user } = useAuth();

  useEffect(() => {
    if (id) {
      loadData();
    }
  }, [id, user]);

  async function loadData() {
    try {
      setLoading(true);
      setError('');

      const initiativeData = await getInitiativeById(id);
      const myAppsData = await getMyApplications();

      setInitiative(initiativeData);
      setMyApplications(myAppsData);

      const token = localStorage.getItem('token');
      const currentUserLogin = user?.login;
      const authorLogin = initiativeData.authorLogin;

      const isAuthorFlag = currentUserLogin && authorLogin &&
        currentUserLogin.toLowerCase() === authorLogin.toLowerCase();

      if (isAuthorFlag && token) {
        const response = await fetch(`/api/v1/applications/initiative/${id}/paged?page=0&size=100`, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });
        if (response.ok) {
          const data = await response.json();
          setApplications(data.content || []);
        } else {
          setApplications([]);
        }
      } else {
        setApplications([]);
      }
    } catch (err) {
      console.error('Ошибка загрузки:', err);
      setError(err.message || 'Ошибка загрузки данных');
    } finally {
      setLoading(false);
    }
  }

  const hasApplied = myApplications.some(app => app.initiativeId === parseInt(id));

  const isAuthor = initiative && user &&
    user.login?.toLowerCase() === initiative.authorLogin?.toLowerCase();

  async function handleApply() {
    if (!window.confirm('Откликнуться на эту инициативу?')) return;
    setApplying(true);
    try {
      await createApplication({
        idInitiative: parseInt(id),
        message: `Хочу участвовать в "${initiative.title}"`
      });
      setSuccess('Заявка отправлена!');
      setTimeout(() => setSuccess(''), 3000);
      const updatedMyApps = await getMyApplications();
      setMyApplications(updatedMyApps);
    } catch (err) {
      setError(err.message || 'Ошибка отправки заявки');
      setTimeout(() => setError(''), 3000);
    } finally {
      setApplying(false);
    }
  }

  async function handleStatusChange(appId, newStatus) {
    try {
      await updateApplicationStatus(appId, newStatus);
      setSuccess(`Заявка ${newStatus === 'APPROVED' ? 'одобрена' : 'отклонена'}`);
      setTimeout(() => setSuccess(''), 3000);
      await loadData();
    } catch (err) {
      setError(err.message || 'Ошибка изменения статуса');
      setTimeout(() => setError(''), 3000);
    }
  }

  async function handleThank(appId) {
    if (!window.confirm('Поблагодарить участника? Рейтинг участника повысится')) return;
    try {
      const response = await fetch(`/api/v1/applications/${appId}/thank`, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
          'Content-Type': 'application/json'
        }
      });
      if (response.ok) {
        setSuccess('Участник поблагодарен! Рейтинг повышен');
        setTimeout(() => {
          setSuccess('');
          window.location.reload();
        }, 1500);
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

  const getStatusText = (status) => {
    switch (status) {
      case 'APPROVED': return 'Одобрена';
      case 'REJECTED': return 'Отклонена';
      case 'COMPLETED': return 'Завершена';
      default: return 'На рассмотрении';
    }
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'APPROVED': return '#5a8a30';
      case 'REJECTED': return '#d48a5a';
      case 'COMPLETED': return '#2b7de9';
      default: return '#f39c12';
    }
  };

  if (loading) return (
    <>
      <Header />
      <div className="hero">Загрузка...</div>
    </>
  );

  if (!initiative) return (
    <>
      <Header />
      <div className="hero">Инициатива не найдена</div>
    </>
  );

  const isCurrentUserAuthor = user && initiative &&
    user.login?.toLowerCase() === initiative.authorLogin?.toLowerCase();

  return (
    <>
      <Header />
      <div className="initiatives-container">
        <div className="initiative-card" style={{ marginBottom: '2rem' }}>
          <h1>{initiative.title}</h1>
          <div className="initiative-meta">
            {initiative.category || 'Без категории'} |
            {initiative.city && `${initiative.city} | `}
            Автор: {initiative.authorLogin}
          </div>
          <p style={{ margin: '1rem 0', whiteSpace: 'pre-wrap' }}>{initiative.description}</p>

          {!isCurrentUserAuthor && !hasApplied && (
            <div style={{ marginTop: '30px', marginBottom: '30px' }}>
              <button onClick={handleApply} disabled={applying} className="btn-primary">
                {applying ? 'Отправка...' : 'Откликнуться'}
              </button>
            </div>
          )}
          {!isCurrentUserAuthor && hasApplied && (
            <div className="success-message" style={{ display: 'inline-block', marginTop: '30px', marginBottom: '30px' }}>
              Вы уже откликнулись
            </div>
          )}
          {initiative.contactInfo && (
            <div className="contact-info">
              <strong>Контактная информация:</strong>
              <p style={{ marginTop: '0.25rem' }}>{initiative.contactInfo}</p>
            </div>
          )}
        </div>

        {isCurrentUserAuthor && (
          <div>
            <h2 style={{ marginBottom: '1rem' }}>Отклики на инициативу</h2>
            {error && <div className="error-message">{error}</div>}
            {success && <div className="success-message">{success}</div>}

            {applications.length === 0 ? (
              <div className="card" style={{ padding: '2rem', textAlign: 'center' }}>
                Пока нет откликов
              </div>
            ) : (
              applications.map((app) => (
                <div key={app.id} className="initiative-card">
                  <div className="initiative-meta">
                    От: {app.applicantLogin} |
                    Статус: <span style={{ color: getStatusColor(app.status), fontWeight: '500' }}>
                      {getStatusText(app.status)}
                    </span>
                  </div>
                  <p><strong>Сообщение:</strong> {app.message}</p>
                  <div className="initiative-actions">
                    {app.status === 'PENDING' && (
                      <>
                        <button onClick={() => handleStatusChange(app.id, 'APPROVED')} className="btn-small btn-edit">
                          Одобрить
                        </button>
                        <button onClick={() => handleStatusChange(app.id, 'REJECTED')} className="btn-small btn-delete">
                          Отклонить
                        </button>
                      </>
                    )}
                    {app.status === 'APPROVED' && (
                      <button onClick={() => handleThank(app.id)} className="btn-small" style={{ background: '#f39c12', color: 'white' }}>
                        Поблагодарить
                      </button>
                    )}
                    {app.status === 'COMPLETED' && (
                      <span className="btn-small" style={{ background: 'rgba(43, 125, 233, 0.2)', color: '#2b7de9' }}>
                        Участник отблагодарен
                      </span>
                    )}
                  </div>
                </div>
              ))
            )}
          </div>
        )}
      </div>
    </>
  );
}