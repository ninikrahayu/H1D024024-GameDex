# GameDex

GameDex adalah aplikasi Android untuk melihat katalog video game. Datanya diambil dari [RAWG Video Games Database API](https://rawg.io/apidocs). Pengguna bisa melihat daftar game, mencari game berdasarkan nama, lalu membuka halaman detail untuk membaca deskripsinya.

Aplikasi ini dibuat untuk tugas mata kuliah Pemrograman Mobile, memakai Kotlin dan Jetpack Compose dengan arsitektur MVVM.

## Fitur

- Daftar game dari API RAWG, lengkap dengan gambar, nama, rating, tanggal rilis, dan genre.
- Pencarian game berdasarkan nama.
- Halaman "All Games" (tombol "See all" di Home) berisi daftar lengkap dengan pencarian sendiri. Daftar dimuat bertahap saat digulir ke bawah.
- Halaman detail berisi gambar, nama, rating, tanggal rilis, genre, dan deskripsi.
- Tampilan khusus saat data sedang dimuat, saat terjadi error (ada tombol "Coba lagi"), dan saat hasil pencarian kosong.

## Teknologi yang Dipakai

| Kebutuhan | Yang dipakai |
|---|---|
| Bahasa | Kotlin 2.2.10 |
| UI | Jetpack Compose + Material Design 3 |
| Networking | Retrofit 2.11.0 + Gson |
| Gambar | Coil 3.3.0 |
| State | ViewModel + StateFlow (lifecycle 2.11.0) |
| Min SDK / Target SDK | 29 / 37 |

## Arsitektur

Aplikasi memakai pola MVVM. Alur datanya satu arah:

```
RAWG API -> RawgApiService (Retrofit) -> GameRepository -> GameViewModel (StateFlow) -> Composable
```

Event dari pengguna (mengetik, menekan kartu game) mengalir ke arah sebaliknya, dari Composable ke ViewModel.

### Struktur folder

```
com.pemmob.gamedex
├── MainActivity.kt
├── data
│   ├── model
│   │   └── GameDtos.kt          DTO dari API + fungsi toGame()
│   ├── network
│   │   ├── RawgApiService.kt    interface Retrofit
│   │   └── RetrofitClient.kt    pembuatan instance Retrofit
│   └── repository
│       └── GameRepository.kt
├── model
│   └── Game.kt                  model yang dipakai UI
└── ui
    ├── GameViewModel.kt
    ├── all
    │   └── AllGamesScreen.kt
    ├── components
    │   └── GenreChip.kt
    ├── home
    │   └── HomeScreen.kt
    ├── detail
    │   └── DetailScreen.kt
    └── theme
        ├── Color.kt
        ├── Type.kt
        └── Theme.kt
```

## Penjelasan Teknis

### 1. Data dan jaringan

Ada dua endpoint RAWG yang dipakai, didefinisikan di `RawgApiService`:

- `GET games` dengan parameter `key`, `search`, dan `page_size` (diisi 20). Dipakai untuk daftar di Home sekaligus pencarian.
- `GET games/{id}` dengan parameter `key`. Dipakai untuk halaman Detail karena hanya endpoint ini yang mengembalikan deskripsi.

`RetrofitClient` membuat satu instance Retrofit (base URL `https://api.rawg.io/api/`) dengan `GsonConverterFactory`. Instance-nya dibuat sekali saja memakai `by lazy`.

Respons API ditampung di DTO (`GameListResponse`, `GameItemDto`, `GameDetailDto`) yang field-nya dicocokkan ke nama JSON lewat `@SerializedName`. Field yang bisa kosong, seperti `released` dan `background_image`, dibuat nullable.

### 2. Model untuk UI

DTO sengaja tidak dipakai langsung di layar. Hasil dari API diubah dulu menjadi `Game` lewat fungsi `toGame()`. Di situ genre diubah dari list objek menjadi `List<String>`, dan deskripsi diambil dari `description_raw`. Kalau `description_raw` kosong, dipakai `description` yang berformat HTML, lalu tag HTML-nya dibersihkan memakai `HtmlCompat`. Dengan cara ini, kalau format API berubah, yang perlu diubah hanya DTO dan mapper-nya.

### 3. Repository

`GameRepository` punya dua fungsi, `getGames(query)` dan `getGameDetail(id)`. Keduanya mengembalikan `Result`, jadi ViewModel cukup menangani `onSuccess` dan `onFailure` tanpa harus memakai `try-catch`. `CancellationException` sengaja dilempar ulang supaya pembatalan coroutine (misalnya saat pencarian diganti di tengah jalan) tidak dianggap sebagai error.

### 4. ViewModel

`GameViewModel` menyimpan semua state layar dalam `StateFlow`:

| State | Tipe | Fungsi |
|---|---|---|
| `query` | `String` | teks di kolom pencarian |
| `homeState` | `HomeUiState` | `Loading`, `Success(games)`, atau `Error(message)` |
| `showAllGames` | `Boolean` | `true` kalau halaman All Games sedang dibuka |
| `allQuery` | `String` | teks di kolom pencarian halaman All Games |
| `allState` | `AllGamesUiState` | daftar game, status loading, status memuat halaman berikutnya, dan pesan error |
| `selectedGameId` | `Int?` | game yang sedang dibuka, `null` berarti di Home |
| `detailState` | `DetailUiState` | `Loading`, `Success(game)`, atau `Error(message)` |

Pencarian tidak memanggil API di setiap huruf. Perubahan `query` ditahan dengan `debounce(400)`, jadi API baru dipanggil sekitar 400 ms setelah pengguna berhenti mengetik. Setiap pemanggilan baru juga membatalkan pemanggilan sebelumnya (`Job.cancel()`), sehingga hasil lama tidak menimpa hasil yang lebih baru.

Halaman All Games memuat data per halaman memakai parameter `page` dari RAWG. `loadMoreAllGames()` mengambil halaman berikutnya selama respons masih punya `next`, lalu menggabungkannya ke daftar yang sudah ada (duplikat dibuang dengan `distinctBy`). Mengganti kata pencarian mengulang pemuatan dari halaman pertama.

### 5. UI dan state

Aplikasi dibangun dengan prinsip state-driven UI. Composable tidak menyimpan data sendiri dan tidak memanggil API. Mereka hanya menerima state dan callback:

```kotlin
HomeScreen(
    uiState = homeState,
    query = query,
    onQueryChange = viewModel::onQueryChange,
    onGameClick = { viewModel.openGame(it.id) },
    onRetry = viewModel::loadGames
)
```

`MainActivity` membaca state dari ViewModel dengan `collectAsStateWithLifecycle()`. Pilihan layar ditentukan oleh state di ViewModel: `showAllGames` memilih antara `HomeScreen` dan `AllGamesScreen`, sedangkan `selectedGameId` yang tidak `null` menampilkan `DetailScreen` di atas keduanya. Karena Home atau All Games tetap ada di bawah Detail, posisi scroll dan hasil pencarian tidak hilang saat pengguna kembali. Tombol back sistem ditangani dengan `BackHandler`.

**Recomposition.** Pencarian bisa dipakai untuk menjelaskan cara kerjanya:

1. Pengguna mengetik, lalu `onQueryChange` mengubah nilai `query` di ViewModel.
2. `StateFlow` mengeluarkan nilai baru, dan `collectAsStateWithLifecycle()` meneruskannya sebagai `State` ke Compose.
3. Composable yang membaca `query` dijalankan ulang (recomposition), sehingga teks di search bar ikut berubah.
4. Saat itu `homeState` belum berubah, jadi `GameList` menerima parameter yang sama dan Compose melewatinya.
5. Setelah jeda 400 ms, API dipanggil dan `homeState` berubah dari `Loading` ke `Success`. Baru pada tahap ini daftar game digambar ulang.

Di `LazyColumn`, setiap baris diberi `key` berupa id game. Dengan begitu Compose bisa mengenali baris yang sama ketika hasil pencarian berganti, dan tidak membuat semuanya dari awal.

**Home.** Daftar memakai `LazyColumn`. Satu item `LazyColumn` berisi dua kartu game dalam satu `Row`, jadi tampilannya dua kolom tapi tetap satu lazy layout. Keempat kondisi tampilan ditentukan lewat `when (uiState)`: loading menampilkan `CircularProgressIndicator`, error menampilkan pesan dan tombol "Coba lagi", sukses tapi kosong menampilkan "Game tidak ditemukan", dan sukses menampilkan daftar.

**All Games.** Halaman ini punya search bar sendiri dan memakai komponen kartu yang sama dengan Home. Pemuatan halaman berikutnya dipicu saat item yang terlihat sudah mendekati akhir daftar. Kondisi itu dihitung dengan `derivedStateOf` dari `LazyListState`, sehingga hanya berubah saat statusnya benar-benar berpindah, bukan di setiap pergeseran scroll. Di Home, daftar hanya menampilkan 4 game populer ketika kolom pencarian kosong, dan menampilkan seluruh hasil (maksimal 20) ketika pengguna sedang mencari.

**Detail.** Gambar header diletakkan tetap di bagian atas. Di bawahnya ada lembar putih berisi nama, rating, tanggal rilis, dan genre yang ikut diam di layar. Bagian yang bisa digulir (`verticalScroll`) hanya "About this game" dan deskripsinya.

### 6. Theme dan typography

Theme dibuat sendiri di folder `ui/theme`:

- `Color.kt` berisi palet biru lembut untuk mode terang, palet untuk mode gelap, warna bintang rating, dan warna chip genre.
- `Type.kt` mengatur ukuran dan ketebalan teks untuk judul, isi, dan label.
- `Theme.kt` membungkus aplikasi dalam `GameDexTheme` berbasis `MaterialTheme`, lengkap dengan bentuk sudut membulat (12, 20, dan 28 dp).

Composable mengambil warna dan gaya teks dari `MaterialTheme.colorScheme` dan `MaterialTheme.typography`, tidak ditulis langsung di tiap komponen. Default `GameDexTheme` adalah mode terang.

### 7. Penyimpanan API key

API key tidak ditulis di dalam kode. Key disimpan di `local.properties` (sudah masuk `.gitignore`), dibaca oleh `app/build.gradle.kts`, lalu dimasukkan ke `BuildConfig.RAWG_API_KEY`. `RawgApiService` memakai nilai itu sebagai default parameter `key`.

## Cara Menjalankan

1. Clone repository ini dan buka di Android Studio.
2. Daftar di [rawg.io/apidocs](https://rawg.io/apidocs) untuk mendapatkan API key gratis.
3. Buka `local.properties` di folder root proyek (kalau belum ada, salin dari `local.properties.example`), lalu tambahkan baris berikut di bawah `sdk.dir`:

   ```properties
   RAWG_API_KEY=isi_api_key_anda
   ```

4. Sync Gradle, lalu jalankan aplikasi di emulator atau perangkat (Android 10 / API 29 ke atas). Perangkat harus terhubung ke internet.

Kalau key belum diisi, aplikasi tetap bisa dibuild, tapi daftar game tidak akan muncul dan layar menampilkan pesan error.

## Sumber Data

Seluruh data game berasal dari [RAWG.io](https://rawg.io).

## Tampilan Aplikasi

<img src="https://github.com/user-attachments/assets/2c368b69-0b68-4254-80e4-59976ae5eac2" width="220" alt="Home" />
<img src="https://github.com/user-attachments/assets/dc319c63-9774-4e58-8d50-58d06cf4bb95" width="220" alt="All Games" />
<img src="https://github.com/user-attachments/assets/a49ebd1e-b70d-4e63-ad62-6b1388172705" width="220" alt="Search" />
<img src="https://github.com/user-attachments/assets/1c80a5cc-54f9-419a-a351-4ed3d8157067" width="220" alt="Detail" />

