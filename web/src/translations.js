import Vue from 'vue';
import VueI18n from 'vue-i18n';

Vue.use(VueI18n);

const messages = {
    en: {
        // Pages
        home: {
            welcome: 'Welcome',
            discord: 'Log in with Discord'
        },
        auth: {
            waiting: {
                title: 'Waiting',
                description: 'Waiting for confirmation in the separate window...'
            },
            fetching: {
                title: 'Loading',
                description: 'Retrieving information...'
            }
        },
        microsoft: {
            connect: 'Log in with Microsoft'
        },
        notFound: {
            description: 'The requested page does not exist'
        },
        redirect: {
            success: 'Successfully connected',
            failure: 'Connection denied'
        },
        settings: {
            remember: 'Remember who I am (optional)',

            iAcceptThe: 'I accept the ',
            terms: 'Terms of Services',
            andThe: 'and the',
            policy: 'Privacy Policy',

            link: 'Link my account'
        },
        about: {
            sources: 'Original sources',
            authors: 'Authors'
        },

        layout: {
            cancel: 'Cancel the procedure',
            logout: 'Log out',

            navigation: {
                privacy: 'Privacy',
                about: 'About'
            }
        },

        error: {
            title: 'Error',
            back: 'Back',
            retry: 'Retry'
        },

        popups: {
            discord: 'Connection to Discord',
            microsoft: 'Connection to Microsoft'
        },
        steps: {
            discord: 'Connection to Discord',
            microsoft: 'Connection to Microsoft',
            settings: 'Settings review'
        }
    },
    fr: {
        // Pages
        home: {
            welcome: 'Bienvenue',
            discord: 'Se connecter via Discord'
        },
        auth: {
            waiting: {
                title: 'En attente',
                description: 'En attente de la confirmation dans la fenêtre séparée'
            },
            fetching: {
                title: 'Chargement',
                description: 'Récupération des informations...'
            }
        },
        microsoft: {
            connect: 'Se connecter via Microsoft'
        },
        notFound: {
            description: 'La page demandée n\'existe pas'
        },
        redirect: {
            success: 'Connexion réussie',
            failure: 'Connexion refusée'
        },
        settings: {
            remember: 'Se souvenir de qui je suis (facultatif)',

            iAcceptThe: 'J\'accepte les',
            terms: 'conditions générales d\'utilisation',
            andThe: 'et la',
            policy: 'politique de confidentialité',

            link: 'Lier mon compte'
        },
        about: {
            sources: 'Sources originales',
            authors: 'Auteurs'
        },

        layout: {
            cancel: 'Annuler la procédure',
            logout: 'Se déconnecter',

            navigation: {
                privacy: 'Confidentialité',
                about: 'À Propos'
            }
        },

        error: {
            title: 'Erreur',
            back: 'Retour',
            retry: 'Rééssayer'
        },

        popups: {
            discord: 'Connexion à Discord',
            microsoft: 'Connexion à Microsoft'
        },
        steps: {
            discord: 'Connexion à Discord',
            microsoft: 'Connexion à Microsoft',
            settings: 'Validation des paramètres'
        }
    }
};

export default new VueI18n({
    locale: window.navigator.language.slice(0, 2) === 'fr' ? 'fr' : 'en',
    messages: messages
});