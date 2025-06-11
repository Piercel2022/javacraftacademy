// Absolute path: frontend/src/pages/Courses/Courses.jsx
import React, { useState, useEffect } from 'react';
import CourseCard from '../../components/course/CourseCard';
import Loading from '../../components/common/Loading';
import styles from './Courses.module.css';

const Courses = () => {
  const [courses, setCourses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('all');

  useEffect(() => {
    // Mock data - replace with actual API call
    setTimeout(() => {
      setCourses([
        {
          id: 1,
          title: 'Java Fundamentals',
          description: 'Learn the basics of Java programming',
          level: 'beginner',
          duration: '4 weeks',
          image: '/api/placeholder/300/200',
          progress: 0
        },
        {
          id: 2,
          title: 'Object-Oriented Programming',
          description: 'Master OOP concepts in Java',
          level: 'intermediate',
          duration: '6 weeks',
          image: '/api/placeholder/300/200',
          progress: 45
        },
        {
          id: 3,
          title: 'Advanced Java Development',
          description: 'Advanced topics and frameworks',
          level: 'advanced',
          duration: '8 weeks',
          image: '/api/placeholder/300/200',
          progress: 0
        }
      ]);
      setLoading(false);
    }, 1000);
  }, []);

  const filteredCourses = courses.filter(course => 
    filter === 'all' || course.level === filter
  );

  if (loading) return <Loading />;

  return (
    <div className={styles.coursesContainer}>
      <div className={styles.header}>
        <h1>Available Courses</h1>
        <div className={styles.filters}>
          <button 
            className={filter === 'all' ? styles.active : ''}
            onClick={() => setFilter('all')}
          >
            All Courses
          </button>
          <button 
            className={filter === 'beginner' ? styles.active : ''}
            onClick={() => setFilter('beginner')}
          >
            Beginner
          </button>
          <button 
            className={filter === 'intermediate' ? styles.active : ''}
            onClick={() => setFilter('intermediate')}
          >
            Intermediate
          </button>
          <button 
            className={filter === 'advanced' ? styles.active : ''}
            onClick={() => setFilter('advanced')}
          >
            Advanced
          </button>
        </div>
      </div>
      
      <div className={styles.coursesGrid}>
        {filteredCourses.map(course => (
          <CourseCard key={course.id} course={course} />
        ))}
      </div>
    </div>
  );
};

export default Courses;