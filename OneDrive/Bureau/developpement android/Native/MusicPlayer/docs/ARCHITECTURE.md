# 🏗️ Documentation de l'Architecture : Melody Music Player

Le **Melody Music Player** est une application Android native construite selon les standards modernes. Elle utilise l'architecture **MVVM (Model-View-ViewModel)** et **Jetpack Compose** pour une base de code propre, modulaire et réactive.

## 🧱 Architecture en Couches

### 1. Couche Modèle (Model)
- **Modèles de Données** : Simples classes de données Kotlin (ex: [Song.kt](file:///c:/Users/amine/OneDrive/Bureau/developpement%20android/Native/MusicPlayer/app/src/main/java/com/example/musicplayer/model/Song.kt), [Playlist.kt](file:///c:/Users/amine/OneDrive/Bureau/developpement%20android/Native/MusicPlayer/app/src/main/java/com/example/musicplayer/model/Playlist.kt)).
- **Données Locales** : Gérées par l'API **MediaStore** pour la découverte de fichiers locaux et **SharedPreferences** avec **GSON** pour la persistance des playlists.
- **Données Distantes** : Gérées par **Retrofit** pour la communication avec l'API Deezer.

### 2. Couche ViewModel
- **[MusicViewModel.kt](file:///c:/Users/amine/OneDrive/Bureau/developpement%20android/Native/MusicPlayer/app/src/main/java/com/example/musicplayer/MusicViewModel.kt)** : Le hub central de l'application.
  - Détient l'état des chansons actuelles, des playlists et du statut de lecture.
  - Gère la logique de chargement pour les pistes locales et API.
  - Gère les opérations asynchrones en utilisant `viewModelScope` et `Dispatchers.IO`.

### 3. Couche Vue (UI)
- **Jetpack Compose** : Tous les composants de l'interface utilisateur sont déclaratifs et réactifs.
- **[MainActivity.kt](file:///c:/Users/amine/OneDrive/Bureau/developpement%20android/Native/MusicPlayer/app/src/main/java/com/example/musicplayer/MainActivity.kt)** : Le point d'entrée qui héberge les écrans Compose et gère la connexion au `MediaController`.
- **Écrans** : Chaque fonctionnalité majeure a son propre écran (ex: `MusicListScreen`, `SearchScreen`, `PlaylistScreen`).

### 4. Couche Service (Lecture)
- **[MusicService.kt](file:///c:/Users/amine/OneDrive/Bureau/developpement%20android/Native/MusicPlayer/app/src/main/java/com/example/musicplayer/service/MusicService.kt)** : Un `MediaSessionService` qui s'exécute indépendamment de l'interface utilisateur.
  - Héberge l'instance `ExoPlayer`.
  - Gère la `MediaSession` pour les contrôles de lecture à l'échelle du système.

---

## 🔄 Flux de Données et d'Événements

1. **Action Utilisateur** : L'utilisateur clique sur une chanson dans `MusicListScreen`.
2. **Événement ViewModel** : `MainActivity` reçoit le clic et envoie une commande au `mediaController`.
3. **Logique du Service** : Le `mediaController` communique avec le `MusicService`. Le service demande à `ExoPlayer` de lire la chanson.
4. **Mise à jour d'État** : Le service informe le `mediaController` du changement d'état. `MainActivity` écoute cela et met à jour le `MusicViewModel`.
5. **Mise à jour UI** : L'interface utilisateur Compose se re-dessine automatiquement car elle observe l'état du `MusicViewModel`.

---

## 🛠️ Meilleures Pratiques de Performance

- **Opérations en Arrière-plan** : Toutes les entrées/sorties (scan de fichiers, appels API, sauvegarde JSON) sont déportées sur `Dispatchers.IO`.
- **Chargement d'Images** : Nous utilisons **Coil** pour le chargement paresseux et la mise en cache des bitmaps de pochettes d'album.
- **Recomposition Minimale** : Nous passons des objets stables et des primitives aux fonctions Compose pour éviter les redessins d'UI inutiles.
- **Nettoyage des Ressources** : Nous libérons `ExoPlayer` et `MediaController` dans `onDestroy()` et `onStop()` pour éviter les fuites de mémoire et économiser la batterie.

## 🛠️ Gestion des Erreurs et Résilience

- **Permissions** : Vérifications au moment de l'exécution pour le stockage et les notifications avec des conseils clairs pour l'utilisateur.
- **États de Secours** : Des titres, artistes et pochettes par défaut sont utilisés si les métadonnées sont manquantes.
- **Résilience Réseau** : Des blocs try-catch enveloppent les appels API pour éviter les plantages si l'utilisateur est hors ligne.
- **Sécurité de Sérialisation** : `TypeAdapter` personnalisé pour GSON afin de sauvegarder en toute sécurité les objets `Uri`.

Pour plus d'informations, consultez la [Plongée Technique](file:///c:/Users/amine/OneDrive/Bureau/developpement%20android/Native/MusicPlayer/docs/README.md).
