# 🔍 MediaStore et Chargement de Fichiers : Mécanique du Stockage Local

L'application utilise l'API **MediaStore** d'Android pour scanner le stockage de l'appareil à la recherche de fichiers audio. Cela nous permet de fournir une bibliothèque musicale locale complète à l'utilisateur.

## 🏗️ Définition Technique

La classe [MusicViewModel](file:///c:/Users/amine/OneDrive/Bureau/developpement%20android/Native/MusicPlayer/app/src/main/java/com/example/musicplayer/MusicViewModel.kt) contient la fonction `loadSongs()`, qui effectue une requête sur le `ContentResolver`.

```kotlin
val projection = arrayOf(
    MediaStore.Audio.Media._ID,
    MediaStore.Audio.Media.DISPLAY_NAME,
    MediaStore.Audio.Media.ARTIST,
    MediaStore.Audio.Media.ALBUM_ID,
    MediaStore.Audio.Media.DATA
)
```

---

## 🛠️ Exemples de Code et Commentaires Détaillés

### Exemple 1 : Requête MediaStore de Base
La manière fondamentale de requêter des fichiers audio depuis l'appareil.

```kotlin
val cursor = contentResolver.query(
    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
    projection,
    null, // Sélection (clause WHERE)
    null, // Arguments de sélection
    "${MediaStore.Audio.Media.TITLE} ASC" // Ordre de tri
)
```
**Analyse ligne par ligne :**
- `MediaStore.Audio.Media.EXTERNAL_CONTENT_URI` : La table de base de données cible pour tous les fichiers audio sur l'appareil.
- `projection` : La liste des colonnes que nous voulons récupérer (IDs, titres, artistes).
- Sélection `null` : Nous voulons *tous* les fichiers. Dans notre application, nous filtrons par répertoire plus tard dans la boucle.
- `ASC` : Trie les résultats par ordre alphabétique du titre de la piste.

### Exemple 2 : Filtrage par Répertoire
Comment restreindre les résultats à un dossier spécifique (par exemple, le dossier `Download`).

```kotlin
val selection = "${MediaStore.Audio.Media.DATA} LIKE ?"
val selectionArgs = arrayOf("%/Download/%")

val cursor = contentResolver.query(
    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
    projection,
    selection,
    selectionArgs,
    null
)
```
**Analyse ligne par ligne :**
- `MediaStore.Audio.Media.DATA` : Cette colonne contient le chemin complet du fichier sur le disque (ex: `/storage/emulated/0/Download/chanson.mp3`).
- `LIKE ?` : Un opérateur SQL qui permet la recherche de motifs (pattern matching).
- `%/Download/%` : Le `%` est un joker (wildcard), signifiant que nous voulons tout chemin contenant le mot "Download".

### Exemple 3 : Filetage et Coroutines (`Dispatchers.IO`)
Exécution de l'opération de scan lourde sur un thread d'arrière-plan.

```kotlin
viewModelScope.launch(Dispatchers.IO) {
    val loadedSongs = mutableListOf<Song>()
    // ... code de requête ...
    withContext(Dispatchers.Main) {
        songs.clear()
        songs.addAll(loadedSongs)
    }
}
```
**Analyse ligne par ligne :**
- `Dispatchers.IO` : Optimisé pour les opérations sur disque/réseau. Il empêche l'interface utilisateur de se figer pendant que le téléphone scanne des milliers de fichiers.
- `withContext(Dispatchers.Main)` : Repasse sur le thread UI pour mettre à jour la liste `songs` en toute sécurité. Dans Compose, les mises à jour de l'UI *doivent* se faire sur le thread principal.

---

## ⚠️ Pièges Courants et Considérations de Performance

- **Gestion du Cursor** : Utilisez toujours `.use { ... }` ou `.close()` sur un `Cursor` pour éviter les fuites de mémoire.
- **Grandes Bibliothèques** : Si un utilisateur possède plus de 10 000 chansons, le scan peut prendre plusieurs secondes. Envisagez d'implémenter un cache ou un chargement paresseux.
- **Permission Refusée** : Si l'application n'a pas les permissions de stockage, la fonction `query()` retournera `null` ou lèvera une exception. Vérifiez toujours les permissions avant de scanner.

## 🛠️ Dépannage
- **Chansons n'apparaissant pas** : Vérifiez si les fichiers sont réellement dans le dossier `Download`. Sur certains appareils, le chemin peut être différent (ex: `/sdcard/Download/`).
- **Doublons** : Certains appareils signalent le même fichier deux fois (stockage interne vs externe). Utilisez l' `_ID` pour filtrer les doublons si nécessaire.
- **Métadonnées manquantes** : Tous les MP3 n'ont pas de métadonnées internes (tags ID3). Si `artist` est nul, utilisez une valeur de secours comme `"Artiste inconnu"`.
