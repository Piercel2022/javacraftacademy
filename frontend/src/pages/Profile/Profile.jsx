import React, { useState } from 'react';
import { User, Mail, Calendar, Trophy, BookOpen, Code, Settings, Edit2, Save, X } from 'lucide-react';

const Profile = () => {
  const [isEditing, setIsEditing] = useState(false);
  const [userData, setUserData] = useState({
    name: 'Alex Johnson',
    email: 'alex.johnson@email.com',
    joinDate: 'January 2024',
    bio: 'Passionate Java developer and lifelong learner. Love solving complex problems and building awesome applications.',
    location: 'San Francisco, CA',
    github: 'alexjohnson',
    linkedin: 'alex-johnson-dev'
  });

  const [editData, setEditData] = useState(userData);

  const stats = {
    coursesCompleted: 12,
    totalLessons: 156,
    codeExercises: 89,
    studyStreak: 23,
    totalPoints: 2840,
    rank: 'Advanced Beginner'
  };

  const achievements = [
    { id: 1, title: 'First Steps', description: 'Completed your first lesson', icon: '🎯', earned: true, date: 'Jan 15, 2024' },
    { id: 2, title: 'Code Warrior', description: 'Solved 50 coding exercises', icon: '⚔️', earned: true, date: 'Feb 28, 2024' },
    { id: 3, title: 'Streak Master', description: 'Maintained a 14-day study streak', icon: '🔥', earned: true, date: 'Mar 10, 2024' },
    { id: 4, title: 'Java Explorer', description: 'Completed 10 courses', icon: '🗺️', earned: true, date: 'Apr 5, 2024' },
    { id: 5, title: 'Speed Demon', description: 'Complete a lesson in under 15 minutes', icon: '⚡', earned: false, date: null },
    { id: 6, title: 'Master Coder', description: 'Solve 100 coding challenges', icon: '👑', earned: false, date: null }
  ];

  const recentActivity = [
    { id: 1, type: 'lesson', title: 'Object-Oriented Programming Basics', date: '2 hours ago', points: 25 },
    { id: 2, type: 'exercise', title: 'Array Manipulation Challenge', date: '1 day ago', points: 15 },
    { id: 3, type: 'course', title: 'Java Fundamentals', date: '3 days ago', points: 100 },
    { id: 4, type: 'lesson', title: 'Exception Handling', date: '5 days ago', points: 30 },
    { id: 5, type: 'exercise', title: 'String Processing', date: '1 week ago', points: 20 }
  ];

  // Styles objets
  const styles = {
    container: {
      minHeight: '100vh',
      backgroundColor: '#f9fafb',
      padding: '32px 0'
    },
    maxWidth: {
      maxWidth: '1152px',
      margin: '0 auto',
      padding: '0 16px'
    },
    profileHeader: {
      backgroundColor: 'white',
      borderRadius: '12px',
      boxShadow: '0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05)',
      padding: '32px',
      marginBottom: '32px'
    },
    headerTop: {
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      marginBottom: '24px'
    },
    userInfo: {
      display: 'flex',
      alignItems: 'center',
      gap: '24px'
    },
    avatar: {
      width: '96px',
      height: '96px',
      background: 'linear-gradient(135deg, #3b82f6 0%, #9333ea 100%)',
      borderRadius: '50%',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      color: 'white',
      fontSize: '24px',
      fontWeight: 'bold',
      flexShrink: 0
    },
    userName: {
      fontSize: '30px',
      fontWeight: 'bold',
      color: '#111827',
      margin: 0
    },
    userEmail: {
      color: '#6b7280',
      display: 'flex',
      alignItems: 'center',
      gap: '8px',
      margin: '4px 0'
    },
    userMeta: {
      display: 'flex',
      alignItems: 'center',
      gap: '16px',
      marginTop: '12px',
      fontSize: '14px',
      color: '#6b7280'
    },
    metaItem: {
      display: 'flex',
      alignItems: 'center',
      gap: '4px'
    },
    editButtons: {
      display: 'flex',
      gap: '8px'
    },
    button: {
      display: 'flex',
      alignItems: 'center',
      gap: '8px',
      padding: '8px 16px',
      borderRadius: '8px',
      border: 'none',
      cursor: 'pointer',
      fontWeight: '500',
      transition: 'background-color 0.2s ease'
    },
    editButton: {
      backgroundColor: '#2563eb',
      color: 'white'
    },
    saveButton: {
      backgroundColor: '#16a34a',
      color: 'white'
    },
    cancelButton: {
      backgroundColor: '#4b5563',
      color: 'white'
    },
    input: {
      fontSize: '30px',
      fontWeight: 'bold',
      borderBottom: '2px solid #d1d5db',
      outline: 'none',
      backgroundColor: 'transparent',
      padding: '4px 0'
    },
    emailInput: {
      color: '#6b7280',
      borderBottom: '2px solid #d1d5db',
      outline: 'none',
      backgroundColor: 'transparent',
      padding: '4px 0'
    },
    bioSection: {
      borderTop: '1px solid #e5e7eb',
      paddingTop: '24px'
    },
    bioTitle: {
      fontSize: '18px',
      fontWeight: '600',
      marginBottom: '12px'
    },
    bioText: {
      color: '#374151'
    },
    bioTextarea: {
      width: '100%',
      padding: '12px',
      border: '1px solid #d1d5db',
      borderRadius: '8px',
      outline: 'none',
      resize: 'vertical',
      fontFamily: 'inherit'
    },
    statsGrid: {
      display: 'grid',
      gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
      gap: '24px',
      marginBottom: '32px'
    },
    statCard: {
      backgroundColor: 'white',
      borderRadius: '8px',
      boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06)',
      padding: '24px',
      textAlign: 'center'
    },
    statNumber: {
      fontSize: '30px',
      fontWeight: 'bold',
      marginBottom: '8px'
    },
    statLabel: {
      color: '#6b7280'
    },
    mainGrid: {
      display: 'grid',
      gridTemplateColumns: 'repeat(auto-fit, minmax(500px, 1fr))',
      gap: '32px'
    },
    sectionCard: {
      backgroundColor: 'white',
      borderRadius: '12px',
      boxShadow: '0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05)',
      padding: '24px'
    },
    sectionTitle: {
      fontSize: '24px',
      fontWeight: 'bold',
      marginBottom: '24px',
      display: 'flex',
      alignItems: 'center',
      gap: '8px'
    },
    achievementsList: {
      display: 'flex',
      flexDirection: 'column',
      gap: '16px'
    },
    achievementItem: {
      display: 'flex',
      alignItems: 'center',
      gap: '16px',
      padding: '16px',
      borderRadius: '8px',
      border: '2px solid'
    },
    achievementEarned: {
      borderColor: '#fef3c7',
      backgroundColor: '#fffbeb'
    },
    achievementNotEarned: {
      borderColor: '#e5e7eb',
      backgroundColor: '#f9fafb',
      opacity: 0.6
    },
    achievementIcon: {
      fontSize: '24px'
    },
    achievementContent: {
      flex: 1
    },
    achievementTitle: {
      fontWeight: '600',
      color: '#111827',
      margin: 0
    },
    achievementDescription: {
      fontSize: '14px',
      color: '#6b7280',
      margin: '4px 0'
    },
    achievementDate: {
      fontSize: '12px',
      color: '#6b7280',
      marginTop: '4px'
    },
    achievementCheck: {
      color: '#16a34a',
      fontWeight: 'bold',
      fontSize: '18px'
    },
    activityList: {
      display: 'flex',
      flexDirection: 'column',
      gap: '16px'
    },
    activityItem: {
      display: 'flex',
      alignItems: 'center',
      gap: '16px',
      padding: '12px',
      borderRadius: '8px',
      transition: 'background-color 0.2s ease',
      cursor: 'pointer'
    },
    activityIcon: {
      padding: '8px',
      backgroundColor: '#f3f4f6',
      borderRadius: '8px'
    },
    activityContent: {
      flex: 1
    },
    activityTitle: {
      fontWeight: '500',
      color: '#111827',
      margin: 0
    },
    activityDate: {
      fontSize: '14px',
      color: '#6b7280',
      margin: '4px 0 0 0'
    },
    activityPoints: {
      fontSize: '14px',
      fontWeight: '600',
      color: '#16a34a'
    },
    progressSection: {
      backgroundColor: 'white',
      borderRadius: '12px',
      boxShadow: '0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05)',
      padding: '24px',
      marginTop: '32px'
    },
    progressGrid: {
      display: 'grid',
      gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
      gap: '24px'
    },
    progressItem: {
      textAlign: 'center'
    },
    progressCircle: {
      position: 'relative',
      width: '80px',
      height: '80px',
      margin: '0 auto 16px'
    },
    progressCircleBase: {
      width: '80px',
      height: '80px',
      borderRadius: '50%',
      border: '8px solid #e5e7eb'
    },
    progressCircleOverlay: {
      position: 'absolute',
      top: 0,
      left: 0,
      width: '80px',
      height: '80px',
      borderRadius: '50%',
      border: '8px solid',
      borderTopColor: 'transparent'
    },
    progressText: {
      position: 'absolute',
      inset: 0,
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      fontWeight: 'bold'
    },
    progressTitle: {
      fontWeight: '600',
      margin: '0 0 4px 0'
    },
    progressSubtitle: {
      fontSize: '14px',
      color: '#6b7280',
      margin: 0
    }
  };

  const handleEdit = () => {
    setIsEditing(true);
    setEditData(userData);
  };

  const handleSave = () => {
    setUserData(editData);
    setIsEditing(false);
  };

  const handleCancel = () => {
    setEditData(userData);
    setIsEditing(false);
  };

  const getActivityIcon = (type) => {
    const iconStyle = { size: 16 };
    switch (type) {
      case 'lesson': return <BookOpen size={16} style={{ color: '#3b82f6' }} />;
      case 'exercise': return <Code size={16} style={{ color: '#16a34a' }} />;
      case 'course': return <Trophy size={16} style={{ color: '#9333ea' }} />;
      default: return <BookOpen size={16} style={{ color: '#6b7280' }} />;
    }
  };

  return (
    <div style={styles.container}>
      <div style={styles.maxWidth}>
        {/* Profile Header */}
        <div style={styles.profileHeader}>
          <div style={styles.headerTop}>
            <div style={styles.userInfo}>
              <div style={styles.avatar}>
                {userData.name.split(' ').map(n => n[0]).join('')}
              </div>
              <div>
                {isEditing ? (
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                    <input
                      type="text"
                      value={editData.name}
                      onChange={(e) => setEditData({...editData, name: e.target.value})}
                      style={{
                        ...styles.input,
                        borderBottomColor: '#3b82f6'
                      }}
                      onFocus={(e) => e.target.style.borderBottomColor = '#3b82f6'}
                      onBlur={(e) => e.target.style.borderBottomColor = '#d1d5db'}
                    />
                    <input
                      type="email"
                      value={editData.email}
                      onChange={(e) => setEditData({...editData, email: e.target.value})}
                      style={{
                        ...styles.emailInput,
                        borderBottomColor: '#3b82f6'
                      }}
                      onFocus={(e) => e.target.style.borderBottomColor = '#3b82f6'}
                      onBlur={(e) => e.target.style.borderBottomColor = '#d1d5db'}
                    />
                  </div>
                ) : (
                  <div>
                    <h1 style={styles.userName}>{userData.name}</h1>
                    <p style={styles.userEmail}>
                      <Mail size={16} />
                      {userData.email}
                    </p>
                  </div>
                )}
                <div style={styles.userMeta}>
                  <span style={styles.metaItem}>
                    <Calendar size={14} />
                    Joined {userData.joinDate}
                  </span>
                  <span style={styles.metaItem}>
                    <Trophy size={14} />
                    {stats.rank}
                  </span>
                </div>
              </div>
            </div>
            
            <div style={styles.editButtons}>
              {isEditing ? (
                <>
                  <button
                    onClick={handleSave}
                    style={{
                      ...styles.button,
                      ...styles.saveButton
                    }}
                    onMouseEnter={(e) => e.target.style.backgroundColor = '#059669'}
                    onMouseLeave={(e) => e.target.style.backgroundColor = '#16a34a'}
                  >
                    <Save size={16} />
                    Save
                  </button>
                  <button
                    onClick={handleCancel}
                    style={{
                      ...styles.button,
                      ...styles.cancelButton
                    }}
                    onMouseEnter={(e) => e.target.style.backgroundColor = '#374151'}
                    onMouseLeave={(e) => e.target.style.backgroundColor = '#4b5563'}
                  >
                    <X size={16} />
                    Cancel
                  </button>
                </>
              ) : (
                <button
                  onClick={handleEdit}
                  style={{
                    ...styles.button,
                    ...styles.editButton
                  }}
                  onMouseEnter={(e) => e.target.style.backgroundColor = '#1d4ed8'}
                  onMouseLeave={(e) => e.target.style.backgroundColor = '#2563eb'}
                >
                  <Edit2 size={16} />
                  Edit Profile
                </button>
              )}
            </div>
          </div>

          {/* Bio Section */}
          <div style={styles.bioSection}>
            <h3 style={styles.bioTitle}>About</h3>
            {isEditing ? (
              <textarea
                value={editData.bio}
                onChange={(e) => setEditData({...editData, bio: e.target.value})}
                style={{
                  ...styles.bioTextarea,
                  borderColor: '#3b82f6',
                  boxShadow: '0 0 0 3px rgba(59, 130, 246, 0.1)'
                }}
                rows={3}
                onFocus={(e) => {
                  e.target.style.borderColor = '#3b82f6';
                  e.target.style.boxShadow = '0 0 0 3px rgba(59, 130, 246, 0.1)';
                }}
                onBlur={(e) => {
                  e.target.style.borderColor = '#d1d5db';
                  e.target.style.boxShadow = 'none';
                }}
              />
            ) : (
              <p style={styles.bioText}>{userData.bio}</p>
            )}
          </div>
        </div>

        {/* Stats Grid */}
        <div style={styles.statsGrid}>
          <div style={styles.statCard}>
            <div style={{ ...styles.statNumber, color: '#2563eb' }}>{stats.coursesCompleted}</div>
            <div style={styles.statLabel}>Courses Completed</div>
          </div>
          <div style={styles.statCard}>
            <div style={{ ...styles.statNumber, color: '#16a34a' }}>{stats.totalLessons}</div>
            <div style={styles.statLabel}>Lessons Finished</div>
          </div>
          <div style={styles.statCard}>
            <div style={{ ...styles.statNumber, color: '#9333ea' }}>{stats.codeExercises}</div>
            <div style={styles.statLabel}>Code Exercises</div>
          </div>
          <div style={styles.statCard}>
            <div style={{ ...styles.statNumber, color: '#ea580c' }}>{stats.studyStreak}</div>
            <div style={styles.statLabel}>Day Streak</div>
          </div>
        </div>

        <div style={styles.mainGrid}>
          {/* Achievements */}
          <div style={styles.sectionCard}>
            <h2 style={styles.sectionTitle}>
              <Trophy style={{ color: '#eab308' }} />
              Achievements
            </h2>
            <div style={styles.achievementsList}>
              {achievements.map((achievement) => (
                <div
                  key={achievement.id}
                  style={{
                    ...styles.achievementItem,
                    ...(achievement.earned ? styles.achievementEarned : styles.achievementNotEarned)
                  }}
                >
                  <div style={styles.achievementIcon}>{achievement.icon}</div>
                  <div style={styles.achievementContent}>
                    <h3 style={styles.achievementTitle}>{achievement.title}</h3>
                    <p style={styles.achievementDescription}>{achievement.description}</p>
                    {achievement.earned && achievement.date && (
                      <p style={styles.achievementDate}>Earned on {achievement.date}</p>
                    )}
                  </div>
                  {achievement.earned && (
                    <div style={styles.achievementCheck}>✓</div>
                  )}
                </div>
              ))}
            </div>
          </div>

          {/* Recent Activity */}
          <div style={styles.sectionCard}>
            <h2 style={styles.sectionTitle}>
              <BookOpen style={{ color: '#3b82f6' }} />
              Recent Activity
            </h2>
            <div style={styles.activityList}>
              {recentActivity.map((activity) => (
                <div 
                  key={activity.id} 
                  style={styles.activityItem}
                  onMouseEnter={(e) => e.target.style.backgroundColor = '#f9fafb'}
                  onMouseLeave={(e) => e.target.style.backgroundColor = 'transparent'}
                >
                  <div style={styles.activityIcon}>
                    {getActivityIcon(activity.type)}
                  </div>
                  <div style={styles.activityContent}>
                    <h3 style={styles.activityTitle}>{activity.title}</h3>
                    <p style={styles.activityDate}>{activity.date}</p>
                  </div>
                  <div style={styles.activityPoints}>
                    +{activity.points} pts
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Progress Overview */}
        <div style={styles.progressSection}>
          <h2 style={styles.sectionTitle}>Learning Progress</h2>
          <div style={styles.progressGrid}>
            <div style={styles.progressItem}>
              <div style={styles.progressCircle}>
                <div style={styles.progressCircleBase}></div>
                <div style={{
                  ...styles.progressCircleOverlay,
                  borderColor: '#3b82f6',
                  transform: 'rotate(270deg)',
                  animation: 'pulse 2s infinite'
                }}></div>
                <div style={{
                  ...styles.progressText,
                  color: '#2563eb'
                }}>
                  75%
                </div>
              </div>
              <h3 style={styles.progressTitle}>Java Fundamentals</h3>
              <p style={styles.progressSubtitle}>15/20 lessons</p>
            </div>
            
            <div style={styles.progressItem}>
              <div style={styles.progressCircle}>
                <div style={styles.progressCircleBase}></div>
                <div style={{
                  ...styles.progressCircleOverlay,
                  borderColor: '#16a34a',
                  transform: 'rotate(180deg)'
                }}></div>
                <div style={{
                  ...styles.progressText,
                  color: '#16a34a'
                }}>
                  50%
                </div>
              </div>
              <h3 style={styles.progressTitle}>OOP Concepts</h3>
              <p style={styles.progressSubtitle}>8/16 lessons</p>
            </div>
            
            <div style={styles.progressItem}>
              <div style={styles.progressCircle}>
                <div style={styles.progressCircleBase}></div>
                <div style={{
                  ...styles.progressCircleOverlay,
                  borderColor: '#9333ea',
                  transform: 'rotate(90deg)'
                }}></div>
                <div style={{
                  ...styles.progressText,
                  color: '#9333ea'
                }}>
                  25%
                </div>
              </div>
              <h3 style={styles.progressTitle}>Data Structures</h3>
              <p style={styles.progressSubtitle}>3/12 lessons</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Profile;