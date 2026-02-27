# Modèle de Données et Métadonnées

L'application utilise une architecture basée sur les données pour représenter la musique. Voici une explication détaillée du code utilisé.

## 1. La classe `Song` (`Song.kt`)

Cette classe est un `data class` Kotlin, ce qui signifie que le compilateur génère automatiquement des méthodes utiles comme `equals()`, `hashCode()` et `toString()`.

```kotlin
data class Song(
    val id: Long,               // Identifiant unique du fichier dans la base Android
    val title: String,          // Titre de la chanson (ex: "Bohemian Rhapsody")
    val artist: String,         // Nom de l'artiste ou "Artiste inconnu"
    val uri: Uri,               // L'adresse physique du fichier sur le téléphone
    val albumArtUri: Uri? = null // L'adresse de l'image de couverture
)
```

## 2. Conversion vers Media3 (`toMediaItem`)

C'est la partie la plus complexe. Media3 ne comprend pas notre classe `Song`, il a besoin d'un `MediaItem`.

```kotlin
fun toMediaItem(): MediaItem {
    // Étape A : Créer les métadonnées affichables
    val metadata = MediaMetadata.Builder()
        .setTitle(title)            // Sera affiché en gros dans la notification
        .setArtist(artist)          // Sera affiché sous le titre
        .setArtworkUri(albumArtUri) // L'image qui apparaîtra en fond de notification
        .build()

    // Étape B : Créer l'objet de lecture
    return MediaItem.Builder()
        .setUri(uri)                // Indique au lecteur quel fichier décoder
        .setMediaId(id.toString())  // Permet de retrouver quel morceau joue
        .setMediaMetadata(metadata) // Attache les infos visuelles au morceau
        .build()
}
```

### Pourquoi séparer Uri et MediaMetadata ?
- **`setUri(uri)`** : Est utilisé par le moteur audio (`ExoPlayer`) pour charger les octets du fichier.
- **`setMediaMetadata(metadata)`** : Est utilisé par le système UI (Notifications, Android Auto, Écran de verrouillage) pour l'affichage.

## 3. Importance des Métadonnées
Sans `MediaMetadata`, votre notification de musique serait vide ou afficherait "Inconnu". En remplissant soigneusement ces champs, vous permettez au système Android d'offrir une expérience utilisateur premium (couleurs de notification adaptées à l'image, contrôles à distance, etc.).
