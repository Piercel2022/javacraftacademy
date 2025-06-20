
import React from 'react';

const Help = () => {
  return (
    <div className="help-page">
      <div className="container">
        <h1>Centre d'aide</h1>
        
        <div className="help-sections">
          <section className="help-section">
            <h2>Premiers pas</h2>
            <ul>
              <li><a href="#getting-started">Comment commencer ?</a></li>
              <li><a href="#account-setup">Configuration du compte</a></li>
              <li><a href="#first-course">Votre premier cours</a></li>
            </ul>
          </section>
          
          <section className="help-section">
            <h2>Problèmes techniques</h2>
            <ul>
              <li><a href="#login-issues">Problèmes de connexion</a></li>
              <li><a href="#video-playback">Lecture des vidéos</a></li>
              <li><a href="#code-editor">Éditeur de code</a></li>
            </ul>
          </section>
          
          <section className="help-section">
            <h2>Compte et profil</h2>
            <ul>
              <li><a href="#profile-update">Modifier votre profil</a></li>
              <li><a href="#password-reset">Réinitialiser le mot de passe</a></li>
              <li><a href="#delete-account">Supprimer le compte</a></li>
            </ul>
          </section>
        </div>
        
        <div className="help-contact">
          <h2>Besoin d'aide supplémentaire ?</h2>
          <p>
            Si vous ne trouvez pas la réponse à votre question, 
            <a href="/contact"> contactez notre équipe support</a>.
          </p>
        </div>
      </div>
    </div>
  );
};

export default Help;
