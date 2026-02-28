# 📖 Référence API : Melody Music Player

Le projet **Melody Music Player** utilise une API modulaire pour sa logique interne et la recherche de pistes externes. Ce guide documente les classes clés, les méthodes et les points de terminaison réseau.

## 📡 API Distante (Deezer)

L'application s'intègre à l'API **Deezer** via [RetrofitInstance.kt](file:///c:/Users/amine/OneDrive/Bureau/developpement%20android/Native/MusicPlayer/app/src/main/java/com/example/musicplayer/api/RetrofitInstance.kt).

### 🔍 Recherche de Pistes
- **Point de terminaison** : `GET /search?q={query}`
- **Réponse** : Une liste d'objets `DeezerTrack`.
- **Usage** :
  ```kotlin
  val response = RetrofitInstance.deezerService.searchTracks(query = "Daft Punk")
  val results: List<DeezerTrack> = response.data
  ```

### 📦 Modèles de Données
- **`DeezerTrack`** : Représente une seule chanson de l'API.
  - `id: Long` : ID unique de la piste.
  - `title: String` : Nom de la piste.
  - `preview: String` : URL HTTP pour un extrait audio de 30 secondes.
  - `artist: DeezerArtist` : L'interprète.
  - `album: DeezerAlbum` : Informations sur l'album et pochette.

---

## 🛠️ API Interne du ViewModel

Le [MusicViewModel](file:///c:/Users/amine/OneDrive/Bureau/developpement%20android/Native/MusicPlayer/app/src/main/java/com/example/musicplayer/MusicViewModel.kt) fournit la logique métier principale et la gestion d'état.

### 🎵 Chargement de Chansons
- **`loadSongs(forceRefresh: Boolean)`** : Scanne le stockage de l'appareil pour les fichiers audio.
  - **Logique** : Requête `MediaStore.Audio.Media.EXTERNAL_CONTENT_URI`.
  - **Thread** : S'exécute sur `Dispatchers.IO`.

### 📂 Gestion des Playlists
- **`createPlaylist(name: String)`** : Ajoute une nouvelle playlist vide.
- **`deletePlaylist(playlist: Playlist)`** : Supprime une playlist du stockage.
- **`addSongToPlaylist(playlist: Playlist, song: Song)`** : Ajoute une piste (locale ou distante) à une playlist.
- **`removeSongFromPlaylist(playlist: Playlist, song: Song)`** : Supprime une piste d'une playlist.

### 🔋 État de Lecture
- **`nowPlayingSong: MutableState<Song?>`** : La piste en cours de lecture.
- **`isPlaying: MutableState<Boolean>`** : Statut de lecture (vrai/faux).
- **`currentPosition: MutableState<Long>`** : Progression de la lecture en millisecondes.
- **`duration: MutableState<Long>`** : Durée totale de la piste actuelle.

---

## 🎧 Commandes Media3 (ExoPlayer)

L'application utilise le `MediaController` pour envoyer des commandes au [MusicService](file:///c:/Users/amine/OneDrive/Bureau/developpement%20android/Native/MusicPlayer/app/src/main/java/com/example/musicplayer/service/MusicService.kt).

### ⏯️ Contrôles de Lecture
- **`mediaController?.play()`** : Reprend la lecture.
- **`mediaController?.pause()`** : Suspend la lecture.
- **`mediaController?.seekTo(position: Long)`** : Saute à un moment précis.
- **`mediaController?.seekToNext()`** : Passe à la chanson suivante dans la file d'attente.
- **`mediaController?.seekToPrevious()`** : Revient à la chanson précédente.

### 🎛️ Modes de Lecture
- **`mediaController?.shuffleModeEnabled = true`** : Active la lecture aléatoire.
- **`mediaController?.repeatMode = Player.REPEAT_MODE_ALL`** : Définit le mode de répétition (Désactivé, Un, Tous).

---

## 🛠️ Sérialisation des Données (GSON)

Les playlists sont sauvegardées sous forme de JSON dans les `SharedPreferences`.

### 🧪 TypeAdapter Personnalisé pour `Uri`
Comme `Uri` n'est pas un type primitif, nous utilisons un adaptateur personnalisé dans [MusicViewModel](file:///c:/Users/amine/OneDrive/Bureau/developpement%20android/Native/MusicPlayer/app/src/main/java/com/example/musicplayer/MusicViewModel.kt) :
```kotlin
private val gson = GsonBuilder()
    .registerTypeAdapter(Uri::class.java, object : JsonSerializer<Uri>, JsonDeserializer<Uri> {
        override fun serialize(src: Uri?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
            return JsonPrimitive(src.toString())
        }
        override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Uri {
            return Uri.parse(json?.asString)
        }
    })
    .create()
```

Pour plus de détails sur l'implémentation, voir la [Plongée Technique](file:///c:/Users/amine/OneDrive/Bureau/developpement%20android/Native/MusicPlayer/docs/README.md).
