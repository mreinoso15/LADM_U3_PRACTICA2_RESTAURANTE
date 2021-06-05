package mx.tecnm.tepic.ladm_u3_practica2_restaurante

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    var baseRemota = FirebaseFirestore.getInstance()
    var dataLista = ArrayList<String>()
    var listaOrdenes = ArrayList<Ordenes>()
    var listaID = ArrayList<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        baseRemota.collection("pedidos")
            .addSnapshotListener { querySnapshot, error ->
                if (error != null){
                    mensaje(error.message!!)
                    return@addSnapshotListener
                }
                dataLista.clear()
                listaID.clear()
                for (document in querySnapshot!!){
                    var cadena = "${document.getString("nombre")} --- ${document.get("total")}"
                    dataLista.add(cadena)
                    listaID.add(document.id.toString())
                }
                lista.adapter = ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,dataLista)
                lista.setOnItemClickListener { parent, view, position, id ->
                    dialogoEliminaActualiza(position)
                }
            }
        agregarorden.setOnClickListener {
            insertarOrden()
        }
        button2.setOnClickListener {
            insertar()
        }

    }

    private fun insertarOrden() {

        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.agregar_ordenes,null)
        val producto = dialogLayout.findViewById<EditText>(R.id.ingresarproducto)
        val cantidad = dialogLayout.findViewById<EditText>(R.id.ingresarcantidad)
        val total = dialogLayout.findViewById<EditText>(R.id.ingresartotal)

        with(builder){
            setTitle("Ingrese su orden")
            setPositiveButton("OK"){ d,i->
                var orden = Ordenes(cantidad.text.toString().toInt(),
                                    producto.text.toString(),
                                    total.text.toString().toInt())
                listaOrdenes.add(orden)
                println(listaOrdenes)
                mensaje("Se ingreso la orden al pedido")
            }
            setNegativeButton("Cerrar"){ d,i->}
                    .setView(dialogLayout)
                    .show()
        }
    }

    private fun calcularTotal():Int{
        var total = 0
        listaOrdenes.forEach {
            total += it.precio
        }
        return total
    }

    private fun insertar(){
        if (listaOrdenes.isEmpty()){
            mensaje("ERROR! \n No se han agregado productos a la orden")
        }else {
            var id = ""
            var formato = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
            var fecha = formato.format(Date())
            var total = calcularTotal()
            var entregado = false
            var datosInsertar = hashMapOf(
                    "nombre" to cliente.text.toString(),
                    "celular" to telefono.text.toString(),
                    "entregado" to entregado,
                    "total" to total,
                    "fecha" to fecha
            )
            baseRemota.collection("pedidos")
                    .add(datosInsertar)
                    .addOnSuccessListener {
                        alerta("EXITO! SE INSERTO CORRECTAMENTE")
                        cliente.setText("")
                        telefono.setText("")
                        id = it.id.toString()
                        println(id)
                        agregarOrdenes(id)

                    }
                    .addOnFailureListener {
                        mensaje("ERROR! no se pudo insertar")
                    }
        }
    }

    private fun agregarOrdenes(id: String) {
        var i = 1;
        listaOrdenes.forEach{
            var datosOrden = hashMapOf(
                    "pedido" to hashMapOf(
                    "item ${i.toString()}" to hashMapOf(
                            "cantidad" to it.cant,
                            "descripcion" to it.desc,
                            "total" to it.precio
                     )
                    )
            )
            baseRemota.collection("pedidos")
                    .document(id)
                    .set(datosOrden, SetOptions.merge())
                    .addOnSuccessListener {
                        println("EXITO! SE INSERTO CORRECTAMENTE")
                    }
                    .addOnFailureListener {
                        mensaje("ERROR! no se pudo insertar")
                    }
            i++
        }
    }


    private fun dialogoEliminaActualiza(position: Int) {
        var idElegido = listaID.get(position)
        AlertDialog.Builder(this).setTitle("ATENCION!!")
                .setMessage("QUE DESEAS HACER CON \n ${dataLista.get(position)}?")
                .setPositiveButton("ELIMINAR"){d, i->
                    eliminar(idElegido)
                }
                .setNeutralButton("ACTUALIZAR"){d,i->
                    modificar(idElegido)
                }
                .setNegativeButton("CANCELAR"){d,i->}
                .show()
    }

    private fun modificar(idElegido: String) {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.modificar_status,null)
        val entrega = dialogLayout.findViewById<CheckBox>(R.id.entrega)

        with(builder){
            setTitle("Se entrego la orden?")
            setPositiveButton("OK"){ d,i->
                if (entrega.isChecked){
                    baseRemota.collection("pedidos")
                            .document(idElegido)
                            .update("entregado",true)
                            .addOnSuccessListener {
                                mensaje("Se modifico el estado del pedido")
                            }
                }

            }
            setNegativeButton("Cerrar"){ d,i->}
                    .setView(dialogLayout)
                    .show()
        }
    }

    private fun mensaje(s: String) {
        AlertDialog.Builder(this).setTitle("ATENCION")
            .setMessage(s)
            .setPositiveButton("OK"){ d,i-> }
            .show()
    }

    private fun alerta(s: String) {
        Toast.makeText(this,s, Toast.LENGTH_LONG).show()
    }

    private fun eliminar(idElegido : String){
        baseRemota.collection("pedidos")
                .document(idElegido)
                .delete()
                .addOnFailureListener {
                    mensaje("ERROR! ${it.message!!}")
                }
                .addOnSuccessListener {
                    mensaje("SE ELIMINO CON EXITO")
                }
    }

}