package mx.tecnm.tepic.ladm_u3_practica2_restaurante

class Ordenes (cantidad:Int,descripcion:String,total:Int) {

        var cant = cantidad
        var desc = descripcion
        var precio = total

        override fun toString(): String {
                return "Ordenes(cant=$cant, desc='$desc', precio=$precio)"
        }


}