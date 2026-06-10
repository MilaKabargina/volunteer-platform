import { useEffect, useState } from 'react';
import { useLocation } from 'react-router-dom';
import Header from '../components/Header';
import { getMyApplications } from '../api/api';

export default function MyApplicationsPage() {
  const [applications, setApplications] = useState([]);
  const [loading, setLoading] = useState(true);
  const location = useLocation();

  async function loadApplications() {
    try {
      setLoading(true);
      const data = await getMyApplications();
      setApplications(data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadApplications();
  }, [location.key]);

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
        <h1 style={{ color: 'var(--text-primary)', marginBottom: '1rem' }}>Мои отклики</h1>

        {loading && <p>Загрузка...</p>}
        {!loading && applications.length === 0 && (
          <div className="card" style={{ padding: '2rem', textAlign: 'center' }}>
            Вы ещё не откликнулись ни на одну инициативу
          </div>
        )}
        {applications.map((app) => (
          <div key={app.id} className="initiative-card">
            <h3>{app.initiativeTitle}</h3>
            <div className="initiative-meta">
              Статус: {getStatusText(app.status)}
            </div>
            <p><strong>Ваше сообщение:</strong> {app.message}</p>
            {app.status === 'APPROVED' && app.contactInfo && (
              <div className="contact-info" style={{
                marginTop: '0.75rem',
                padding: '0.75rem',
                background: 'rgba(90, 138, 48, 0.1)',
                borderRadius: '0.75rem',
                borderLeft: '3px solid #5a8a30'
              }}>
                <strong>Контактная информация:</strong>
                <p style={{ marginTop: '0.25rem' }}>{app.contactInfo}</p>
              </div>
            )}
          </div>
        ))}
      </div>
    </>
  );
}