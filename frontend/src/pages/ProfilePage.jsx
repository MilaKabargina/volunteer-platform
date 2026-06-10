import { useEffect, useState } from 'react';
import Header from '../components/Header';
import { getProfile } from '../api/api';

export default function ProfilePage() {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getProfile()
      .then(setUser)
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="hero">Загрузка...</div>;

  const getStatusName = (status) => {
    const statusMap = {
      NO_PARTICIPATION: 'Новичок',
      BEGINNER: 'Начинающий',
      INTERMEDIATE: 'Опытный',
      ADVANCED: 'Эксперт'
    };
    return statusMap[status] || 'Новичок';
  };

  return (
    <>
      <Header />
      <div className="profile-container">
        <div className="profile-card">
          <div className="avatar">
            {user?.firstName?.[0]}{user?.secondName?.[0]}
          </div>
          <h2>{user?.firstName} {user?.secondName}</h2>
          <p className="username">@{user?.login}</p>

          <div className="stats-grid">
            <div className="stat-item">
              <span className="stat-value">{user?.ratingPoints || 0}</span>
              <span className="stat-label">Рейтинг</span>
            </div>
            <div className="stat-item">
              <span className="stat-value">⭐</span>
              <span className="stat-label">{getStatusName(user?.status)}</span>
            </div>
          </div>

          <div className="info-row">
            {user?.email}
          </div>

          <button className="btn-outline" onClick={() => alert('Редактирование профиля появится позже')}>
            Редактировать профиль
          </button>
        </div>
      </div>
    </>
  );
}