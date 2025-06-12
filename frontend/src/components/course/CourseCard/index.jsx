import React from 'react';

const CourseCard = ({ course }) => {
  return (
    <div className="course-card">
      <h3>{course?.title || 'Course Title'}</h3>
      <p>{course?.description || 'Course description...'}</p>
    </div>
  );
};

export default CourseCard;
