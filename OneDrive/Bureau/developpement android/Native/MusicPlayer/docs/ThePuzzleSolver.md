# 🧩 Dépannage et Optimisation : Le Solveur de Puzzles

Ce guide est conçu pour aider les développeurs à identifier et résoudre les problèmes techniques courants tout en maintenant une performance optimale dans le **Melody Music Player**.

## 🏗️ Définition Technique

Nous catégorisons les problèmes en trois groupes : **Erreurs de Lecture**, **Synchronisation de l'UI** et **Intégrité des Données**.

---

## 🛠️ Exemples de Code et Commentaires Détaillés

### Exemple 1 : Gestion des Erreurs de Lecture
Capture et journalisation des exceptions ExoPlayer.

```kotlin
player.addListener(object : Player.Listener {
    override fun onPlayerError(error: PlaybackException) {
        val message = when (error.errorCode) {
            PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND -> "Le fichier est introuvable."
            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED -> "Pas de connexion Internet."
            else -> "Erreur de lecture : ${error.message}"
        }
        Log.e("MusicPlayer", message)
    }
})
```
**Analyse ligne par ligne :**
- `PlaybackException` : La classe d'erreur spécialisée pour Media3.
- `error.errorCode` : Fournit des codes spécifiques pour différencier un fichier manquant d'une panne réseau.
- `Log.e` : Indispensable pour le débogage pendant le développement.

### Exemple 2 : Mises à jour de l'UI Efficaces (Recomposition)
Optimisation des performances de Jetpack Compose.

```kotlin
@Composable
fun SongItem(song: Song, isSelected: Boolean) {
    val backgroundColor = if (isSelected) 
        MaterialTheme.colorScheme.primaryContainer 
    else 
        Color.Transparent

    Card(colors = CardDefaults.cardColors(containerColor = backgroundColor)) {
        // Code UI
    }
}
```
**Analyse ligne par ligne :**
- `isSelected` : En ne passant que les valeurs primitives nécessaires (ou des objets stables) à un Composable, nous garantissons qu'il ne se redessine (recompose) que lorsque cette valeur spécifique change.
- `Color.Transparent` : L'utilisation de couleurs fournies par le système garantit la compatibilité avec les modes sombre/clair.

### Exemple 3 : Débogage des Connexions MediaController
Vérification du lien entre l'UI et le service.

```kotlin
controllerFuture.addListener({
    try {
        mediaController = controllerFuture.get()
        Log.d("Connection", "MediaController connecté avec succès")
    } catch (e: ExecutionException) {
        Log.e("Connection", "Échec de la connexion : ${e.message}")
    }
}, ContextCompat.getMainExecutor(this))
```
**Analyse ligne par ligne :**
- `ExecutionException` : Ceci est lancé si le service ne parvient pas à démarrer ou si la déclaration du manifeste est incorrecte.
- `Log.d` : Journalisez toujours les connexions réussies pour confirmer que le service d'arrière-plan est actif.

---

## ⚠️ Pièges Courants et Considérations de Performance

- **Mode Strict** : Android peut arrêter votre application si vous effectuez des opérations sur disque (comme scanner des chansons) sur le thread principal. Utilisez toujours `Dispatchers.IO`.
- **Mémoire des Bitmaps** : Charger des pochettes d'album haute résolution en mémoire peut provoquer une `OutOfMemoryError`. Utilisez toujours une bibliothèque comme **Coil** avec mise en cache disque et mémoire.
- **Sérialisation** : Lors de l'enregistrement des playlists, assurez-vous que tous les champs (en particulier `Uri`) sont sérialisables. Dans notre application, nous utilisons un `TypeAdapter` GSON personnalisé pour cela.

## 🛠️ Dépannage
- **Aucune chanson trouvée** : Vérifiez que la permission de stockage est accordée et que les fichiers sont dans le bon répertoire.
- **Sauts ou lags audio** : Cela peut arriver si l' `ExoPlayer` manque de mémoire. Assurez-vous de ne pas effectuer de calculs lourds sur le même thread.
- **Les boutons de notification ne fonctionnent pas** : Cela signifie généralement que la `MediaSession` a été libérée prématurément ou que le `MediaController` a perdu sa connexion.
