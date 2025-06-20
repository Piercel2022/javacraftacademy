
import React from 'react';
import { useParams } from 'react-router-dom';

const BlogPost = () => {
  const { id } = useParams();
  
  // Dans une vraie app, vous récupéreriez l'article depuis une API
  const post = {
    id: id,
    title: "Les nouvelles fonctionnalités de Java 21",
    content: `
      <p>Java 21 marque une étape importante dans l'évolution du langage avec l'introduction de plusieurs fonctionnalités révolutionnaires...</p>
      
      <h2>Virtual Threads</h2>
      <p>Les Virtual Threads constituent probablement la fonctionnalité la plus attendue de Java 21...</p>
      
      <h2>Pattern Matching</h2>
      <p>Le pattern matching continue d'évoluer avec de nouvelles possibilités...</p>
      
      <h2>Conclusion</h2>
      <p>Java 21 apporte des améliorations significatives qui modernisent le développement...</p>
    `,
    author: "Marie Dupont",
    date: "2024-03-15",
    category: "Actualités",
    readTime: "5 min"
  };

  return (
    <div className="blog-post-page">
      <div className="container">
        <article className="blog-post">
          <header className="post-header">
            <div className="post-meta">
              <span className="category">{post.category}</span>
              <span className="read-time">{post.readTime} de lecture</span>
            </div>
            
            <h1>{post.title}</h1>
            
            <div className="post-info">
              <span className="author">Par {post.author}</span>
              <span className="date">{new Date(post.date).toLocaleDateString('fr-FR')}</span>
            </div>
          </header>
          
          <div className="post-content" dangerouslySetInnerHTML={{ __html: post.content }} />
          
          <footer className="post-footer">
            <div className="tags">
              <span className="tag">Java</span>
              <span className="tag">Programmation</span>
              <span className="tag">Développement</span>
            </div>
            
            <div className="share-buttons">
              <button className="btn btn-outline">Partager</button>
              <button className="btn btn-outline">Sauvegarder</button>
            </div>
          </footer>
        </article>
      </div>
    </div>
  );
};

export default BlogPost;