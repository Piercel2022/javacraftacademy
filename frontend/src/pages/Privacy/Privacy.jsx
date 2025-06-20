
import React from 'react';

const Privacy = () => {
  return (
    <div className="privacy-page">
      <div className="container">
        <h1>Politique de confidentialité</h1>
        <div className="legal-content">
          <section>
            <h2>1. Collecte des données</h2>
            <p>
              Nous collectons uniquement les informations nécessaires au fonctionnement de notre service.
            </p>
          </section>
          
          <section>
            <h2>2. Utilisation des données</h2>
            <p>
              Vos données personnelles sont utilisées pour améliorer votre expérience d'apprentissage.
            </p>
          </section>
          
          <section>
            <h2>3. Partage des données</h2>
            <p>
              Nous ne vendons ni ne partageons vos données personnelles avec des tiers.
            </p>
          </section>
          
          <section>
            <h2>4. Sécurité</h2>
            <p>
              Nous mettons en place des mesures de sécurité appropriées pour protéger vos données.
            </p>
          </section>
          
          <section>
            <h2>5. Vos droits</h2>
            <p>
              Vous avez le droit d'accéder, de modifier ou de supprimer vos données personnelles.
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

export default Privacy;
