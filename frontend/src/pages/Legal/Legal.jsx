
import React from 'react';

const Legal = () => {
  return (
    <div className="legal-page">
      <div className="container">
        <h1>Mentions légales</h1>
        <div className="legal-content">
          <section>
            <h2>Éditeur du site</h2>
            <p>
              JavaCraft Academy<br />
              Société par actions simplifiée<br />
              Capital social : 50 000 €<br />
              RCS Paris : 123 456 789
            </p>
          </section>
          
          <section>
            <h2>Directeur de la publication</h2>
            <p>
              Jean Dupont<br />
              Directeur Général
            </p>
          </section>
          
          <section>
            <h2>Hébergement</h2>
            <p>
              Ce site est hébergé par :<br />
              OVH SAS<br />
              2 rue Kellermann<br />
              59100 Roubaix - France
            </p>
          </section>
          
          <section>
            <h2>Contact</h2>
            <p>
              Email : legal@javacraftacademy.com<br />
              Téléphone : +33 1 23 45 67 89
            </p>
          </section>
        </div>
      </div>
    </div>
  );
};

export default Legal;
