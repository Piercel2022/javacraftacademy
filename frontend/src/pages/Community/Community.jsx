
import React from 'react';

const Community = () => {
  const forumCategories = [
    { name: "Débutants", posts: 245, description: "Questions et discussions pour les nouveaux développeurs Java" },
    { name: "Avancé", posts: 127, description: "Sujets techniques approfondis" },
    { name: "Projets", posts: 89, description: "Partagez vos projets et obtenez des feedbacks" },
    { name: "Emploi", posts: 56, description: "Offres d'emploi et conseils carrière" }
  ];

  return (
    <div className="community-page">
      <div className="container">
        <h1>Communauté JavaCraft</h1>
        
        <div className="community-stats">
          <div className="stat-item">
            <h3>12,450</h3>
            <p>Membres actifs</p>
          </div>
          <div className="stat-item">
            <h3>2,847</h3>
            <p>Discussions</p>
          </div>
          <div className="stat-item">
            <h3>8,923</h3>
            <p>Réponses</p>
          </div>
        </div>
        
        <div className="forum-categories">
          <h2>Catégories du forum</h2>
          {forumCategories.map((category, index) => (
            <div key={index} className="category-card">
              <h3>{category.name}</h3>
              <p>{category.description}</p>
              <span className="post-count">{category.posts} discussions</span>
            </div>
          ))}
        </div>
        
        <div className="community-guidelines">
          <h2>Règles de la communauté</h2>
          <ul>
            <li>Respectez les autres membres</li>
            <li>Restez dans le sujet</li>
            <li>Partagez vos connaissances</li>
            <li>Aidez les débutants</li>
            <li>Pas de spam ou de publicité</li>
          </ul>
        </div>
      </div>
    </div>
  );
};

export default Community;
