// Absolute path: frontend/src/pages/CourseDetail/CourseDetail.jsx
import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Button from '../../components/common/Button';
import Loading from '../../components/common/Loading';
import styles from './CourseDetail.module.css';

const CourseDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [course, setCourse] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Mock data - replace with actual API call
    setTimeout(() => {
      setCourse({
        id: parseInt(id),
        title: 'Java Fundamentals',
        description: 'Learn the basics of Java programming language',
        longDescription: 'This comprehensive course covers all the fundamentals of Java programming. You will learn about variables, data types, control structures, methods, classes, and objects. Perfect for beginners who want to start their journey in Java development.',
        level: 'beginner',
        duration: '4 weeks',
        lessons: 12,
        students: 1250,
        rating: 4.8,
        instructor: 'John Smith',
        image: '/api/placeholder/600/300',
        syllabus: [
          'Introduction to Java',
          'Variables and Data Types',
          'Control Structures',
          'Methods and Functions',
          'Object-Oriented Programming',
          'Error Handling'
        ],
        prerequisites: ['Basic computer knowledge'],
        price: 99
      });
      setLoading(false);
    }, 1000);
  }, [id]);

  const handleEnroll = () => {
    // Navigate to first lesson
    navigate(`/lesson/1`);
  };

  if (loading) return <Loading />;
  if (!course) return <div>Course not found</div>;

  return (
    <div className={styles.courseDetailContainer}>
      <div className={styles.courseHeader}>
        <img src={course.image} alt={course.title} className={styles.courseImage} />
        <div className={styles.courseInfo}>
          <h1>{course.title}</h1>
          <p className={styles.description}>{course.description}</p>
          <div className={styles.courseStats}>
            <span>Level: {course.level}</span>
            <span>Duration: {course.duration}</span>
            <span>Lessons: {course.lessons}</span>
            <span>Students: {course.students}</span>
            <span>Rating: {course.rating}/5</span>
          </div>
          <div className={styles.instructor}>
            <strong>Instructor: {course.instructor}</strong>
          </div>
          <div className={styles.price}>
            <span className={styles.priceTag}>${course.price}</span>
            <Button onClick={handleEnroll} variant="primary">
              Enroll Now
            </Button>
          </div>
        </div>
      </div>

      <div className={styles.courseContent}>
        <div className={styles.section}>
          <h2>About This Course</h2>
          <p>{course.longDescription}</p>
        </div>

        <div className={styles.section}>
          <h2>What You'll Learn</h2>
          <ul className={styles.syllabus}>
            {course.syllabus.map((item, index) => (
              <li key={index}>{item}</li>
            ))}
          </ul>
        </div>

        <div className={styles.section}>
          <h2>Prerequisites</h2>
          <ul>
            {course.prerequisites.map((item, index) => (
              <li key={index}>{item}</li>
            ))}
          </ul>
        </div>
      </div>
    </div>
  );
};

export default CourseDetail;