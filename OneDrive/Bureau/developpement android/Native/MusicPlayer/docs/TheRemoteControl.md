# 🎮 Synchronisation de l'UI : La Télécommande `MediaController`

L'application utilise le **MediaController** de Media3 pour interagir avec le service de musique sous-jacent. Il agit comme un pont entre l'interface utilisateur Jetpack Compose et l'instance `ExoPlayer` qui s'exécute en arrière-plan.

## 🏗️ Définition Technique

La [MainActivity.kt](file:///c:/Users/amine/OneDrive/Bureau/developpement%20android/Native/MusicPlayer/app/src/main/java/com/example/musicplayer/MainActivity.kt) utilise un `ListenableFuture` pour se connecter au [MusicService](file:///c:/Users/amine/OneDrive/Bureau/developpement%20android/Native/MusicPlayer/app/src/main/java/com/example/musicplayer/service/MusicService.kt).

---

## 🛠️ Exemples de Code et Commentaires Détaillés

### Exemple 1 : Connexion Asynchrone
Connexion de l'UI au service de lecture.

```kotlin
val sessionToken = SessionToken(this, ComponentName(this, MusicService::class.java))
controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()

controllerFuture.addListener({
    mediaController = controllerFuture.get()
    mediaController?.addListener(playerListener)
    updateUiWithCurrentState()
}, ContextCompat.getMainExecutor(this))
```
**Analyse ligne par ligne :**
- `SessionToken` : Un identifiant unique pour notre service de musique.
- `buildAsync()` : La connexion est non bloquante. Nous utilisons un écouteur (listener) pour savoir quand elle est terminée.
- `controllerFuture.get()` : Récupère l'objet `MediaController` réel une fois connecté.
- `getMainExecutor(this)` : Garantit que l'écouteur s'exécute sur le thread principal (thread UI).

### Exemple 2 : Écouteur d'État (`Player.Listener`)
Réagir aux changements de lecture.

```kotlin
private val playerListener = object : Player.Listener {
    override fun onIsPlayingChanged(playing: Boolean) {
        viewModel.isPlaying.value = playing
    }
    
    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        val currentId = mediaItem?.mediaId
        viewModel.nowPlayingSong.value = viewModel.songs.find { it.id.toString() == currentId }
        updateQueueItems()
    }
}
```
**Analyse ligne par ligne :**
- `onIsPlayingChanged` : Met à jour instantanément l'état du bouton Play/Pause dans l'UI.
- `onMediaItemTransition` : Déclenché quand une nouvelle chanson commence. Nous utilisons l' `mediaId` pour trouver l'objet `Song` correspondant dans notre ViewModel.
- `updateQueueItems()` : Rafraîchit la vue de la file d'attente intégrée dans l'écran de détails.

### Exemple 3 : Exécution de Commandes (Actions UI)
Envoi de commandes depuis l'UI Compose.

```kotlin
// Dans MainActivity DetailScreenWrapper
onPlayPauseClick = { 
    if (viewModel.isPlaying.value) mediaController?.pause() 
    else mediaController?.play() 
},
onNextClick = { mediaController?.seekToNext() },
onSeekTo = { pos -> mediaController?.seekTo(pos) }
```
**Analyse ligne par ligne :**
- `mediaController?.play()` : Envoie une commande au service d'arrière-plan.
- `seekToNext()` : Passe automatiquement à l'élément suivant dans la file d'attente Media3.
- `seekTo(pos)` : Met à jour la position de lecture actuelle (ex: quand l'utilisateur fait glisser la barre de progression).

---

## ⚠️ Pièges Courants et Considérations de Performance

- **Fuites de Mémoire** : Appelez toujours `MediaController.releaseFuture(controllerFuture)` dans `onStop()` pour éviter les fuites.
- **Sécurité Null** : Le `mediaController` peut être nul si le service n'est pas prêt. Utilisez toujours l'opérateur d'appel sécurisé (`?.`).
- **Lags UI** : Évitez d'effectuer une logique lourde à l'intérieur des méthodes de `Player.Listener`. Gardez-les légères (mettez seulement à jour les états du ViewModel).

## 🛠️ Dépannage
- **Les boutons ne répondent pas** : Vérifiez que `mediaController` n'est pas nul et que `mediaController?.addListener(playerListener)` a bien été appelé.
- **État UI décalé** : Assurez-vous que toutes les mises à jour d'état (ex: `isPlaying.value = ...`) se produisent sur le thread principal.
- **La connexion échoue** : Vérifiez si le `MusicService` est correctement déclaré dans le `AndroidManifest.xml`.
