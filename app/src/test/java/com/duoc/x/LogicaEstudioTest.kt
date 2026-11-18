package com.duoc.x

import com.duoc.x.model.ArticuloItem
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LogicaEstudioTest {

    @Test
    fun `verificar que un articulo con texto en la nota devuelva verdadero`() {

        val articuloConNota = ArticuloItem(
            id = "test_1",
            numero = "Art. 1",
            extracto = "Texto de prueba",
            contenido = "Contenido completo",
            nota = "Importante para la prueba del viernes"
        )

        assertTrue(articuloConNota.tieneNota)
    }

    @Test
    fun `verificar que un articulo sin texto en la nota devuelva falso`() {

        val articuloSinNota = ArticuloItem(
            id = "test_2",
            numero = "Art. 2",
            extracto = "Texto de prueba",
            contenido = "Contenido completo",
            nota = ""
        )
        assertFalse(articuloSinNota.tieneNota)
    }
}