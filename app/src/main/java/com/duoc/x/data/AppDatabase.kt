package com.duoc.x.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase

// 1. Entidad Room para guardar la sesión y preferencia de lectura del usuario (Login / Perfil)
@Entity(tableName = "usuario_sesion")
data class UsuarioEntity(
    @PrimaryKey val id: Int = 1,
    val nombre: String,
    val correo: String,
    val tamanoFuenteSp: Int = 16
)

// 2. Entidad Room para guardar las notas y artículos destacados (Mis Estudios)
@Entity(tableName = "articulos_estudio")
data class ArticuloEstudioEntity(
    @PrimaryKey val articuloId: String,
    val normaTitulo: String,
    val numero: String,
    val esDestacado: Boolean,
    val nota: String
)

// 3. DAO para operaciones de Usuario
@Dao
interface UsuarioDao {
    @Query("SELECT * FROM usuario_sesion WHERE id = 1 LIMIT 1")
    fun obtenerUsuario(): UsuarioEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun guardarUsuario(usuario: UsuarioEntity)

    @Query("DELETE FROM usuario_sesion")
    fun cerrarSesion()
}

// 4. DAO para operaciones de Notas y Destacados
@Dao
interface ArticuloEstudioDao {
    @Query("SELECT * FROM articulos_estudio")
    fun obtenerTodos(): List<ArticuloEstudioEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun guardarEstadoArticulo(articulo: ArticuloEstudioEntity)
}

// 5. Base de datos principal de Room
@Database(
    entities = [UsuarioEntity::class, ArticuloEstudioEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun articuloEstudioDao(): ArticuloEstudioDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "codigo_ch_database"
                )
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}