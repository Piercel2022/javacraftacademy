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
    switch (type) {
      case 'lesson': return <BookOpen size={16} className="text-blue-500" />;
      case 'exercise': return <Code size={16} className="text-green-500" />;
      case 'course': return <Trophy size={16} className="text-purple-500" />;
      default: return <BookOpen size={16} className="text-gray-500" />;
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-6xl mx-auto px-4">
        {/* Profile Header */}
        <div className="bg-white rounded-xl shadow-lg p-8 mb-8">
          <div className="flex items-center justify-between mb-6">
            <div className="flex items-center gap-6">
              <div className="w-24 h-24 bg-gradient-to-br from-blue-500 to-purple-600 rounded-full flex items-center justify-center text-white text-3xl font-bold">
                {userData.name.split(' ').map(n => n[0]).join('')}
              </div>
              <div>
                {isEditing ? (
                  <div className="space-y-2">
                    <input
                      type="text"
                      value={editData.name}
                      onChange={(e) => setEditData({...editData, name: e.target.value})}
                      className="text-2xl font-bold border-b-2 border-gray-300 focus:border-blue-500 outline-none bg-transparent"
                    />
                    <input
                      type="email"
                      value={editData.email}
                      onChange={(e) => setEditData({...editData, email: e.target.value})}
                      className="text-gray-600 border-b-2 border-gray-300 focus:border-blue-500 outline-none bg-transparent"
                    />
                  </div>
                ) : (
                  <div>
                    <h1 className="text-3xl font-bold text-gray-900">{userData.name}</h1>
                    <p className="text-gray-600 flex items-center gap-2">
                      <Mail size={16} />
                      {userData.email}
                    </p>
                  </div>
                )}
                <div className="flex items-center gap-4 mt-3 text-sm text-gray-500">
                  <span className="flex items-center gap-1">
                    <Calendar size={14} />
                    Joined {userData.joinDate}
                  </span>
                  <span className="flex items-center gap-1">
                    <Trophy size={14} />
                    {stats.rank}
                  </span>
                </div>
              </div>
            </div>
            
            <div className="flex gap-2">
              {isEditing ? (
                <>
                  <button
                    onClick={handleSave}
                    className="flex items-center gap-2 bg-green-600 text-white px-4 py-2 rounded-lg hover:bg-green-700 transition-colors"
                  >
                    <Save size={16} />
                    Save
                  </button>
                  <button
                    onClick={handleCancel}
                    className="flex items-center gap-2 bg-gray-600 text-white px-4 py-2 rounded-lg hover:bg-gray-700 transition-colors"
                  >
                    <X size={16} />
                    Cancel
                  </button>
                </>
              ) : (
                <button
                  onClick={handleEdit}
                  className="flex items-center gap-2 bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors"
                >
                  <Edit2 size={16} />
                  Edit Profile
                </button>
              )}
            </div>
          </div>

          {/* Bio Section */}
          <div className="border-t pt-6">
            <h3 className="text-lg font-semibold mb-3">About</h3>
            {isEditing ? (
              <textarea
                value={editData.bio}
                onChange={(e) => setEditData({...editData, bio: e.target.value})}
                className="w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                rows={3}
              />
            ) : (
              <p className="text-gray-700">{userData.bio}</p>
            )}
          </div>
        </div>

        {/* Stats Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <div className="bg-white rounded-lg shadow-md p-6 text-center">
            <div className="text-3xl font-bold text-blue-600">{stats.coursesCompleted}</div>
            <div className="text-gray-600">Courses Completed</div>
          </div>
          <div className="bg-white rounded-lg shadow-md p-6 text-center">
            <div className="text-3xl font-bold text-green-600">{stats.totalLessons}</div>
            <div className="text-gray-600">Lessons Finished</div>
          </div>
          <div className="bg-white rounded-lg shadow-md p-6 text-center">
            <div className="text-3xl font-bold text-purple-600">{stats.codeExercises}</div>
            <div className="text-gray-600">Code Exercises</div>
          </div>
          <div className="bg-white rounded-lg shadow-md p-6 text-center">
            <div className="text-3xl font-bold text-orange-600">{stats.studyStreak}</div>
            <div className="text-gray-600">Day Streak</div>
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          {/* Achievements */}
          <div className="bg-white rounded-xl shadow-lg p-6">
            <h2 className="text-2xl font-bold mb-6 flex items-center gap-2">
              <Trophy className="text-yellow-500" />
              Achievements
            </h2>
            <div className="space-y-4">
              {achievements.map((achievement) => (
                <div
                  key={achievement.id}
                  className={`flex items-center gap-4 p-4 rounded-lg border-2 ${
                    achievement.earned
                      ? 'border-yellow-200 bg-yellow-50'
                      : 'border-gray-200 bg-gray-50 opacity-60'
                  }`}
                >
                  <div className="text-2xl">{achievement.icon}</div>
                  <div className="flex-1">
                    <h3 className="font-semibold text-gray-900">{achievement.title}</h3>
                    <p className="text-sm text-gray-600">{achievement.description}</p>
                    {achievement.earned && achievement.date && (
                      <p className="text-xs text-gray-500 mt-1">Earned on {achievement.date}</p>
                    )}
                  </div>
                  {achievement.earned && (
                    <div className="text-green-500 font-bold">✓</div>
                  )}
                </div>
              ))}
            </div>
          </div>

          {/* Recent Activity */}
          <div className="bg-white rounded-xl shadow-lg p-6">
            <h2 className="text-2xl font-bold mb-6 flex items-center gap-2">
              <BookOpen className="text-blue-500" />
              Recent Activity
            </h2>
            <div className="space-y-4">
              {recentActivity.map((activity) => (
                <div key={activity.id} className="flex items-center gap-4 p-3 hover:bg-gray-50 rounded-lg transition-colors">
                  <div className="p-2 bg-gray-100 rounded-lg">
                    {getActivityIcon(activity.type)}
                  </div>
                  <div className="flex-1">
                    <h3 className="font-medium text-gray-900">{activity.title}</h3>
                    <p className="text-sm text-gray-600">{activity.date}</p>
                  </div>
                  <div className="text-sm font-semibold text-green-600">
                    +{activity.points} pts
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Progress Overview */}
        <div className="bg-white rounded-xl shadow-lg p-6 mt-8">
          <h2 className="text-2xl font-bold mb-6">Learning Progress</h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div className="text-center">
              <div className="relative w-20 h-20 mx-auto mb-4">
                <div className="w-20 h-20 rounded-full border-8 border-gray-200"></div>
                <div className="absolute top-0 left-0 w-20 h-20 rounded-full border-8 border-blue-500 border-t-transparent animate-pulse"></div>
                <div className="absolute inset-0 flex items-center justify-center font-bold text-blue-600">
                  75%
                </div>
              </div>
              <h3 className="font-semibold">Java Fundamentals</h3>
              <p className="text-sm text-gray-600">15/20 lessons</p>
            </div>
            
            <div className="text-center">
              <div className="relative w-20 h-20 mx-auto mb-4">
                <div className="w-20 h-20 rounded-full border-8 border-gray-200"></div>
                <div className="absolute top-0 left-0 w-20 h-20 rounded-full border-8 border-green-500 border-t-transparent" style={{transform: 'rotate(180deg)'}}></div>
                <div className="absolute inset-0 flex items-center justify-center font-bold text-green-600">
                  50%
                </div>
              </div>
              <h3 className="font-semibold">OOP Concepts</h3>
              <p className="text-sm text-gray-600">8/16 lessons</p>
            </div>
            
            <div className="text-center">
              <div className="relative w-20 h-20 mx-auto mb-4">
                <div className="w-20 h-20 rounded-full border-8 border-gray-200"></div>
                <div className="absolute top-0 left-0 w-20 h-20 rounded-full border-8 border-purple-500 border-t-transparent" style={{transform: 'rotate(90deg)'}}></div>
                <div className="absolute inset-0 flex items-center justify-center font-bold text-purple-600">
                  25%
                </div>
              </div>
              <h3 className="font-semibold">Data Structures</h3>
              <p className="text-sm text-gray-600">3/12 lessons</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Profile;
