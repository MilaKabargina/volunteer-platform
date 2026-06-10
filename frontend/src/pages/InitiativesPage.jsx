import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import Header from '../components/Header';
import {
  createInitiative,
  getMyInitiatives,
  updateInitiative,
  deleteInitiative,
  resubmitInitiative,
} from '../api/api';
import { CITIES } from '../constants/cities';

const CATEGORIES = [
  'Экология и уборка',
  'Помощь животным',
  'Образование и репетиторство',
  'Социальная помощь',
  'Медицина и здоровье',
  'Культура и искусство',
  'Спорт и активный отдых',
  'Технологии и IT',
  'Городское развитие',
  'События и фестивали',
];

export default function InitiativesPage() {
  const [initiatives, setInitiatives] = useState([]);
  const [form, setForm] = useState({ title: '', category: '', description: '', contactInfo: '', city: '' });
  const [editingId, setEditingId] = useState(null);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [isResubmit, setIsResubmit] = useState(false);

  async function loadInitiatives() {
    try {
      const data = await getMyInitiatives();
      setInitiatives(data);
    } catch (err) {
      setError(err.message);
    }
  }

  useEffect(() => {
    loadInitiatives();
  }, []);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm({ ...form, [name]: value });
  };

  const validateForm = () => {
    if (!form.title.trim()) { setError('Название не может быть пустым'); return false; }
    if (!form.category) { setError('Выберите категорию'); return false; }
    if (!form.description.trim()) { setError('Описание не может быть пустым или состоять из пробелов'); return false; }
    if (!form.contactInfo.trim()) { setError('Контактная информация обязательна'); return false; }
    if (!form.city) { setError('Выберите город'); return false; }
    return true;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    if (!validateForm()) return;

    try {
      if (editingId) {
        if (isResubmit) {
          await resubmitInitiative(editingId, form);
          setSuccess('Инициатива обновлена и отправлена на повторную модерацию');
        } else {
          await updateInitiative(editingId, form);
          setSuccess('Инициатива обновлена');
        }
      } else {
        await createInitiative(form);
        setSuccess('Инициатива создана и отправлена на модерацию');
      }
      setForm({ title: '', category: '', description: '', contactInfo: '', city: '' });
      setEditingId(null);
      setIsResubmit(false);
      await loadInitiatives();
    } catch (err) {
      setError(err.message);
    }
  };

  const handleEdit = (initiative) => {
    setEditingId(initiative.id);
    setForm({
      title: initiative.title,
      category: initiative.category,
      description: initiative.description,
      contactInfo: initiative.contactInfo || '',
      city: initiative.city || ''
    });
    setIsResubmit(initiative.status === 'REJECTED');
  };

  const handleDelete = async (id) => {
    if (window.confirm('Удалить инициативу?')) {
      await deleteInitiative(id);
      await loadInitiatives();
      if (editingId === id) {
        setEditingId(null);
        setForm({ title: '', category: '', description: '', contactInfo: '', city: '' });
        setIsResubmit(false);
      }
    }
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'APPROVED': return '#5a8a30';
      case 'REJECTED': return '#d48a5a';
      default: return '#f39c12';
    }
  };

  const getStatusText = (status) => {
    switch (status) {
      case 'APPROVED': return 'Одобрена';
      case 'REJECTED': return 'Отклонена';
      default: return 'На модерации';
    }
  };

  return (
    <>
      <Header />
      <div className="initiatives-container">
        <div className="form-card">
          <h2>{editingId ? (isResubmit ? 'Редактировать отклонённую инициативу' : 'Редактировать инициативу') : 'Создать инициативу'}</h2>
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <input
                type="text"
                name="title"
                placeholder="Название*"
                value={form.title}
                onChange={handleChange}
                required
              />
            </div>
            <div className="form-group">
              <textarea
                name="contactInfo"
                placeholder="Контактная информация* (телефон, адрес, ссылка на чат...)"
                value={form.contactInfo}
                onChange={handleChange}
                rows="2"
                required
                style={{
                  padding: '0.8rem 1rem',
                  background: 'rgba(255, 255, 255, 0.9)',
                  border: '1px solid rgba(45, 58, 31, 0.15)',
                  borderRadius: '1rem',
                  width: '100%'
                }}
              />
            </div>
            <div className="form-group">
              <select
                name="category"
                value={form.category}
                onChange={handleChange}
                required
                style={{
                  padding: '0.8rem 1rem',
                  background: 'rgba(255, 255, 255, 0.9)',
                  border: '1px solid rgba(45, 58, 31, 0.15)',
                  borderRadius: '1rem',
                  width: '100%',
                  fontFamily: 'inherit',
                  cursor: 'pointer'
                }}
              >
                <option value="" disabled>Выберите категорию*</option>
                {CATEGORIES.map(cat => (
                  <option key={cat} value={cat}>{cat}</option>
                ))}
              </select>
            </div>
            <div className="form-group">
              <select
                name="city"
                value={form.city}
                onChange={handleChange}
                required
                style={{
                  padding: '0.8rem 1rem',
                  background: 'rgba(255, 255, 255, 0.9)',
                  border: '1px solid rgba(45, 58, 31, 0.15)',
                  borderRadius: '1rem',
                  width: '100%',
                  fontFamily: 'inherit',
                  cursor: 'pointer'
                }}
              >
                <option value="" disabled>Выберите город*</option>
                {CITIES.map(city => (
                  <option key={city} value={city}>{city}</option>
                ))}
              </select>
            </div>
            <div className="form-group">
              <textarea
                name="description"
                placeholder="Описание*"
                rows="4"
                value={form.description}
                onChange={handleChange}
                required
              />
            </div>
            {error && <div className="error-message">{error}</div>}
            {success && <div className="success-message">{success}</div>}
            <button type="submit" className="btn-primary">
              {editingId ? (isResubmit ? 'Отправить на модерацию' : 'Сохранить') : 'Создать'}
            </button>
            {editingId && (
              <button
                type="button"
                className="btn-secondary"
                style={{ marginLeft: '0.5rem' }}
                onClick={() => {
                  setEditingId(null);
                  setForm({ title: '', category: '', description: '', contactInfo: '', city: '' });
                  setIsResubmit(false);
                }}
              >
                Отмена
              </button>
            )}
          </form>
        </div>

        <h2 style={{ marginBottom: '1rem', fontSize: '1.3rem', color: 'var(--text-primary)' }}>
          Мои инициативы
        </h2>
        {initiatives.length === 0 ? (
          <div className="card" style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-secondary)' }}>
            У вас пока нет инициатив. Создайте первую!
          </div>
        ) : (
          initiatives.map((initiative) => (
            <div key={initiative.id} className="initiative-card">
              <h3>{initiative.title}</h3>
              <div className="initiative-meta">
                {initiative.category || 'Без категории'} |
                {initiative.city && `${initiative.city} | `}
                Статус: <span style={{ color: getStatusColor(initiative.status), fontWeight: '500' }}>
                  {getStatusText(initiative.status)}
                </span>
              </div>
              {initiative.status === 'REJECTED' && initiative.moderationReason && (
                <div className="error-message" style={{ marginBottom: '0.5rem', fontSize: '0.8rem' }}>
                  Причина отклонения: {initiative.moderationReason}
                </div>
              )}
              <p>
                {initiative.description.length > 140
                  ? initiative.description.slice(0, 140) + '...'
                  : initiative.description}
              </p>
              <div className="initiative-actions">
                <Link to={`/initiatives/${initiative.id}`} className="btn-small btn-edit" style={{ textDecoration: 'none' }}>
                  Подробнее
                </Link>
                {(initiative.status === 'PENDING' || initiative.status === 'REJECTED') && (
                  <button onClick={() => handleEdit(initiative)} className="btn-small btn-edit">
                    Редактировать
                  </button>
                )}
                <button onClick={() => handleDelete(initiative.id)} className="btn-small btn-delete">
                  Удалить
                </button>
              </div>
            </div>
          ))
        )}
      </div>
    </>
  );
}