// src/components/profile/UserProfile/UserProfile.jsx
import React, { useState, useEffect } from 'react';
import { useAuth } from '../../../hooks/useAuth';
import { userService } from '../../../services/userService';
import ProfileSettings from '../ProfileSettings';
import AvatarUpload from '../AvatarUpload';
import ProgressBar from '../../progress/ProgressBar';
import StatsCard from '../../progress/StatsCard';
import Achievement from '../../progress/Achievement';
import Button from '../../common/Button';
import Modal from '../../common/Modal';
import Loading from '../../common/Loading';
import styles from './UserProfile.module.css';

const UserProfile = () => {
  const { user, updateUser } = useAuth();
  const [userStats, setUserStats] = useState(null);
  const [achievements, setAchievements] = useState([]);
  const [isEditing, setIsEditing] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const [activeTab, setActiveTab] = useState('overview');

  useEffect(() => {
    fetchUserData();
  }, [user?.id]);

  const fetchUserData = async () => {
    try {
      setIsLoading(true);
      const [statsData, achievementsData] = await Promise.all([
        userService.getUserStats(user.id),
        userService.getUserAchievements(user.id)
      ]);
      
      setUserStats(statsData);
      setAchievements(achievementsData);
    } catch (err) {
      setError('Erreur lors du chargement des données du profil');
      console.error('Error fetching user data:', err);
    } finally {
      setIsLoading(false);
    }
  };

  const handleAvatarUpdate = async (newAvatarUrl) => {
    try {
      await updateUser({ avatar: newAvatarUrl });
    } catch (err) {
      setError('Erreur lors de la mise à jour de l\'avatar');
    }
  };

  const handleProfileUpdate = async (updatedData) => {
    try {
      await updateUser(updatedData);
      setIsEditing(false);
    } catch (err) {
      setError('Erreur lors de la mise à jour du profil');
    }
  };

  const formatJoinDate = (date) => {
    return new Date(date).toLocaleDateString('fr-FR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  };

  const tabs = [
    { id: 'overview', label: 'Vue d\'ensemble', icon: '📊' },
    { id: 'progress', label: 'Progression', icon: '📈' },
    { id: 'achievements', label: 'Réalisations', icon: '🏆' },
    { id: 'settings', label: 'Paramètres', icon: '⚙️' }
  ];

  if (isLoading) {
    return (
      <div className={styles.profileContainer}>
        <Loading message="Chargement du profil..." />
      </div>
    );
  }

  return (
    <div className={styles.profileContainer}>
      <div className={styles.profileHeader}>
        <div className={styles.avatarSection}>
          <AvatarUpload
            currentAvatar={user?.avatar}
            onAvatarUpdate={handleAvatarUpdate}
            userId={user?.id}
          />
          <div className={styles.userInfo}>
            <h1 className={styles.userName}>{user?.firstName} {user?.lastName}</h1>
            <p className={styles.userEmail}>{user?.email}</p>
            <p className={styles.joinDate}>
              Membre depuis le {formatJoinDate(user?.createdAt)}
            </p>
            <div className={styles.userBadges}>
              {user?.level && (
                <span className={`${styles.badge} ${styles.levelBadge}`}>
                  Niveau {user.level}
                </span>
              )}
              {user?.role && (
                <span className={`${styles.badge} ${styles.roleBadge}`}>
                  {user.role}
                </span>
              )}
            </div>
          </div>
        </div>
        
        <div className={styles.headerActions}>
          <Button
            variant="outline"
            onClick={() => setIsEditing(true)}
            className={styles.editButton}
          >
            ✏️ Modifier le profil
          </Button>
        </div>
      </div>

      {error && (
        <div className={styles.errorMessage}>
          {error}
        </div>
      )}

      <div className={styles.profileTabs}>
        {tabs.map(tab => (
          <button
            key={tab.id}
            className={`${styles.tab} ${activeTab === tab.id ? styles.activeTab : ''}`}
            onClick={() => setActiveTab(tab.id)}
          >
            <span className={styles.tabIcon}>{tab.icon}</span>
            {tab.label}
          </button>
        ))}
      </div>

      <div className={styles.profileContent}>
        {activeTab === 'overview' && (
          <div className={styles.overviewTab}>
            <div className={styles.statsGrid}>
              {userStats && (
                <>
                  <StatsCard
                    title="Cours complétés"
                    value={userStats.completedCourses}
                    icon="📚"
                    trend={userStats.courseTrend}
                  />
                  <StatsCard
                    title="Heures d'étude"
                    value={`${userStats.studyHours}h`}
                    icon="⏱️"
                    trend={userStats.timeTrend}
                  />
                  <StatsCard
                    title="Exercices résolus"
                    value={userStats.exercisesSolved}
                    icon="💡"
                    trend={userStats.exerciseTrend}
                  />
                  <StatsCard
                    title="Streak actuel"
                    value={`${userStats.currentStreak} jours`}
                    icon="🔥"
                    trend={userStats.streakTrend}
                  />
                </>
              )}
            </div>

            <div className={styles.recentActivity}>
              <h3>Activité récente</h3>
              {userStats?.recentActivity?.length > 0 ? (
                <div className={styles.activityList}>
                  {userStats.recentActivity.map((activity, index) => (
                    <div key={index} className={styles.activityItem}>
                      <span className={styles.activityIcon}>{activity.icon}</span>
                      <div className={styles.activityDetails}>
                        <p className={styles.activityText}>{activity.description}</p>
                        <span className={styles.activityDate}>
                          {formatJoinDate(activity.date)}
                        </span>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <p className={styles.noActivity}>Aucune activité récente</p>
              )}
            </div>
          </div>
        )}

        {activeTab === 'progress' && (
          <div className={styles.progressTab}>
            <div className={styles.progressSection}>
              <h3>Progression globale</h3>
              {userStats && (
                <ProgressBar
                  current={userStats.overallProgress}
                  total={100}
                  label="Progression générale"
                  showPercentage
                />
              )}
            </div>

            <div className={styles.skillsProgress}>
              <h3>Compétences</h3>
              {userStats?.skills?.map((skill, index) => (
                <div key={index} className={styles.skillItem}>
                  <div className={styles.skillHeader}>
                    <span className={styles.skillName}>{skill.name}</span>
                    <span className={styles.skillLevel}>Niveau {skill.level}</span>
                  </div>
                  <ProgressBar
                    current={skill.progress}
                    total={100}
                    variant="skill"
                  />
                </div>
              ))}
            </div>
          </div>
        )}

        {activeTab === 'achievements' && (
          <div className={styles.achievementsTab}>
            <h3>Réalisations débloquées</h3>
            <div className={styles.achievementsGrid}>
              {achievements.map((achievement, index) => (
                <Achievement
                  key={index}
                  {...achievement}
                  isUnlocked={achievement.unlocked}
                />
              ))}
            </div>
          </div>
        )}

        {activeTab === 'settings' && (
          <div className={styles.settingsTab}>
            <ProfileSettings
              user={user}
              onUpdate={handleProfileUpdate}
            />
          </div>
        )}
      </div>

      <Modal
        isOpen={isEditing}
        onClose={() => setIsEditing(false)}
        title="Modifier le profil"
        size="large"
      >
        <ProfileSettings
          user={user}
          onUpdate={handleProfileUpdate}
          onCancel={() => setIsEditing(false)}
          isModal={true}
        />
      </Modal>
    </div>
  );
};

export default UserProfile;