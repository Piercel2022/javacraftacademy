
import React from 'react';

const Cookies = () => {
  return (
    <div className="cookies-page">
      <div className="container">
        <h1>Politique des cookies</h1>
        <div className="legal-content">
          <section>
            <h2>Qu'est-ce qu'un cookie ?</h2>
            <p>
              Un cookie est un petit fichier texte déposé sur votre ordinateur lors de la visite d'un site web.
            </p>
          </section>
          
          <section>
            <h2>Types de cookies utilisés</h2>
            <h3>Cookies essentiels</h3>
            <p>Nécessaires au fonctionnement du site (authentification, préférences).</p>
            
            <h3>Cookies analytiques</h3>
            <p>Nous aident à comprendre comment vous utilisez notre site.</p>
            
            <h3>Cookies de performance</h3>
            <p>Améliorent les performances et la fonctionnalité du site.</p>
          </section>
          
          <section>
            <h2>Gestion des cookies</h2>
            <p>
              Vous pouvez configurer votre navigateur pour accepter ou refuser les cookies.
            </p>
          </section>
          
          <section>
            <h2>Durée de conservation</h2>
            <p>
              Les cookies sont conservés pour une durée maximale de 13 mois.
            </p>
          </section>
        </div>
        
        <p className="last-updated">
          Dernière mise à jour : 15 mars 2024
        </p>
      </div>
    </div>
  );
};

export default Cookies;