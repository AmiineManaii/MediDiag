# Lecture et Contrôles (MediaController)

Ce fichier explique comment l'interface utilisateur communique avec le service de musique via le `MediaController`.

## 1. Initialisation du Controller

Dans `MainActivity.kt`, nous utilisons un `ListenableFuture` car la connexion au service est asynchrone (elle prend un peu de temps).

```kotlin
// Étape 1 : Créer un jeton de session pour identifier le service
val sessionToken = SessionToken(this, ComponentName(this, MusicService::class.java))

// Étape 2 : Construire le controller
controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()

// Étape 3 : Écouter quand la connexion est prête
controllerFuture.addListener({
    mediaController = controllerFuture.get() // Le controller est maintenant prêt !
    mediaController?.addListener(playerListener) // On écoute les changements
}, ContextCompat.getMainExecutor(this))
```

## 2. Le Listener (`Player.Listener`)

C'est le mécanisme qui permet à l'UI de rester à jour. Voici les fonctions clés utilisées :

```kotlin
private val playerListener = object : Player.Listener {
    // Appelé quand on appuie sur Play ou Pause
    override fun onIsPlayingChanged(playing: Boolean) {
        isPlaying.value = playing
    }

    // Appelé quand la chanson change (automatiquement ou via Suivant)
    override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
        val mediaId = mediaController?.currentMediaItem?.mediaId
        // On met à jour l'UI avec la nouvelle chanson
        nowPlayingSong.value = songs.find { it.id.toString() == mediaId }
    }
}
```

## 3. Envoyer des commandes

Le `MediaController` propose des méthodes prêtes à l'emploi. Voici comment nous les utilisons dans les boutons de l'application :

- **Play/Pause** : 
  ```kotlin
  if (isPlaying.value) mediaController?.pause() else mediaController?.play()
  ```
- **Changement de piste** :
  ```kotlin
  mediaController?.seekToNext()      // Chanson suivante
  mediaController?.seekToPrevious()  // Chanson précédente
  ```
- **Barre de progression** :
  ```kotlin
  mediaController?.seekTo(position) // Aller à un temps précis (en millisecondes)
  ```

## 4. Pourquoi asynchrone ?
La séparation entre le service et l'activité permet à la musique de continuer même si l'activité est détruite. Le `MediaController` agit comme une télécommande : il se connecte au service, envoie des ordres, et reçoit des mises à jour sur l'état du lecteur.
