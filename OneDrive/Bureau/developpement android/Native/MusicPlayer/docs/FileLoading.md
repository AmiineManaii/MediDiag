# Chargement des Fichiers (MediaStore)

Ce fichier détaille comment l'application scanne et récupère la musique stockée sur l'appareil en utilisant l'API `MediaStore`.

## 1. La Requête (Query)

Dans `MainActivity.kt`, la fonction `loadSongs()` effectue une requête sur le système de fichiers d'Android.

```kotlin
val projection = arrayOf(
    MediaStore.Audio.Media._ID,          // ID unique du fichier
    MediaStore.Audio.Media.DISPLAY_NAME, // Nom du fichier
    MediaStore.Audio.Media.ARTIST,       // Nom de l'artiste
    MediaStore.Audio.Media.ALBUM_ID,     // ID de l'album (pour l'image par défaut)
    MediaStore.Audio.Media.DATA          // Chemin complet du fichier
)

val cursor = contentResolver.query(
    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, // Où chercher (stockage externe)
    projection,                                  // Quelles colonnes lire
    null,                                        // Filtre (WHERE)
    null,                                        // Valeurs du filtre
    "${MediaStore.Audio.Media.TITLE} ASC"        // Tri par titre (A-Z)
)
```

## 2. Lecture du Cursor

Le `Cursor` est un objet qui permet de parcourir les résultats ligne par ligne.

```kotlin
cursor?.use { c ->
    // On récupère les index des colonnes pour gagner en performance
    val idCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
    val titleCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
    
    while (c.moveToNext()) {
        val id = c.getLong(idCol)
        val title = c.getString(titleCol)
        
        // Création de l'URI du fichier audio
        val contentUri = ContentUris.withAppendedId(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id
        )
        
        // Création de l'URI pour l'image (Pochette individuelle)
        val albumArtUri = ContentUris.withAppendedId(
            Uri.parse("content://media/external/audio/media"), id
        ).buildUpon().appendPath("albumart").build()
        
        // Ajout à notre liste locale
        loadedSongs.add(Song(id, title, artist, contentUri, albumArtUri))
    }
}
```

## 3. Pourquoi `Dispatchers.IO` ?
La recherche de fichiers peut être lente (disque dur lent, des milliers de chansons).
```kotlin
lifecycleScope.launch(Dispatchers.IO) {
    // ... code de chargement ...
    withContext(Dispatchers.Main) {
        // ... mise à jour de l'UI sur le thread principal ...
    }
}
```
`Dispatchers.IO` exécute le code sur un thread secondaire pour éviter que l'écran du téléphone ne se bloque (freeze) pendant le scan.

## 4. Gestion des Images Individuelles
L'URI `content://media/external/audio/media/[ID]/albumart` est une astuce qui permet à la bibliothèque **Coil** d'aller chercher l'image directement à l'intérieur du fichier MP3. Cela garantit que chaque morceau a sa propre image, même si plusieurs chansons sont dans le même dossier ou appartiennent au même album.
