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


/*

Estos codigos deben pasar a la db en retrofit y usarse como keys para los articulos

NormaItem(
            id = "c_civil",
            titulo = "Código Civil",
            descripcion = "Materia: Derecho Civil, Contratos y Propiedad",
            categoria = "Civil"
        ),
        NormaItem(
            id = "c_trabajo",
            titulo = "Código del Trabajo",
            descripcion = "Materia: Derecho Laboral, Protección y Contratos",
            categoria = "Laboral"
        ),
        NormaItem(
            id = "c_constitucion",
            titulo = "Constitución Política",
            descripcion = "Materia: Derecho Político, Bases de la Institucionalidad",
            categoria = "Constitución"
        )

 */

/*

Estos articulos se deben pasar a la db en retrofit y deben de estar conectados con una key de los codigos

ArticuloItem(
            id = "ct_art1",
            normaTitulo = "Código del Trabajo",
            numero = "Art. 1",
            extracto = "Las relaciones laborales entre empleadores y trabajadores se regularán por este Código y por sus leyes complementarias.",
            contenido = "Las relaciones laborales entre empleadores y trabajadores se regularán por este Código y por sus leyes complementarias.\n\nEstas normas no se aplicarán, sin embargo, a los funcionarios de la Administración del Estado, centralizada y descentralizada, del Congreso Nacional y del Poder Judicial, ni a los del Ministerio Público.",
            esDestacado = false,
            nota = "Regla general: aplica a toda relación laboral."
        ),
        ArticuloItem(
            id = "ct_art2",
            normaTitulo = "Código del Trabajo",
            numero = "Art. 2",
            extracto = "Las funciones o servicios prestados en una empresa deben considerar la igual dignidad de los trabajadores y la protección social.",
            contenido = "Las funciones o servicios prestados en una empresa deben considerar la igual dignidad de los trabajadores y la protección social.\n\nSon contrarios a los principios de las leyes laborales los actos de discriminación. Se consideran actos de discriminación las distinciones, exclusiones o preferencias basadas en motivos de raza, color, sexo, edad, estado civil, sindicación o religión.",
            esDestacado = false,
            nota = ""
        ),
        ArticuloItem(
            id = "ct_art3",
            normaTitulo = "Código del Trabajo",
            numero = "Art. 3",
            extracto = "Para todos los efectos legales se entiende por empleador la persona natural o jurídica que utiliza los servicios de uno o más trabajadores...",
            contenido = "Para todos los efectos legales se entiende por empleador la persona natural o jurídica que utiliza los servicios de uno o más trabajadores en virtud de un contrato de trabajo.\n\nTrabajador es toda persona natural que presta servicios personales intelectuales o materiales bajo dependencia o subordinación, en virtud de un contrato de trabajo.",
            esDestacado = true,
            nota = "Distinguir bien empleador de empresa. Repasar antes de la prueba."
        ),
        ArticuloItem(
            id = "ct_art4",
            normaTitulo = "Código del Trabajo",
            numero = "Art. 4",
            extracto = "Se presume de derecho que representa al empleador y que en tal carácter obliga a este con los trabajadores el gerente, el director...",
            contenido = "Se presume de derecho que representa al empleador y que en tal carácter obliga a este con los trabajadores el gerente, el administrador, el capitán de barco y, en general, la persona que ejerce habitualmente funciones de dirección o administración por cuenta o representación de una persona natural o jurídica.",
            esDestacado = false,
            nota = ""
        ),
        ArticuloItem(
            id = "cc_art1",
            normaTitulo = "Código Civil",
            numero = "Art. 1",
            extracto = "La ley es una declaración de la voluntad soberana que, manifestada en la forma prescrita por la Constitución, manda, prohíbe o permite.",
            contenido = "La ley es una declaración de la voluntad soberana que, manifestada en la forma prescrita por la Constitución, manda, prohíbe o permite.",
            esDestacado = false,
            nota = ""
        ),
        ArticuloItem(
            id = "cc_art2",
            normaTitulo = "Código Civil",
            numero = "Art. 2",
            extracto = "La costumbre no constituye derecho sino en los casos en que la ley se remite a ella.",
            contenido = "La costumbre no constituye derecho sino en los casos en que la ley se remite a ella.",
            esDestacado = true,
            nota = "Revisar diferencia con costumbre mercantil."
        ),
        ArticuloItem(
            id = "cc_art1545",
            normaTitulo = "Código Civil",
            numero = "Art. 1545",
            extracto = "Todo contrato legalmente celebrado es una ley para los contratantes, y no puede ser invalidado sino por su consentimiento mutuo...",
            contenido = "Todo contrato legalmente celebrado es una ley para los contratantes, y no puede ser invalidado sino por su consentimiento mutuo o por causas legales.",
            esDestacado = false,
            nota = "Ejemplo para el ensayo de contratos."
        ),
        ArticuloItem(
            id = "cp_art1",
            normaTitulo = "Constitución Política",
            numero = "Art. 1",
            extracto = "Las personas nacen libres e iguales en dignidad y derechos. La familia es el núcleo fundamental de la sociedad.",
            contenido = "Las personas nacen libres e iguales en dignidad y derechos.\n\nLa familia es el núcleo fundamental de la sociedad.\n\nEl Estado está al servicio de la persona humana y su finalidad es promover el bien común.",
            esDestacado = true,
            nota = "Base de la institucionalidad."
        ),
        ArticuloItem(
            id = "cp_art2",
            normaTitulo = "Constitución Política",
            numero = "Art. 2",
            extracto = "Es deber del Estado resguardar la seguridad nacional, dar protección a la población y a la familia.",
            contenido = "Son emblemas nacionales la bandera nacional, el escudo de armas de la República y el himno nacional.",
            esDestacado = false,
            nota = ""
        )
    )

 */