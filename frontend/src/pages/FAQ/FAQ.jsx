
import React, { useState } from 'react';

const FAQ = () => {
  const [openItem, setOpenItem] = useState(null);

  const faqItems = [
    {
      id: 1,
      question: "Comment fonctionne JavaCraft Academy ?",
      answer: "JavaCraft Academy propose des cours interactifs de Java avec des exercices pratiques, des projets et un suivi personnalisé de votre progression."
    },
    {
      id: 2,
      question: "Les cours sont-ils gratuits ?",
      answer: "Nous proposons des cours gratuits et premium. Les cours de base sont accessibles gratuitement, tandis que les cours avancés nécessitent un abonnement."
    },
    {
      id: 3,
      question: "Ai-je besoin d'installer Java sur mon ordinateur ?",
      answer: "Non ! Notre plateforme inclut un environnement de développement intégré dans le navigateur. Vous pouvez coder directement sur le site."
    },
    {
      id: 4,
      question: "Puis-je obtenir un certificat ?",
      answer: "Oui, nous délivrons des certificats de completion pour chaque cours terminé avec succès."
    },
    {
      id: 5,
      question: "Comment puis-je suivre ma progression ?",
      answer: "Votre tableau de bord personnel affiche votre progression, vos réalisations et vos statistiques d'apprentissage."
    }
  ];

  const toggleItem = (id) => {
    setOpenItem(openItem === id ? null : id);
  };

  return (
    <div className="faq-page">
      <div className="container">
        <h1>Questions Fréquentes</h1>
        
        <div className="faq-list">
          {faqItems.map((item) => (
            <div key={item.id} className="faq-item">
              <button
                className="faq-question"
                onClick={() => toggleItem(item.id)}
              >
                {item.question}
                <span className={`faq-icon ${openItem === item.id ? 'open' : ''}`}>
                  ▼
                </span>
              </button>
              {openItem === item.id && (
                <div className="faq-answer">
                  <p>{item.answer}</p>
                </div>
              )}
            </div>
          ))}
        </div>
        
        <div className="faq-footer">
          <h2>Votre question n'est pas listée ?</h2>
          <p>
            <a href="/contact">Contactez-nous</a> et nous vous répondrons rapidement !
          </p>
        </div>
      </div>
    </div>
  );
};

export default FAQ;