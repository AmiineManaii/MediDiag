# Configuration du Manifest (AndroidManifest.xml)

Le fichier `AndroidManifest.xml` est la carte d'identité de votre application. Voici l'explication détaillée de sa configuration pour la musique.

## 1. Permissions de Stockage (Le pourquoi du comment)

```xml
<uses-permission android:name="android.permission.READ_MEDIA_AUDIO" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" android:maxSdkVersion="32" />
```
- **READ_MEDIA_AUDIO** : Depuis Android 13, Google a rendu les permissions plus précises. On ne demande plus l'accès à "tous les fichiers", mais seulement aux "fichiers audio".
- **READ_EXTERNAL_STORAGE** : Est conservé pour les anciens téléphones (Android 12 et moins). Le `maxSdkVersion="32"` dit à Android d'ignorer cette ligne sur les versions récentes.

## 2. Permissions Foreground Service

```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK"/>
```
- **FOREGROUND_SERVICE** : Permission de base pour lancer un service persistant.
- **FOREGROUND_SERVICE_MEDIA_PLAYBACK** : **CRITIQUE**. Depuis Android 14, vous devez déclarer *pourquoi* votre service tourne en arrière-plan. Si vous oubliez cette ligne, l'application crashera immédiatement au lancement de la musique.

## 3. Déclaration du Service (Lien avec Media3)

```xml
<service
    android:name=".service.MusicService"
    android:exported="false"
    android:foregroundServiceType="mediaPlayback">
    <intent-filter>
        <action android:name="androidx.media3.session.MediaSessionService"/>
    </intent-filter>
</service>
```

### Explication des attributs :
- **`android:name`** : Le chemin vers votre fichier Kotlin. Le point `.` devant signifie "dans le package de l'application".
- **`android:exported="false"`** : Signifie que seules votre propre application peut utiliser ce service. C'est une mesure de sécurité.
- **`foregroundServiceType="mediaPlayback"`** : Confirme au système que le service appartient à la catégorie "Musique".
- **`intent-filter`** : C'est le "numéro de téléphone" du service. Quand vous utilisez `MediaController` dans le code, Media3 cherche un service qui a exactement l'action `androidx.media3.session.MediaSessionService`.

## 4. Permission des Notifications
```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```
Sans cette permission, le lecteur de musique fonctionnera, mais l'utilisateur ne verra jamais la barre de contrôle (Play/Pause) dans son centre de notifications sur Android 13+.
