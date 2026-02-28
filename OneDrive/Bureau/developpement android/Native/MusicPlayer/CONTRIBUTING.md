# 🤝 Directives de Contribution

Bienvenue dans le projet **Melody Music Player** ! Nous sommes ravis que vous souhaitiez contribuer.

## 🌟 Comment Contribuer

1. **Forkez le dépôt** sur GitHub.
2. **Clonez votre fork** sur votre machine locale :
   ```bash
   git clone https://github.com/votreusername/MusicPlayer.git
   ```
3. **Créez une nouvelle branche** pour votre fonctionnalité ou correction :
   ```bash
   git checkout -b feature/ma-nouvelle-fonctionnalite
   ```
4. **Appliquez vos changements** en suivant les [Directives d'Architecture](file:///c:/Users/amine/OneDrive/Bureau/developpement%20android/Native/MusicPlayer/docs/ARCHITECTURE.md).
5. **Ajoutez des commentaires détaillés** pour toute nouvelle logique.
6. **Testez vos changements** sur un émulateur ou un appareil Android réel.
7. **Committez vos changements** avec un message clair et descriptif.
8. **Poussez votre branche** sur votre fork :
   ```bash
   git push origin feature/ma-nouvelle-fonctionnalite
   ```
9. **Ouvrez une Pull Request** (PR) sur GitHub.

## 📜 Conventions de Codage

- **Kotlin d'abord** : Tout nouveau code doit être écrit en Kotlin.
- **Jetpack Compose** : Tous les changements d'interface doivent utiliser Compose (Material 3).
- **Modèle MVVM** : Suivez le modèle Model-View-ViewModel établi.
- **Injection de Dépendances** : Utilisez une injection manuelle ou des singletons simples (pas de Hilt/Koin pour ce petit projet).
- **Gestion des Erreurs** : Enveloppez toujours les appels IO et réseau dans des blocs try-catch et gérez les erreurs via le ViewModel.
- **Nommage** : Utilisez des noms descriptifs (ex: `SongListScreen` au lieu de `List`).

## 🛠️ Exigences de Test

- Vérifiez les changements d'interface sur différentes tailles d'écran (téléphone et tablette).
- Testez la lecture média en arrière-plan et sur l'écran de verrouillage.
- Assurez-vous de ne pas introduire de fuites de mémoire (vérifiez les nettoyages dans `onStop` et `onDestroy`).
- Confirmez que toutes les permissions sont gérées correctement.

## 🧩 Signalement de Bugs et Problèmes

Si vous trouvez un bug ou avez une suggestion de fonctionnalité, veuillez ouvrir un ticket sur GitHub avec :
- Une description claire du problème.
- Les étapes pour reproduire le bug.
- Le comportement attendu vs le comportement actuel.
- Des captures d'écran si possible.

Merci d'aider à rendre **Melody** encore meilleur ! 🎶✨
