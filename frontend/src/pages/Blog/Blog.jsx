
import React from 'react';
import { Link } from 'react-router-dom';

const Blog = () => {
  const blogPosts = [
    {
      id: 1,
      title: "Les nouvelles fonctionnalités de Java 21",
      excerpt: "Découvrez les principales nouveautés apportées par Java 21, la dernière version LTS...",
      author: "Marie Dupont",
      date: "2024-03-15",
      category: "Actualités",
      readTime: "5 min"
    },
    {
      id: 2,
      title: "Optimiser les performances de vos applications Java",
      excerpt: "Techniques avancées pour améliorer les performances de vos applications Java en production...",
      author: "Pierre Martin",
      date: "2024-03-10",
      category: "Performance",
      readTime: "8 min"
    },
    {
      id: 3,
      title: "Guide complet des collections Java",
      excerpt: "Tout ce que vous devez savoir sur les collections Java : ArrayList, HashMap, TreeSet...",
      author: "Sophie Leroy",
      date: "2024-03-05",
      category: "Tutoriel",
      readTime: "12 min"
    }
  ];

  return (
    <div className="blog-page">
      <div className="container">
        <h1>Blog JavaCraft</h1>
        <p className="blog-subtitle">
          Actualités, tutoriels et conseils pour maîtriser Java
        </p>
        
        <div className="blog-posts">
          {blogPosts.map((post) => (
            <article key={post.id} className="blog-post-card">
              <div className="post-meta">
                <span className="category">{post.category}</span>
                <span className="read-time">{post.readTime} de lecture</span>
              </div>
              
              <h2>
                <Link to={`/blog/${post.id}`}>{post.title}</Link>
              </h2>
              
              <p className="excerpt">{post.excerpt}</p>
              
              <div className="post-footer">
                <span className="author">Par {post.author}</span>
                <span className="date">{new Date(post.date).toLocaleDateString('fr-FR')}</span>
              </div>
            </article>
          ))}
        </div>
        
        <div className="blog-pagination">
          <button className="btn btn-outline">← Articles précédents</button>
          <button className="btn btn-outline">Articles suivants →</button>
        </div>
      </div>
    </div>
  );
};

export default Blog;
