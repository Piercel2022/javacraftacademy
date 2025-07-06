import React, { useState, useEffect } from 'react';
import CourseCard from '../../components/course/CourseCard';
import Loading from '../../components/common/Loading';
import styles from './Courses.module.css';

const Courses = () => {
  const [courses, setCourses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [filter, setFilter] = useState('all');

  const fetchCourses = async () => {
    try {
      setLoading(true);
      setError(null);
      
      const response = await fetch('/api/courses', {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          // Add authorization header if needed
          // 'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = await response.json();
      setCourses(data);
    } catch (err) {
      console.error('Error fetching courses:', err);
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCourses();
  }, []);

  const filteredCourses = courses.filter(course => 
    filter === 'all' || course.level === filter
  );

  if (loading) return <Loading />;

  if (error) {
    return (
      <div className={styles.coursesContainer}>
        <div className={styles.errorContainer}>
          <h2>Error loading courses</h2>
          <p>{error}</p>
          <button onClick={fetchCourses} className={styles.retryButton}>
            Retry
          </button>
        </div>
      </div>
    );
  }

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
        {filteredCourses.length > 0 ? (
          filteredCourses.map(course => (
            <CourseCard key={course.id} course={course} />
          ))
        ) : (
          <div className={styles.noCourses}>
            <p>No courses found for the selected filter.</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default Courses;