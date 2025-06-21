
// components/common/NotificationsContainer.jsx
import React from 'react';
import { CSSTransition, TransitionGroup } from 'react-transition-group';
import './NotificationsContainer.css';

const NotificationsContainer = ({ notifications = [] }) => {
  if (notifications.length === 0) return null;

  return (
    <div className="notifications-container" role="region" aria-label="Notifications">
      <TransitionGroup>
        {notifications.map((notification) => (
          <CSSTransition
            key={notification.id}
            timeout={300}
            classNames="notification"
          >
            <div
              className={`notification notification--${notification.type}`}
              role="alert"
              aria-live="polite"
            >
              <div className="notification__content">
                <span className="notification__message">
                  {notification.message}
                </span>
                {notification.action && (
                  <button
                    className="notification__action"
                    onClick={notification.action.onClick}
                  >
                    {notification.action.label}
                  </button>
                )}
              </div>
              {notification.dismissible !== false && (
                <button
                  className="notification__close"
                  onClick={() => notification.onDismiss?.(notification.id)}
                  aria-label="Fermer la notification"
                >
                  ×
                </button>
              )}
            </div>
          </CSSTransition>
        ))}
      </TransitionGroup>
    </div>
  );
};

export default NotificationsContainer;