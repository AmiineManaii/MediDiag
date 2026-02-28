# 🛠️ Analyse de MusicViewModel.kt : La Logique Métier

Ce document explique ligne par ligne le fonctionnement du [MusicViewModel.kt](file:///c:/Users/amine/OneDrive/Bureau/developpement%20android/Native/MusicPlayer/app/src/main/java/com/example/musicplayer/MusicViewModel.kt). Ce fichier est le cerveau de l'application, gérant les données et l'état.

## 📝 Code Expliqué

```kotlin
class MusicViewModel(application: Application) : AndroidViewModel(application) {

    // --- États Observables (Compose) ---
    // mutableStateListOf permet à Compose de détecter l'ajout/suppression d'éléments
    val songs = mutableStateListOf<Song>() // Liste de toutes les chansons locales
    val playlists = mutableStateListOf<Playlist>() // Liste des playlists créées par l'utilisateur
    
    // mutableStateOf permet à Compose de détecter le changement de valeur
    val nowPlayingSong = mutableStateOf<Song?>(null) // Chanson actuellement en lecture
    val isPlaying = mutableStateOf(false) // État de lecture (joue ou en pause)
    val currentPosition = mutableStateOf(0L) // Position actuelle dans la chanson (ms)
    val duration = mutableStateOf(0L) // Durée totale de la chanson (ms)
    
    // États pour la navigation et l'interface
    val currentScreen = mutableStateOf(Screen.HOME) // Écran actuellement affiché
    val showDetailScreen = mutableStateOf(false) // Afficher ou non l'écran de détails plein écran

    // --- Persistance avec GSON ---
    // GSON ne sait pas gérer le type Uri par défaut, on lui apprend comment faire
    private val gson = GsonBuilder()
        .registerTypeAdapter(Uri::class.java, object : JsonSerializer<Uri>, JsonDeserializer<Uri> {
            override fun serialize(src: Uri?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
                return JsonPrimitive(src.toString()) // Transforme Uri en String pour JSON
            }
            override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Uri {
                return Uri.parse(json?.asString) // Transforme String en Uri depuis JSON
            }
        })
        .create()

    init {
        loadPlaylists() // Charge les playlists au démarrage du ViewModel
    }

    // --- Gestion des Chansons (MediaStore) ---
    fun loadSongs(forceRefresh: Boolean = false) {
        if (songs.isNotEmpty() && !forceRefresh) return // Évite de recharger pour rien

        // Lance une coroutine sur un thread IO (optimisé pour le disque)
        viewModelScope.launch(Dispatchers.IO) {
            val projection = arrayOf(...) // Définit les colonnes à lire dans la base Android
            
            // Requête sur le stockage externe d'Android
            contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                "${MediaStore.Audio.Media.DATA} LIKE ?", // Filtre par dossier
                arrayOf("%/Download/%"), // On ne cherche que dans /Download
                "${MediaStore.Audio.Media.TITLE} ASC" // Tri alphabétique
            )?.use { cursor ->
                // Boucle sur les résultats du scan
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(...) // Récupère l'ID unique
                    val title = cursor.getString(...) // Récupère le titre
                    
                    // Création de l'objet Song avec son URI
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id
                    )
                    
                    // Ajout à la liste temporaire
                    loadedSongs.add(Song(id, title, artist, contentUri, albumArtUri))
                }
            }
            
            // Met à jour l'UI sur le thread principal (Main)
            withContext(Dispatchers.Main) {
                songs.clear()
                songs.addAll(loadedSongs)
            }
        }
    }

    // --- Gestion des Playlists ---
    fun savePlaylists() {
        // Transforme la liste en JSON
        val json = gson.toJson(playlists.toList())
        // Sauvegarde dans les SharedPreferences (mémoire permanente légère)
        sharedPreferences.edit().putString("playlists", json).apply()
    }

    fun addSongToPlaylist(playlist: Playlist, song: Song) {
        val index = playlists.indexOf(playlist)
        if (index != -1) {
            val updatedSongs = playlists[index].songs.toMutableList()
            // Vérifie si la chanson n'est pas déjà présente
            if (!updatedSongs.any { it.id == song.id }) {
                updatedSongs.add(song)
                // Met à jour la liste (déclenche la recomposition Compose)
                playlists[index] = playlists[index].copy(songs = updatedSongs)
                savePlaylists() // Sauvegarde les changements
            }
        }
    }
}
```

## 💡 Concepts Clés

- **viewModelScope** : Une portée de coroutine qui s'arrête automatiquement quand l'écran est détruit. Cela évite les fuites de mémoire.
- **Dispatchers.IO** : Indispensable pour ne pas bloquer l'écran pendant que le téléphone cherche des fichiers ou enregistre des données.
- **MutableState** : Le moteur de Jetpack Compose. Quand une de ces variables change, l'écran se met à jour tout seul.
- **SharedPreferences** : Un moyen simple de stocker des données (comme vos playlists) pour qu'elles restent là même après avoir éteint le téléphone.
