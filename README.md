# GymMonitor - Aplicație Android pentru monitorizarea antrenamentelor (Java)

![Java](https://img.shields.io/badge/Java-11-orange)
![Android](https://img.shields.io/badge/Android-minSdk%2024-green)
![SQLite](https://img.shields.io/badge/Database-SQLite-blue)
![Status](https://img.shields.io/badge/Project-Completed-brightgreen)

GymMonitor este o aplicație Android realizată în **Java**, cu **Android Studio**, pentru urmărirea antrenamentelor de sală. Aplicația permite:

- **înregistrarea antrenamentelor** (exerciții, greutăți, repetări, serii)
- **urmărirea progresului** la greutatea corporală și la fiecare exercițiu, cu grafice
- **calculul automat al necesarului caloric zilnic**, pe baza profilului utilizatorului
- **planificarea antrenamentelor** într-un calendar, cu notificări programate

Toate datele sunt stocate local, pe telefon, într-o bază de date SQLite, deci aplicația funcționează fără internet.

Proiect realizat în echipă, la materia **Programare Java** (UNSTPB, Facultatea de Inginerie Industrială și Robotică, grupa 631AB, FIIR - SIA).

---

## Funcționalități

- **Profil utilizator**
  - Înregistrare la prima deschidere (nume, data nașterii, sex, înălțime, greutate, nivel de activitate, obiectiv)
  - Același ecran devine ecran de setări după crearea contului
- **Meniu principal** cu acces la Antrenament, Progres, Calendar și Unelte, plus ținta calorică afișată direct pe ecran
- **Calcul caloric personalizat**
  - BMR cu ecuația **Mifflin-St Jeor**
  - Factor de activitate: Sedentar, Ușor activ, Moderat, Foarte activ
  - Ajustare pe obiectiv: slăbire (−500 kcal), menținere, masă musculară (+300 kcal)
  - Folosește cea mai recentă greutate din istoricul de progres
- **Antrenament**
  - Selecția grupei musculare și a exercițiului (Piept, Spate, Picioare, Umeri, Biceps, Triceps, Abdomen, plus exerciții personalizate)
  - Butoane +/− pentru greutate (pas de 2,5 kg) și repetări
  - Exercițiile repetate se grupează într-un singur card, cu mai multe serii
- **Progres**
  - **Greutate corporală**: grafic al evoluției și listă de înregistrări, cu opțiune de ștergere
  - **Exerciții**: istoricul fiecărui exercițiu, afișat într-un grafic
- **Calendar**: vizualizarea antrenamentelor făcute, notițe pentru fiecare zi și planificarea celor viitoare
- **Alarme**: notificări programate la o oră aleasă, care funcționează și cu aplicația închisă
- **Unelte**: cronometru și countdown pentru pauzele dintre serii

---

## Capturi de ecran - folderul screenshots

1. `1_Profil.png` – Crearea profilului
2. `2_Meniu_Principal.png` – Meniul principal cu ținta calorică
3. `3_Antrenament.png` – Adăugarea exercițiilor și a seriilor
4. `4_Progres_Greutate.png` – Graficul greutății corporale
5. `5_Progres_Exercitii.png` – Graficul unui exercițiu
6. `6_Calendar.png` – Calendarul și notițele
7. `7_Unelte.png` – Cronometru și countdown

---

## Tehnologii utilizate

- **Java 11**
- **Android SDK** (minSdk 24, targetSdk 36), construit cu Gradle
- **SQLite** (`SQLiteOpenHelper`) pentru stocarea datelor
- **MPAndroidChart 3.1.0** pentru grafice
- **Material Components** și **ConstraintLayout** pentru interfață
- **AlarmManager** și **BroadcastReceiver** pentru notificări programate

---

## Arhitectura aplicației

| Clasă | Rol |
|---|---|
| `MainActivity` | Meniul principal; calculează și afișează ținta calorică |
| `ProfileActivity` | Înregistrare și setări (comportament dual, în funcție de context) |
| `AntrenamentActivity` | Introducerea exercițiilor, greutăților și repetărilor |
| `ProgressMenuActivity` | Alegerea între progresul greutății și al exercițiilor |
| `ProgressActivity` | Grafic și listă pentru greutatea corporală |
| `ExerciseProgressActivity` | Grafic pentru exercițiul ales (două spinnere dependente) |
| `CalendarActivity` | Calendar, notițe pe zile și programarea alarmelor |
| `ClockActivity` | Cronometru (cu reluare din punctul de oprire) și countdown |
| `AlarmReceiver` | Afișează notificarea la ora programată; creează canalul de notificări |
| `DatabaseHelper` | Schema SQLite și toate operațiile asupra datelor |

### Baza de date (`GymMonitor.db`)

| Tabel | Conținut |
|---|---|
| `profile_table` | date personale, sex, înălțime, greutate, nivel de activitate, obiectiv |
| `workouts_table` | notițe de antrenament pe zile (calendar) |
| `sessions_table` | sesiunile de antrenament (nume, dată) |
| `exercises_log_table` | exercițiile unei sesiuni (grupă musculară, nume, kg, repetări) |
| `weight_log_table` | istoricul greutății corporale |

---

## Instalare și rulare

1. Clonează repo-ul:
   ```bash
   git clone https://github.com/DanielxManole/GymMonitor.git
   ```
2. Deschide folderul în **Android Studio** (versiune recentă, cu suport pentru AGP 8.13).
3. Așteaptă sincronizarea Gradle.
4. Rulează aplicația pe un emulator sau pe un telefon cu **Android 7.0 (API 24)** sau mai nou.

Pe Android 13+, notificările necesită permisiunea `POST_NOTIFICATIONS`, pe care aplicația o verifică înainte de a afișa o alarmă.

---

## Structura proiectului

```
GymMonitor/
├─ app/
│   ├─ build.gradle
│   └─ src/main/
│       ├─ AndroidManifest.xml
│       ├─ java/com/example/gymmonitor/
│       │   ├─ MainActivity.java
│       │   ├─ ProfileActivity.java
│       │   ├─ AntrenamentActivity.java
│       │   ├─ ProgressMenuActivity.java
│       │   ├─ ProgressActivity.java
│       │   ├─ ExerciseProgressActivity.java
│       │   ├─ CalendarActivity.java
│       │   ├─ ClockActivity.java
│       │   ├─ AlarmReceiver.java
│       │   └─ DatabaseHelper.java
│       └─ res/                 # layout-uri, teme, culori, iconițe
├─ gradle/
├─ build.gradle
├─ settings.gradle
├─ gradle.properties
├─ gradlew
├─ gradlew.bat
├─ docs/
│   └─ GymMonitor-Prezentare.pptx
├─ screenshots/
│   ├─ 1_Profil.png
│   ├─ 2_Meniu_Principal.png
│   ├─ 3_Antrenament.png
│   ├─ 4_Progres_Greutate.png
│   ├─ 5_Progres_Exercitii.png
│   ├─ 6_Calendar.png
│   └─ 7_Unelte.png
├─ README.md
├─ LICENSE
└─ .gitignore
```

---

## Limitări cunoscute

- Parola din profil este salvată în baza de date **în text simplu**. Aplicația este locală și are un singur profil, dar pentru o versiune publică parola ar trebui stocată ca hash.
- La schimbarea versiunii bazei de date, `onUpgrade` șterge tabelele existente, deci datele se pierd. O versiune de producție ar avea migrări.
- Aplicația suportă un singur profil pe dispozitiv și nu are sincronizare în cloud.

---

## Echipa

Proiect realizat de studenții grupei 631AB, FIIR - SIA:

- **Odoroagă Vlad-Ionuț**: profil și meniul principal
- **Manole Daniel**: antrenamente și progres (exerciții și greutate corporală)
- **Pralea Bogdan-Ștefan**: calendar, alarme și unelte

---

## Licență

Acest proiect este distribuit sub licența MIT.

---

## Contact

Pentru întrebări sau sugestii:
- manoledaniel2004@gmail.com
- https://www.linkedin.com/in/manoledaniel/
