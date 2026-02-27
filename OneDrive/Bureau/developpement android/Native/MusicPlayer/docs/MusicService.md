# Service de Musique (MusicService)

Le `MusicService` est la partie de l'application qui ne s'arrête jamais. Elle utilise Media3 pour gérer le son et la notification système.

## 1. Initialisation (`onCreate`)

C'est ici que tout commence. On prépare le lecteur et on le lie à une session.

```kotlin
override fun onCreate() {
    super.onCreate()
    // 1. On crée l'instance du lecteur ExoPlayer
    val player = ExoPlayer.Builder(this).build()

    // 2. On prépare l'action à faire quand on clique sur la notification
    val sessionActivityPendingIntent = PendingIntent.getActivity(
        this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
    )

    // 3. On crée la MediaSession qui fait le pont entre le lecteur et Android
    mediaSession = MediaSession.Builder(this, player)
        .setSessionActivity(sessionActivityPendingIntent)
        .build()
}
```

## 2. Connexion avec l'UI (`onGetSession`)

Cette fonction est appelée quand `MainActivity` essaie de se connecter.

```kotlin
override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
    // On renvoie notre session au controller qui la demande
    return mediaSession
}
```

## 3. Gestion de la fermeture (`onTaskRemoved`)

C'est une fonction cruciale pour la batterie et l'expérience utilisateur.

```kotlin
override fun onTaskRemoved(rootIntent: Intent?) {
    val player = mediaSession?.player!!
    // Si la musique ne joue pas ou si la playlist est vide
    if (!player.playWhenReady || player.mediaItemCount == 0) {
        // On arrête totalement le service pour libérer la mémoire
        stopSelf()
    }
}
```
*Note : Si la musique joue, on ne fait rien, ce qui permet à la musique de continuer même si vous fermez l'application (swipe).*

## 4. Libération des ressources (`onDestroy`)

```kotlin
override fun onDestroy() {
    mediaSession?.run {
        player.release() // On arrête le moteur audio
        release()        // On ferme la session
        mediaSession = null
    }
    super.onDestroy()
}
```

## Pourquoi utiliser MediaSessionService ?
- **Notification Automatique** : Media3 crée et gère pour vous la notification avec les boutons Play/Pause/Suivant.
- **Support Bluetooth/Casque** : Les boutons de votre casque bluetooth fonctionneront automatiquement car ils communiquent avec la `MediaSession`.
- **Économie d'Énergie** : Android sait que ce service est important et ne le fermera pas tant que la musique joue.

---

## Annexe : MusicService2 (Version Service simple)

Il existe une variante appelée `MusicService2` qui hérite de `android.app.Service` au lieu de `MediaSessionService`.

### Différences majeures :
1. **onBind** : Contrairement à la version 1, vous devez implémenter `onBind` pour permettre la communication.
2. **Gestion Manuelle** : La version 1 est spécifiquement optimisée par Google pour Media3. La version 2 est plus "générique" et demande plus de code manuel pour gérer les notifications et les connexions `MediaController`.
3. **Pédagogie** : La version 2 est utile pour comprendre comment intégrer `MediaSession` dans un service Android standard pré-existant.
