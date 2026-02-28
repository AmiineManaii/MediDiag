# 🎵 Melody Music Player

Un lecteur de musique Android natif et moderne construit avec **Jetpack Compose** et **Media3 (ExoPlayer)**. Il permet de scanner la musique locale, de rechercher des titres via une API et de gérer des playlists personnalisées.

## ✨ Fonctionnalités

- **Scan Local** : Découvre automatiquement les fichiers audio dans le dossier `Download` via MediaStore.
- **Lecture en Arrière-plan** : Lecture continue grâce à `MediaSessionService`.
- **Recherche API** : Recherche de titres via l'API Deezer avec lecture d'extraits.
- **Gestion de la File d'attente** : File d'attente intégrée directement dans l'écran de détails de la musique.
- **Playlists** : Créez, gérez et ajoutez des titres locaux ou distants à vos playlists personnelles.
- **Interface Moderne** : Interface claire et réactive construite avec Jetpack Compose et Material 3.

## 🛠️ Stack Technique

- **Langage** : Kotlin
- **Framework UI** : Jetpack Compose
- **Moteur Média** : Media3 (ExoPlayer)
- **Réseau** : Retrofit & OkHttp
- **Chargement d'Images** : Coil
- **Architecture** : MVVM (Model-View-ViewModel)

## 🚀 Commencer

1. **Cloner le dépôt** :
   ```bash
   git clone https://github.com/votreusername/MusicPlayer.git
   ```
2. **Ouvrir dans Android Studio** :
   Ouvrez le dossier du projet et laissez Gradle se synchroniser.
3. **Permissions** :
   L'application demandera l'accès au stockage et aux notifications au premier lancement.

## 📚 Documentation

Une documentation détaillée est disponible dans le dossier [docs/](file:///c:/Users/amine/OneDrive/Bureau/developpement%20android/Native/MusicPlayer/docs/) :
- [Présentation Technique](file:///c:/Users/amine/OneDrive/Bureau/developpement%20android/Native/MusicPlayer/docs/README.md)
- [Détails de l'Architecture](file:///c:/Users/amine/OneDrive/Bureau/developpement%20android/Native/MusicPlayer/docs/ARCHITECTURE.md)
- [Référence API](file:///c:/Users/amine/OneDrive/Bureau/developpement%20android/Native/MusicPlayer/docs/API_REFERENCE.md)
- [Guide de Dépannage](file:///c:/Users/amine/OneDrive/Bureau/developpement%20android/Native/MusicPlayer/docs/ThePuzzleSolver.md)

## 🤝 Contribuer

Les contributions sont les bienvenues ! Veuillez consulter [CONTRIBUTING.md](file:///c:/Users/amine/OneDrive/Bureau/developpement%20android/Native/MusicPlayer/CONTRIBUTING.md) pour les directives.

## 📄 Licence

Ce projet est sous licence MIT - voir le fichier LICENSE pour plus de détails.
