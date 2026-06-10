import { useEffect, useState, useRef } from 'react';
import { useAuth } from '../context/AuthContext';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export default function NotificationBell() {
  const { token } = useAuth();
  const [unreadCount, setUnreadCount] = useState(0);
  const [showDropdown, setShowDropdown] = useState(false);
  const [notifications, setNotifications] = useState([]);
  const stompClient = useRef(null);

  useEffect(() => {
    if ('Notification' in window && Notification.permission === 'default') {
      Notification.requestPermission();
    }
  }, []);

  useEffect(() => {
    if (token) {
      connectWebSocket();
      loadUnreadCount();
      loadNotifications();

      return () => {
        if (stompClient.current) {
          stompClient.current.deactivate();
        }
      };
    }
  }, [token]);

  const connectWebSocket = () => {
    const client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
      connectHeaders: {
        'Authorization': `Bearer ${token}`
      },
      debug: (str) => console.log('WebSocket:', str),
      reconnectDelay: 5000,
      onConnect: () => {
        console.log('WebSocket подключен');

        const userId = getUserIdFromToken();

        client.subscribe(`/topic/notifications/${userId}`, (message) => {
          const notification = JSON.parse(message.body);
          console.log('Получено новое уведомление:', notification);

          if (notification.type === 'NEW_NOTIFICATION') {
            setNotifications(prev => [notification.data, ...prev]);
            setUnreadCount(prev => prev + 1);

            if (Notification.permission === 'granted') {
              new Notification('Новое уведомление', {
                body: notification.data.message,
                icon: '/favicon.ico'
              });
            }
          }
        });

        client.subscribe(`/topic/notifications/count/${userId}`, (message) => {
          const update = JSON.parse(message.body);
          if (update.type === 'COUNT_UPDATE') {
            console.log('Обновление счётчика:', update.data);
            setUnreadCount(update.data);
          }
        });
      },
      onStompError: (frame) => {
        console.error('WebSocket ошибка:', frame);
      }
    });

    client.activate();
    stompClient.current = client;
  };

  const getUserIdFromToken = () => {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.userId || payload.sub;
    } catch (e) {
      console.error('Ошибка парсинга токена:', e);
      return null;
    }
  };

  async function safeJsonParse(response) {
    const text = await response.text();
    console.log('Raw response (first 500 chars):', text.substring(0, 500));

    if (text.trim().startsWith('<') || text.includes('<!DOCTYPE')) {
      console.error('Server returned HTML instead of JSON:', text.substring(0, 200));
      throw new Error('Server returned HTML (probably error page)');
    }

    try {
      const cleanText = text.trim().replace(/^\uFEFF/, '');
      return JSON.parse(cleanText);
    } catch (e) {
      console.error('JSON parse error:', e.message);
      throw new Error(`Invalid JSON: ${e.message}`);
    }
  }

  async function loadUnreadCount() {
    if (!token) return;

    try {
      const res = await fetch('/api/v1/notifications/unread/count', {
        headers: { 'Authorization': `Bearer ${token}` }
      });

      if (!res.ok) {
        throw new Error(`HTTP ${res.status}: ${res.statusText}`);
      }

      const data = await safeJsonParse(res);

      if (typeof data === 'number') {
        setUnreadCount(data);
      } else if (data && typeof data === 'object' && data.count !== undefined) {
        setUnreadCount(data.count);
      } else {
        console.warn('Unexpected unread count format:', data);
        setUnreadCount(0);
      }
    } catch (err) {
      console.error('loadUnreadCount error:', err);
      setUnreadCount(0);
    }
  }

  async function loadNotifications() {
    if (!token) return;

    try {
      const res = await fetch('/api/v1/notifications?page=0&size=20', {
        headers: { 'Authorization': `Bearer ${token}` }
      });

      if (!res.ok) {
        throw new Error(`HTTP ${res.status}: ${res.statusText}`);
      }

      const data = await res.json();
      console.log('Получены уведомления:', data);

      if (data && data.content && Array.isArray(data.content)) {
        setNotifications(data.content);
      } else if (Array.isArray(data)) {
        setNotifications(data);
      } else {
        console.warn('Неожиданный формат данных:', data);
        setNotifications([]);
      }
    } catch (err) {
      console.error('loadNotifications error:', err);
      setNotifications([]);
    }
  }

  async function markAsRead(id) {
    if (!token) return;

    try {
      const res = await fetch(`/api/v1/notifications/${id}/read`, {
        method: 'PUT',
        headers: { 'Authorization': `Bearer ${token}` }
      });

      if (!res.ok) throw new Error(`HTTP ${res.status}`);

      setNotifications(prev =>
        prev.map(n => n.id === id ? { ...n, read: true } : n)
      );
      setUnreadCount(prev => Math.max(0, prev - 1));

    } catch (err) {
      console.error('markAsRead error:', err);
    }
  }

  async function markAllAsRead() {
    if (!token) return;

    try {
      const res = await fetch('/api/v1/notifications/read-all', {
        method: 'PUT',
        headers: { 'Authorization': `Bearer ${token}` }
      });

      if (!res.ok) throw new Error(`HTTP ${res.status}`);

      setNotifications(prev => prev.map(n => ({ ...n, read: true })));
      setUnreadCount(0);

    } catch (err) {
      console.error('markAllAsRead error:', err);
    }
  }

  function toggleDropdown() {
    if (!showDropdown && token) {
      loadNotifications();
    }
    setShowDropdown(!showDropdown);
  }

  const handleNotificationClick = async (notification) => {
    if (!notification.read) {
      await markAsRead(notification.id);
    }
    window.location.href = notification.link;
  };

  const hasUnread = unreadCount > 0;

  return (
    <div className="notification-bell">
      <button onClick={toggleDropdown} className="bell-button">
        Уведомления
        {hasUnread && <span className="notification-count">{unreadCount}</span>}
      </button>

      {showDropdown && (
        <div className="notification-dropdown">
          <div className="dropdown-header">
            <span>Уведомления</span>
            {hasUnread && (
              <button onClick={markAllAsRead} className="mark-all-read">Прочитать всё</button>
            )}
          </div>
          <div className="dropdown-list">
            {notifications.length === 0 ? (
              <div className="empty">Нет уведомлений</div>
            ) : (
              notifications.map(n => (
                <div
                  key={n.id}
                  className={`notification-item ${!n.read ? 'unread' : ''}`}
                  onClick={() => handleNotificationClick(n)}
                >
                  <div className="message">{n.message}</div>
                  <div className="date">{new Date(n.createdAt).toLocaleString()}</div>
                </div>
              ))
            )}
          </div>
        </div>
      )}
    </div>
  );
}