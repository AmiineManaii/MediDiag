# 📖 Modèle de Données et Métadonnées : L'Entité `Song`

La classe `Song` est le modèle de données fondamental pour toute l'application. Elle encapsule toutes les informations nécessaires sur une piste audio, qu'elle soit stockée localement sur l'appareil ou récupérée depuis une API distante.

## 🏗️ Définition Technique

Le fichier [Song.kt](file:///c:/Users/amine/OneDrive/Bureau/developpement%20android/Native/MusicPlayer/app/src/main/java/com/example/musicplayer/model/Song.kt) définit une `data class` Kotlin. Cela garantit que l'objet fournit des méthodes intégrées pour la gestion des données, telles que `equals()`, `hashCode()` et `toString()`.

```kotlin
data class Song(
    val id: Long,               // Identifiant unique provenant de MediaStore ou de l'API
    val title: String,          // Nom affichable de la piste
    val artist: String,         // Nom affichable de l'interprète
    val uri: Uri,               // URI de contenu (local) ou URL de flux (distant)
    val albumArtUri: Uri? = null // URI vers l'image de la pochette
)
```

---

## 🛠️ Exemples de Code et Commentaires Détaillés

### Exemple 1 : Initialisation depuis MediaStore (Local)
Comment une chanson locale est créée à partir des données d'un curseur MediaStore.

```kotlin
val id = cursor.getLong(idColumn)
val title = cursor.getString(titleColumn)
val artist = cursor.getString(artistColumn)
val contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)

val localSong = Song(
    id = id,
    title = title,
    artist = artist,
    uri = contentUri,
    albumArtUri = null
)
```
**Analyse ligne par ligne :**
- `idColumn` : L'index de la colonne `_ID` dans la requête MediaStore.
- `ContentUris.withAppendedId` : Construit une URI `content://` unique pour le fichier, permettant à d'autres applications (comme le lecteur système) d'accéder au fichier en toute sécurité.
- `uri = contentUri` : La partie la plus critique ; c'est ce que Media3 utilisera pour charger les octets audio réels.

### Exemple 2 : Intégration de Titres API (Distant)
Création d'un objet `Song` à partir d'une réponse d'API distante (ex: Deezer).

```kotlin
val remoteSong = Song(
    id = track.id,
    title = track.title,
    artist = track.artist.name,
    uri = Uri.parse(track.preview), // URL de flux HTTP distant
    albumArtUri = Uri.parse(track.album.coverMedium)
)
```
**Analyse ligne par ligne :**
- `Uri.parse(track.preview)` : Convertit une URL sous forme de chaîne simple (provenant de l'API) en un objet `Uri` que Media3 peut comprendre comme un flux réseau.
- `track.album.coverMedium` : Fournit l'URL de la pochette, qui sera chargée de manière asynchrone par Coil dans l'interface utilisateur.

### Exemple 3 : Conversion Media3 (`toMediaItem`)
Conversion de notre modèle personnalisé en l'objet standard `MediaItem` de Media3.

```kotlin
fun toMediaItem(): MediaItem {
    val metadata = MediaMetadata.Builder()
        .setTitle(title)
        .setArtist(artist)
        .setArtworkUri(albumArtUri)
        .build()
        
    return MediaItem.Builder()
        .setUri(uri)
        .setMediaId(id.toString())
        .setMediaMetadata(metadata)
        .build()
}
```
**Analyse ligne par ligne :**
- `MediaMetadata.Builder` : Utilisé pour définir les informations visuelles (titre, artiste, pochette) qui seront affichées dans la notification système Android et sur l'écran de verrouillage.
- `setMediaId` : Crucial pour identifier la piste au sein de la file d'attente du `MediaController`.
- `setUri` : Indique au moteur `ExoPlayer` sous-jacent où récupérer le flux audio (local ou réseau).

---

## ⚠️ Pièges Courants et Considérations de Performance

- **`albumArtUri` Nullable** : Toutes les chansons n'ont pas de pochette. Utilisez toujours une icône de secours dans l'interface utilisateur (ex: `Icons.Default.MusicNote`).
- **Gestion de la Mémoire** : Stocker des milliers d'objets `Song` dans une liste ne pose pas de problème, mais évitez de charger tous les bitmaps `albumArtUri` en même temps. Utilisez une bibliothèque comme **Coil** pour le chargement paresseux (lazy loading).
- **Persistance des URI** : Les identifiants MediaStore locaux peuvent changer si la carte SD est retirée ou si la base de données est effacée. Rafraîchissez toujours votre liste de chansons au démarrage de l'application.

## 🛠️ Dépannage
- **La chanson ne joue pas** : Vérifiez si l' `uri` est valide. Pour les fichiers locaux, vérifiez les permissions de stockage. Pour les fichiers API, vérifiez la connexion Internet.
- **Mauvais titre/artiste** : Assurez-vous que les chaînes `title` et `artist` ne sont pas vides. Si elles le sont, utilisez une valeur de secours comme `"Artiste inconnu"`.
