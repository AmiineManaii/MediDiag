# 🛠️ Analyse de MainActivity.kt : Le Chef d'Orchestre

Ce document explique ligne par ligne le fonctionnement du [MainActivity.kt](file:///c:/Users/amine/OneDrive/Bureau/developpement%20android/Native/MusicPlayer/app/src/main/java/com/example/musicplayer/MainActivity.kt). C'est le point d'entrée qui lie l'interface (Compose) au service de musique.

## 📝 Code Expliqué

```kotlin
class MainActivity : ComponentActivity() {

    // --- Variables de contrôle Media3 ---
    private var mediaController: MediaController? = null // La "télécommande" pour parler au service
    private lateinit var controllerFuture: ListenableFuture<MediaController> // Pour la connexion asynchrone

    // --- Listener (Écouteur) du Lecteur ---
    private val playerListener = object : Player.Listener {
        // Appelé quand on fait Play ou Pause
        override fun onIsPlayingChanged(playing: Boolean) {
            viewModel.isPlaying.value = playing
        }

        // Appelé quand la chanson change
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val currentId = mediaItem?.mediaId
            // On cherche la chanson correspondante dans notre liste pour mettre à jour l'UI
            viewModel.nowPlayingSong.value = viewModel.songs.find { it.id.toString() == currentId }
            updateQueueItems() // Met à jour l'affichage de la file d'attente
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Vérifie les permissions avant de commencer
        checkPermissionsAndLoadSongs()

        setContent {
            viewModel = viewModel() // Initialise le ViewModel
            MusicPlayerTheme {
                // Lanceur d'effets pour mettre à jour la barre de progression (Slider)
                PlayerStateUpdater()
                // Affiche le contenu principal (Écrans Compose)
                MainContent()
            }
        }
    }

    // --- Connexion au Service ---
    override fun onStart() {
        super.onStart()
        // 1. On crée un jeton pour identifier notre service
        val sessionToken = SessionToken(this, ComponentName(this, MusicService::class.java))
        
        // 2. On demande à créer une "télécommande" (MediaController)
        controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        
        // 3. On attend que la connexion soit établie
        controllerFuture.addListener({
            mediaController = controllerFuture.get() // On récupère enfin la télécommande
            mediaController?.addListener(playerListener) // On branche nos écouteurs
            updateUiWithCurrentState() // On synchronise l'UI avec l'état actuel du service
        }, ContextCompat.getMainExecutor(this))
    }

    // --- Logique de Lecture ---
    private fun playLocalSong(index: Int) {
        mediaController?.let { controller ->
            // On donne toute la liste des chansons au contrôleur
            val mediaItems = viewModel.songs.map { it.toMediaItem() }
            controller.setMediaItems(mediaItems, index, 0L) // On commence à l'index cliqué
            controller.prepare() // Prépare le moteur audio
            controller.play() // Lance la musique !
        }
    }

    private fun cycleRepeatMode() {
        // Alterne entre : Désactivé -> Tout répéter -> Répéter 1 titre
        val newMode = when (mediaController?.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        mediaController?.repeatMode = newMode // Applique le changement au service
    }

    // --- Nettoyage ---
    override fun onStop() {
        super.onStop()
        mediaController?.removeListener(playerListener) // Débranche les écouteurs
        MediaController.releaseFuture(controllerFuture) // Libère la connexion pour économiser la RAM
    }
}
```

## 💡 Concepts Clés

- **MediaController** : C'est l'objet le plus important. C'est lui qui envoie les ordres (Play, Pause, Suivant) au `MusicService` qui tourne en arrière-plan.
- **ListenableFuture** : La connexion au service n'est pas instantanée. On utilise cet objet pour dire "préviens-moi quand tu as fini de te connecter".
- **Player.Listener** : C'est le lien de retour. Quand le service change de chanson de lui-même (fin de piste), il prévient l'activité via cet écouteur pour que l'écran se mette à jour.
- **onStart / onStop** : On se connecte quand l'appli devient visible, et on se déconnecte quand elle disparaît. C'est essentiel pour la gestion de la mémoire.
