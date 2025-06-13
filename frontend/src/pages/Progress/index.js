import React, { useState } from 'react';
import { BookOpen, CheckCircle, Clock, Trophy, TrendingUp, Calendar, Target, Award } from 'lucide-react';

const Progress = () => {
  const [selectedTimeframe, setSelectedTimeframe] = useState('week');
  
  const courses = [
    {
      id: 1,
      title: 'Java Fundamentals',
      description: 'Master the basics of Java programming',
      progress: 85,
      totalLessons: 20,
      completedLessons: 17,
      timeSpent: '12h 30m',
      status: 'in-progress',
      difficulty: 'Beginner',
      nextLesson: 'Arrays and Collections'
    },
    {
      id: 2,
      title: 'Object-Oriented Programming',
      description: 'Learn OOP concepts and design patterns',
      progress: 60,
      totalLessons: 16,
      completedLessons: 10,
      timeSpent: '8h 45m',
      status: 'in-progress',
      difficulty: 'Intermediate',
      nextLesson: 'Inheritance and Polymorphism'
    },
    {
      id: 3,
      title: 'Data Structures & Algorithms',
      description: 'Understand core data structures and algorithms',
      progress: 30,
      totalLessons: 24,
      completedLessons: 7,
      timeSpent: '5h 15m',
      status: 'in-progress',
      difficulty: 'Advanced',
      nextLesson: 'Binary Trees'
    },
    {
      id: 4,
      title: 'Java Web Development',
      description: 'Build web applications with Java',
      progress: 100,
      totalLessons: 18,
      completedLessons: 18,
      timeSpent: '15h 20m',
      status: 'completed',
      difficulty: 'Intermediate',
      nextLesson: null
    }
  ];

  const weeklyStats = {
    thisWeek: {
      lessonsCompleted: 8,
      timeSpent: 420, // minutes
      streakDays: 5,
      exercisesSolved: 12
    },
    lastWeek: {
      lessonsCompleted: 6,
      timeSpent: 360,
      streakDays: 4,
      exercisesSolved: 9
    }
  };

  const monthlyGoals = [
    {
      id: 1,
      title: 'Complete Java Fundamentals',
      target: 20,
      current: 17,
      type: 'lessons',
      deadline: '2024-07-15',
      status: 'on-track'
    },
    {
      id: 2,
      title: 'Solve 50 Coding Exercises',
      target: 50,
      current: 38,
      type: 'exercises',
      deadline: '2024-07-31',
      status: 'on-track'
    },
    {
      id: 3,
      title: 'Study 20 Hours This Month',
      target: 20,
      current: 15.5,
      type: 'hours',
      deadline: '2024-07-31',
      status: 'behind'
    }
  ];

  const learningPath = [
    { id: 1, title: 'Variables & Data Types', completed: true, current: false },
    { id: 2, title: 'Control Structures', completed: true, current: false },
    { id: 3, title: 'Methods & Functions', completed: true, current: false },
    { id: 4, title: 'Arrays & Collections', completed: false, current: true },
    { id: 5, title: 'Object-Oriented Basics', completed: false, current: false },
    { id: 6, title: 'Inheritance & Polymorphism', completed: false, current: false },
    { id: 7, title: 'Exception Handling', completed: false, current: false },
    { id: 8, title: 'File I/O Operations', completed: false, current: false }
  ];

  const achievements = [
    { id: 1, title: 'First Steps', description: 'Completed your first lesson', icon: '🎯', earned: true },
    { id: 2, title: 'Speed Learner', description: 'Completed 5 lessons in one day', icon: '⚡', earned: true },
    { id: 3, title: 'Consistent Learner', description: 'Maintained a 7-day streak', icon: '🔥', earned: false },
    { id: 4, title: 'Java Master', description: 'Completed all Java courses', icon: '👑', earned: false }
  ];

  const getProgressColor = (progress) => {
    if (progress >= 80) return 'bg-green-500';
    if (progress >= 60) return 'bg-blue-500';
    if (progress >= 40) return 'bg-yellow-500';
    return 'bg-red-500';
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'completed': return 'text-green-600 bg-green-100';
      case 'in-progress': return 'text-blue-600 bg-blue-100';
      case 'not-started': return 'text-gray-600 bg-gray-100';
      default: return 'text-gray-600 bg-gray-100';
    }
  };

  const formatTime = (minutes) => {
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    return `${hours}h ${mins}m`;
  };

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-7xl mx-auto px-4">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">Learning Progress</h1>
          <p className="text-gray-600">Track your journey and achievements</p>
        </div>

        {/* Overview Stats */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <div className="bg-white rounded-lg shadow-md p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600">This Week</p>
                <p className="text-2xl font-bold text-blue-600">{weeklyStats.thisWeek.lessonsCompleted}</p>
                <p className="text-sm text-gray-500">Lessons Completed</p>
              </div>
              <BookOpen className="h-8 w-8 text-blue-500" />
            </div>
            <div className="mt-2">
              <span className="text-sm text-green-600">
                +{weeklyStats.thisWeek.lessonsCompleted - weeklyStats.lastWeek.lessonsCompleted} from last week
              </span>
            </div>
          </div>

          <div className="bg-white rounded-lg shadow-md p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600">Study Time</p>
                <p className="text-2xl font-bold text-green-600">{formatTime(weeklyStats.thisWeek.timeSpent)}</p>
                <p className="text-sm text-gray-500">This Week</p>
              </div>
              <Clock className="h-8 w-8 text-green-500" />
            </div>
            <div className="mt-2">
              <span className="text-sm text-green-600">
                +{formatTime(weeklyStats.thisWeek.timeSpent - weeklyStats.lastWeek.timeSpent)} from last week
              </span>
            </div>
          </div>

          <div className="bg-white rounded-lg shadow-md p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600">Current Streak</p>
                <p className="text-2xl font-bold text-orange-600">{weeklyStats.thisWeek.streakDays}</p>
                <p className="text-sm text-gray-500">Days</p>
              </div>
              <TrendingUp className="h-8 w-8 text-orange-500" />
            </div>
            <div className="mt-2">
              <span className="text-sm text-green-600">
                +{weeklyStats.thisWeek.streakDays - weeklyStats.lastWeek.streakDays} from last week
              </span>
            </div>
          </div>

          <div className="bg-white rounded-lg shadow-md p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600">Exercises Solved</p>
                <p className="text-2xl font-bold text-purple-600">{weeklyStats.thisWeek.exercisesSolved}</p>
                <p className="text-sm text-gray-500">This Week</p>
              </div>
              <Target className="h-8 w-8 text-purple-500" />
            </div>
            <div className="mt-2">
              <span className="text-sm text-green-600">
                +{weeklyStats.thisWeek.exercisesSolved - weeklyStats.lastWeek.exercisesSolved} from last week
              </span>
            </div>
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Course Progress */}
          <div className="lg:col-span-2">
            <div className="bg-white rounded-xl shadow-lg p-6 mb-8">
              <h2 className="text-2xl font-bold mb-6 flex items-center gap-2">
                <BookOpen className="text-blue-500" />
                Course Progress
              </h2>
              <div className="space-y-6">
                {courses.map((course) => (
                  <div key={course.id} className="border border-gray-200 rounded-lg p-6 hover:shadow-md transition-shadow">
                    <div className="flex items-center justify-between mb-4">
                      <div className="flex-1">
                        <h3 className="text-lg font-semibold text-gray-900">{course.title}</h3>
                        <p className="text-gray-600">{course.description}</p>
                        <div className="flex items-center gap-4 mt-2 text-sm text-gray-500">
                          <span className={`px-2 py-1 rounded-full text-xs font-medium ${getStatusColor(course.status)}`}>
                            {course.status.replace('-', ' ').toUpperCase()}
                          </span>
                          <span>{course.difficulty}</span>
                          <span>{course.timeSpent} spent</span>
                        </div>
                      </div>
                      <div className="text-right">
                        <div className="text-2xl font-bold text-gray-900">{course.progress}%</div>
                        <div className="text-sm text-gray-600">
                          {course.completedLessons}/{course.totalLessons} lessons
                        </div>
                      </div>
                    </div>
                    
                    <div className="mb-4">
                      <div className="flex justify-between text-sm text-gray-600 mb-1">
                        <span>Progress</span>
                        <span>{course.progress}% Complete</span>
                      </div>
                      <div className="w-full bg-gray-200 rounded-full h-2">
                        <div
                          className={`h-2 rounded-full ${getProgressColor(course.progress)} transition-all duration-500`}
                          style={{ width: `${course.progress}%` }}
                        ></div>
                      </div>
                    </div>

                    {course.nextLesson && (
                      <div className="flex items-center justify-between">
                        <span className="text-sm text-gray-600">Next: {course.nextLesson}</span>
                        <button className="text-blue-600 hover:text-blue-700 font-medium text-sm">
                          Continue →
                        </button>
                      </div>
                    )}

                    {course.status === 'completed' && (
                      <div className="flex items-center text-green-600 text-sm font-medium">
                        <CheckCircle size={16} className="mr-1" />
                        Course Completed!
                      </div>
                    )}
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* Sidebar */}
          <div className="space-y-8">
            {/* Monthly Goals */}
            <div className="bg-white rounded-xl shadow-lg p-6">
              <h2 className="text-xl font-bold mb-6 flex items-center gap-2">
                <Target className="text-green-500" />
                Monthly Goals
              </h2>
              <div className="space-y-4">
                {monthlyGoals.map((goal) => (
                  <div key={goal.id} className="border border-gray-200 rounded-lg p-4">
                    <div className="flex items-center justify-between mb-2">
                      <h3 className="font-medium text-gray-900">{goal.title}</h3>
                      <span className={`px-2 py-1 rounded-full text-xs font-medium ${
                        goal.status === 'on-track' ? 'text-green-600 bg-green-100' : 'text-orange-600 bg-orange-100'
                      }`}>
                        {goal.status.replace('-', ' ')}
                      </span>
                    </div>
                    <div className="mb-2">
                      <div className="flex justify-between text-sm text-gray-600 mb-1">
                        <span>{goal.current} / {goal.target} {goal.type}</span>
                        <span>{Math.round((goal.current / goal.target) * 100)}%</span>
                      </div>
                      <div className="w-full bg-gray-200 rounded-full h-2">
                        <div
                          className={`h-2 rounded-full ${
                            goal.status === 'on-track' ? 'bg-green-500' : 'bg-orange-500'
                          } transition-all duration-500`}
                          style={{ width: `${Math.min((goal.current / goal.target) * 100, 100)}%` }}
                        ></div>
                      </div>
                    </div>
                    <div className="text-xs text-gray-500">
                      Due: {new Date(goal.deadline).toLocaleDateString()}
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* Learning Path */}
            <div className="bg-white rounded-xl shadow-lg p-6">
              <h2 className="text-xl font-bold mb-6 flex items-center gap-2">
                <Award className="text-purple-500" />
                Learning Path
              </h2>
              <div className="space-y-3">
                {learningPath.map((step, index) => (
                  <div key={step.id} className="flex items-center gap-3">
                    <div className={`w-6 h-6 rounded-full flex items-center justify-center text-xs font-medium ${
                      step.completed ? 'bg-green-500 text-white' :
                      step.current ? 'bg-blue-500 text-white' :
                      'bg-gray-200 text-gray-600'
                    }`}>
                      {step.completed ? '✓' : index + 1}
                    </div>
                    <div className={`flex-1 ${
                      step.current ? 'font-medium text-blue-600' :
                      step.completed ? 'text-gray-600' :
                      'text-gray-400'
                    }`}>
                      {step.title}
                    </div>
                    {step.current && (
                      <span className="text-xs bg-blue-100 text-blue-600 px-2 py-1 rounded-full">
                        Current
                      </span>
                    )}
                  </div>
                ))}
              </div>
            </div>

            {/* Achievements */}
            <div className="bg-white rounded-xl shadow-lg p-6">
              <h2 className="text-xl font-bold mb-6 flex items-center gap-2">
                <Trophy className="text-yellow-500" />
                Achievements
              </h2>
              <div className="space-y-3">
                {achievements.map((achievement) => (
                  <div key={achievement.id} className={`flex items-center gap-3 p-3 rounded-lg transition-all ${
                    achievement.earned ? 'bg-yellow-50 border-yellow-200 border' : 'bg-gray-50 opacity-60'
                  }`}>
                    <div className="text-2xl">
                      {achievement.earned ? achievement.icon : '🔒'}
                    </div>
                    <div className="flex-1">
                      <h3 className={`font-medium ${achievement.earned ? 'text-gray-900' : 'text-gray-500'}`}>
                        {achievement.title}
                      </h3>
                      <p className={`text-sm ${achievement.earned ? 'text-gray-600' : 'text-gray-400'}`}>
                        {achievement.description}
                      </p>
                    </div>
                    {achievement.earned && (
                      <CheckCircle className="w-5 h-5 text-green-500" />
                    )}
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Progress;