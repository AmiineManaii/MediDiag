# 🚀 En Route pour l'Aventure ! : Guide d'Installation 🏰✨

Prêt à installer le **Melody Music Player** sur votre environnement de développement ? Suivez ces étapes comme une quête épique pour faire apparaître l'application sur votre téléphone ! 🏴‍☠️🗝️

## 🏗️ Configuration de l'Environnement

Avant de commencer, assurez-vous d'avoir les outils de magicien nécessaires :
1. **Android Studio Jellyfish** (ou une version plus récente).
2. **JDK 17** installé et configuré.
3. Un appareil Android (réel ou émulateur) fonctionnant sous **Android 8.0 (API 26)** ou plus.

---

## 🛠️ Étapes d'Installation

### Étape 1 : Récupérer le Code Magique 🪄
Tout le code est rangé dans un château appelé GitHub.
```bash
git clone https://github.com/votreusername/MusicPlayer.git
```
**Pourquoi ?** Cela crée une copie locale de tout le projet sur votre ordinateur.

### Étape 2 : Ouvrir l'Atelier (Android Studio) 🛠️
1. Lancez Android Studio.
2. Choisissez **Open** et sélectionnez le dossier `MusicPlayer`.
3. Attendez que le petit éléphant **Gradle** finisse de synchroniser (cela peut prendre quelques minutes la première fois).

### Étape 3 : Configurer les Secrets de l'API (Optionnel) 🤫
Si vous voulez utiliser la recherche Deezer, vérifiez que le fichier `RetrofitInstance.kt` pointe vers la bonne URL de base. (Actuellement configuré pour Jamendo/Deezer public).

### Étape 4 : Lancer l'Application ! 🚀
1. Connectez votre téléphone en USB (avec le mode "Débogage USB" activé).
2. Cliquez sur le gros bouton vert **[Play]** en haut à droite d'Android Studio.
3. Magie ! L'application s'installe et s'ouvre sur votre écran.

---

## 🧪 Vérification et Tests

Une fois installé, vérifiez que tout fonctionne :
- **Scan** : L'appli demande-t-elle la permission ? Voyez-vous vos musiques du dossier `Download` ?
- **Lecture** : La musique démarre-t-elle quand vous cliquez sur un titre ?
- **Arrière-plan** : Si vous quittez l'appli, la musique continue-t-elle de jouer ?
- **Recherche** : Pouvez-vous trouver un artiste via l'écran de recherche ?

---

## ⚠️ Problèmes Courants au Démarrage

- **Erreur de SDK** : Assurez-vous d'avoir installé le SDK Android 34 dans le "SDK Manager".
- **Gradle échoue** : Vérifiez votre connexion Internet ; Gradle doit télécharger des bibliothèques la première fois.
- **Appareil non détecté** : Vérifiez votre câble USB et les pilotes (drivers) de votre téléphone.

Bravo ! Vous avez terminé l'installation. La fête musicale peut commencer ! 🎧🕺🌟🚀
