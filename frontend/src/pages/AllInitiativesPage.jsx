import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import Header from '../components/Header';
import { createApplication } from '../api/api';
import { useAuth } from '../context/AuthContext';

export default function AllInitiativesPage() {
  const [initiatives, setInitiatives] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const { user } = useAuth();

  const PAGE_SIZE = 20;

  useEffect(() => {
    loadInitiatives();
  }, [page]);

  async function loadInitiatives() {
    try {
      setLoading(true);
      const response = await fetch(`/api/v1/initiatives?page=${page}&size=${PAGE_SIZE}`, {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      });
      if (!response.ok) throw new Error('Ошибка загрузки');
      const data = await response.json();
      setInitiatives(data.content);
      setTotalPages(data.totalPages);
    } catch (err) {
      setError(err.message || 'Ошибка загрузки инициатив');
    } finally {
      setLoading(false);
    }
  }

  async function handleApply(initiativeId, title) {
    if (!window.confirm(`Откликнуться на инициативу "${title}"?`)) return;
    try {
      await createApplication({
        idInitiative: initiativeId,
        message: `Хочу помочь в инициативе "${title}"`
      });
      setSuccess(`Заявка на "${title}" отправлена!`);
      setTimeout(() => setSuccess(''), 3000);
    } catch (err) {
      setError(err.message || 'Ошибка отправки заявки');
      setTimeout(() => setError(''), 3000);
    }
  }

  const isAuthor = (initiative) => initiative.authorLogin === user?.login;

  return (
    <>
      <Header />
      <div className="initiatives-container">
        <h1 style={{ marginBottom: '1rem', color: 'var(--text-primary)' }}>
          Все инициативы
        </h1>

        {error && <div className="error-message">{error}</div>}
        {success && <div className="success-message">{success}</div>}

        {loading ? (
          <div className="card" style={{ padding: '2rem', textAlign: 'center' }}>Загрузка...</div>
        ) : initiatives.length === 0 ? (
          <div className="card" style={{ padding: '2rem', textAlign: 'center' }}>Нет инициатив</div>
        ) : (
          <>
            {initiatives.map((initiative) => (
              <div key={initiative.id} className="initiative-card">
                <h3>{initiative.title}</h3>
                <div className="initiative-meta">
                  {initiative.category || 'Без категории'} |
                  {initiative.city && ` ${initiative.city} | `}
                  👤 Автор: {initiative.authorLogin}
                </div>
                <p>
                  {initiative.description.length > 140
                    ? initiative.description.slice(0, 140) + '...'
                    : initiative.description}
                </p>
                <div className="initiative-actions">
                  <Link to={`/initiatives/${initiative.id}`} className="btn-small btn-edit">
                    Подробнее
                  </Link>
                  {!isAuthor(initiative) && (
                    <button onClick={() => handleApply(initiative.id, initiative.title)} className="btn-small">
                      Откликнуться
                    </button>
                  )}
                  {isAuthor(initiative) && (
                    <span className="btn-small" style={{ background: 'rgba(0,0,0,0.1)', color: '#888', cursor: 'not-allowed' }}>
                      Ваша инициатива
                    </span>
                  )}
                </div>
              </div>
            ))}

            {totalPages > 1 && (
              <div className="pagination">
                <button onClick={() => setPage(p => Math.max(0, p - 1))} disabled={page === 0}>
                  Назад
                </button>
                <span>Страница {page + 1} из {totalPages}</span>
                <button onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))} disabled={page === totalPages - 1}>
                  Вперёд
                </button>
              </div>
            )}
          </>
        )}
      </div>
    </>
  );
}