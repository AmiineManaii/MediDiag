# 📜 Journal des Modifications du Projet

Tous les changements notables du projet **Melody Music Player** seront documentés dans ce fichier.

## [1.0.0] - 2026-02-27 (Sortie Technique Initiale)

### 🚀 Ajouté
- **Application Android Native** : Construite avec Jetpack Compose et Material 3.
- **Moteur de Lecture Media3** : Intégration d' `ExoPlayer` pour une lecture audio locale robuste.
- **Scan MediaStore** : Scanne le répertoire `Download` pour les fichiers audio locaux.
- **Recherche API Deezer** : Intégration de `Retrofit` pour rechercher des titres et lire des extraits.
- **Gestion des Playlists** : Création d'un système de playlist personnalisé basé sur JSON avec `GSON`.
- **File d'attente Intégrée** : Affichage de la file d'attente actuelle directement dans l'écran de détails de la musique.
- **Permissions** : Gestion des permissions au moment de l'exécution pour le stockage et les notifications (compatible Android 13/14).

### 🛠️ Corrigé
- **Bug du Mode Répétition** : Correction d'un problème où la logique du mode répétition était inversée dans l'UI.
- **Fin de Lecture** : Résolution d'un bug où la musique était supprimée de la file d'attente de manière inattendue après la lecture.
- **Sérialisation GSON** : Ajout d'un `TypeAdapter` personnalisé pour les objets `Uri` afin de garantir que les playlists sont sauvegardées correctement.

### 📚 Documentation
- **Melody Docs en Français** : Création d'un ensemble complet de documentations markdown dans `docs/` expliquant chaque aspect du projet via des histoires et des plongées techniques.
- **Analyses de Code** : Ajout de fichiers d'explication ligne par ligne pour `MusicService`, `MusicViewModel` et `MainActivity`.
- **Guides Techniques** : Ajout de commentaires sur le code et de conseils d'optimisation des performances.

## [Prévu]

### 🔮 Fonctionnalités Futures
- **Téléchargements Hors-ligne** : Sauvegarder les extraits de l'API localement pour une écoute hors-ligne.
- **Égaliseur** : Ajouter des effets audio et des profils sonores personnalisés.
- **Thématisation Dynamique** : Implémenter des thèmes de couleurs basés sur la pochette de l'album actuel.
- **Widget** : Ajouter un widget sur l'écran d'accueil pour un contrôle rapide de la lecture.

---
*Restez à l'écoute pour plus de mises à jour musicales !* 🎶✨
