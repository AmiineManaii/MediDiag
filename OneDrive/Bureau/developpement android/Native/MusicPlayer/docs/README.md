# 🤖 Présentation du Projet : Melody Music Player 🎶

Bienvenue dans la documentation technique complète du **Melody Music Player**. Ce projet est une application musicale native Android moderne, construite avec Jetpack Compose et la bibliothèque Media3 (ExoPlayer). Elle est conçue pour offrir une expérience de lecture fluide de la musique locale avec des fonctionnalités avancées comme l'intégration d'API pour la recherche de titres et la gestion de playlists.

## 🏗️ Architecture Technique

L'application suit un modèle architectural **MVVM (Model-View-ViewModel)**, assurant une séparation claire des préoccupations entre l'interface utilisateur, la logique métier et les sources de données.

### 1. Composants Clés
- **Couche UI** : Construite entièrement avec **Jetpack Compose**, offrant une interface réactive et déclarative.
- **Gestion d'État** : Gérée par [MusicViewModel](file:///c:/Users/amine/OneDrive/Bureau/developpement%20android/Native/MusicPlayer/app/src/main/java/com/example/musicplayer/MusicViewModel.kt) en utilisant `MutableState` et `CoroutineScope`.
- **Moteur de Lecture** : Propulsé par **Media3 (ExoPlayer)**, encapsulé dans un `MediaSessionService`.
- **Couche de Données** : Interface avec l'API **MediaStore** d'Android pour les fichiers locaux et **Retrofit** pour les appels API distants.

### 2. Diagramme de Flux de Haut Niveau
```text
[ Interface Utilisateur ] <--> [ MediaController ] <--> [ MediaSession ] <--> [ ExoPlayer ]
          ^                           |                                         |
          |                           v                                         v
[ MusicViewModel ] <--> [ API MediaStore ]                          [ Sortie Audio ]
```

---

## 🚀 Fonctionnalités Techniques Clés

### 1. Lecture Média Robuste
L'application utilise `MediaSessionService` pour garantir que la lecture continue même lorsque l'interface utilisateur est en arrière-plan. Elle prend en charge :
- La gestion automatique des notifications.
- Les contrôles sur l'écran de verrouillage et le volet de notification.
- La prise en charge des boutons Bluetooth et des casques.

### 2. Scan Local Dynamique
En utilisant `ContentResolver` et `MediaStore`, l'application scanne le stockage de l'appareil pour trouver des fichiers audio, en filtrant spécifiquement le répertoire `Download` pour assurer une bibliothèque utilisateur propre.

### 3. Intégration d'API
L'intégration de l'API **Deezer** via Retrofit permet aux utilisateurs de rechercher des titres, de lire des extraits et d'ajouter des chansons distantes à leurs playlists locales ou à leur file d'attente.

---

## 📚 Guides Techniques Détaillés

Pour comprendre des parties spécifiques de la base de code, explorez ces modules détaillés :

1. [📖 Modèle de Données et Métadonnées](file:///c:/Users/amine/OneDrive/Bureau/developpement%20android/Native/MusicPlayer/docs/TheSecretDiary.md) - Comment nous représentons les chansons et les convertissons pour Media3.
2. [🔍 MediaStore et Chargement de Fichiers](file:///c:/Users/amine/OneDrive/Bureau/developpement%20android/Native/MusicPlayer/docs/TheMagicTreasureHunt.md) - La mécanique du scan du stockage local.
3. [📜 Permissions et Manifeste](file:///c:/Users/amine/OneDrive/Bureau/developpement%20android/Native/MusicPlayer/docs/TheGoldenRules.md) - Sécurité Android et exigences des services d'arrière-plan.
4. [🎮 Synchronisation de l'UI](file:///c:/Users/amine/OneDrive/Bureau/developpement%20android/Native/MusicPlayer/docs/TheRemoteControl.md) - Connexion de l'interface Compose à la MediaSession.
5. [🔋 Services d'Arrière-plan](file:///c:/Users/amine/OneDrive/Bureau/developpement%20android/Native/MusicPlayer/docs/TheNeverEndingParty.md) - Gestion du cycle de vie du service de lecture.
6. [🧩 Dépannage et Optimisation](file:///c:/Users/amine/OneDrive/Bureau/developpement%20android/Native/MusicPlayer/docs/ThePuzzleSolver.md) - Pièges courants et considérations de performance.

## 🛠️ Explications Détaillées du Code (Ligne par Ligne)

Pour une compréhension approfondie de l'implémentation, consultez les guides suivants :
- [Analyse de MusicService.kt](file:///c:/Users/amine/OneDrive/Bureau/developpement%20android/Native/MusicPlayer/docs/CODE_MUSIC_SERVICE.md)
- [Analyse de MusicViewModel.kt](file:///c:/Users/amine/OneDrive/Bureau/developpement%20android/Native/MusicPlayer/docs/CODE_MUSIC_VIEWMODEL.md)
- [Analyse de MainActivity.kt](file:///c:/Users/amine/OneDrive/Bureau/developpement%20android/Native/MusicPlayer/docs/CODE_MAIN_ACTIVITY.md)
