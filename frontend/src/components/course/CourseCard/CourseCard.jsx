import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useCourse } from '../../../hooks/useCourse';
import { useProgress } from '../../../hooks/useProgress';
import Progress from '../../../pages/Progress';
import Button from '../../common/Button';
import styles from './CourseCard.module.css';

const CourseCard = ({ 
  course,
  showProgress = true,
  showEnrollButton = true,
  className = '',
  onEnroll,
  onView
}) => {
  if (!course) return null;
  const navigate = useNavigate();
  const { enrollInCourse, isEnrolled } = useCourse();
  const { getCourseProgress } = useProgress();

  const {
    id,
    title,
    description,
    thumbnail,
    instructor,
    duration,
    level,
    price,
    rating,
    studentsCount,
    tags = [],
    lastUpdated,
    isNew = false,
    isFeatured = false
  } = course;

  const enrolled = isEnrolled(id);
  const progress = getCourseProgress(id);

  const handleEnroll = async (e) => {
    e.stopPropagation();
    try {
      await enrollInCourse(id);
      if (onEnroll) {
        onEnroll(course);
      }
    } catch (error) {
      console.error('Erreur lors de l\'inscription:', error);
    }
  };

  const handleView = () => {
    if (onView) {
      onView(course);
    } else {
      navigate(`/courses/${id}`);
    }
  };

  const handleStartLesson = (e) => {
    e.stopPropagation();
    navigate(`/courses/${id}/lessons`);
  };

  const formatDuration = (minutes) => {
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    return hours > 0 ? `${hours}h ${mins}m` : `${mins}m`;
  };

  const formatPrice = (price) => {
    return price === 0 ? 'Gratuit' : `${price}€`;
  };

  const renderRating = (rating) => {
    const stars = [];
    const fullStars = Math.floor(rating);
    const hasHalfStar = rating % 1 !== 0;

    for (let i = 0; i < fullStars; i++) {
      stars.push(<span key={i} className={styles.star}>★</span>);
    }

    if (hasHalfStar) {
      stars.push(<span key="half" className={styles.halfStar}>☆</span>);
    }

    const emptyStars = 5 - Math.ceil(rating);
    for (let i = 0; i < emptyStars; i++) {
      stars.push(<span key={`empty-${i}`} className={styles.emptyStar}>☆</span>);
    }

    return stars;
  };

  return (
    <div 
      className={`${styles.courseCard} ${className} ${isFeatured ? styles.featured : ''}`}
      onClick={handleView}
    >
      {/* Badge pour nouveaux cours */}
      {isNew && (
        <div className={styles.badge}>
          <span className={styles.newBadge}>Nouveau</span>
        </div>
      )}

      {/* Badge pour cours en vedette */}
      {isFeatured && (
        <div className={styles.badge}>
          <span className={styles.featuredBadge}>★ Vedette</span>
        </div>
      )}

      {/* Thumbnail du cours */}
      <div className={styles.thumbnail}>
        <img 
          src={thumbnail || '/assets/images/course-placeholder.jpg'} 
          alt={title}
          className={styles.thumbnailImage}
          onError={(e) => {
            e.target.src = '/assets/images/course-placeholder.jpg';
          }}
        />
        <div className={styles.overlay}>
          <div className={styles.playButton}>
            <span>▶</span>
          </div>
        </div>
      </div>

      {/* Contenu du cours */}
      <div className={styles.content}>
        {/* Header avec titre et niveau */}
        <div className={styles.header}>
          <h3 className={styles.title}>{title}</h3>
          <span className={`${styles.level} ${styles[level?.toLowerCase()]}`}>
            {level}
          </span>
        </div>

        {/* Description */}
        <p className={styles.description}>{description}</p>

        {/* Informations de l'instructeur */}
        <div className={styles.instructor}>
          <img 
            src={instructor?.avatar || '/assets/images/avatars/default.png'} 
            alt={instructor?.name}
            className={styles.instructorAvatar}
          />
          <span className={styles.instructorName}>{instructor?.name}</span>
        </div>

        {/* Tags */}
        {tags.length > 0 && (
          <div className={styles.tags}>
            {tags.slice(0, 3).map((tag, index) => (
              <span key={index} className={styles.tag}>
                {tag}
              </span>
            ))}
            {tags.length > 3 && (
              <span className={styles.moreTagsIndicator}>
                +{tags.length - 3}
              </span>
            )}
          </div>
        )}

        {/* Progression si inscrit */}
        {enrolled && showProgress && progress && (
          <div className={styles.progressSection}>
            <div className={styles.progressInfo}>
              <span className={styles.progressLabel}>Progression</span>
              <span className={styles.progressValue}>{Math.round(progress.percentage)}%</span>
            </div>
            <Progress
              progress={progress.percentage} 
              height="6px"
              className={styles.progressBar}
            />
          </div>
        )}

        {/* Métadonnées du cours */}
        <div className={styles.metadata}>
          <div className={styles.metaItem}>
            <span className={styles.metaIcon}>🕒</span>
            <span className={styles.metaText}>{formatDuration(duration)}</span>
          </div>
          
          <div className={styles.metaItem}>
            <span className={styles.metaIcon}>👥</span>
            <span className={styles.metaText}>{studentsCount} étudiants</span>
          </div>

          {rating && (
            <div className={styles.metaItem}>
              <div className={styles.rating}>
                {renderRating(rating)}
                <span className={styles.ratingValue}>({rating})</span>
              </div>
            </div>
          )}
        </div>

        {/* Prix */}
        <div className={styles.pricing}>
          <span className={`${styles.price} ${price === 0 ? styles.free : ''}`}>
            {formatPrice(price)}
          </span>
          {lastUpdated && (
            <span className={styles.lastUpdated}>
              Mis à jour le {new Date(lastUpdated).toLocaleDateString('fr-FR')}
            </span>
          )}
        </div>
      </div>

      {/* Actions */}
      <div className={styles.actions}>
        {enrolled ? (
          <Button 
            variant="primary" 
            onClick={handleStartLesson}
            className={styles.actionButton}
          >
            Continuer
          </Button>
        ) : (
          showEnrollButton && (
            <Button 
              variant="primary" 
              onClick={handleEnroll}
              className={styles.actionButton}
            >
              {price === 0 ? 'S\'inscrire' : 'Acheter'}
            </Button>
          )
        )}
        
        <Button 
          variant="secondary" 
          onClick={handleView}
          className={styles.actionButton}
        >
          Voir détails
        </Button>
      </div>
    </div>
  );
};

export default CourseCard;