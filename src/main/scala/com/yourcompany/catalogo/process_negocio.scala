package com.yourcompany.catalogo


import com.huemulsolutions.bigdata.common._
import com.huemulsolutions.bigdata.control._
import java.util.Calendar
import com.yourcompany.tables.master._
import com.yourcompany.catalogo.datalake._
import com.yourcompany.settings._
import com.huemulsolutions.bigdata.control.huemulType_Frequency

//import com.huemulsolutions.bigdata.tables._
//import com.huemulsolutions.bigdata.dataquality._


object process_negocio {
  
  /**
   * Este codigo se ejecuta cuando se llama el JAR desde spark2-submit. el codigo esta preparado para hacer reprocesamiento masivo.
  */
  def main(args : Array[String]) {
    //Creacion API
    val huemulBigDataGov  = new huemul_BigDataGovernance(s"Masterizacion tabla tbl_comun_negocio - ${this.getClass.getSimpleName}", args, globalSettings.Global)
    
    /*************** PARAMETROS **********************/
    var param_ano = huemulBigDataGov.arguments.GetValue("ano", null, "Debe especificar el parametro año, ej: ano=2017").toInt
    var param_mes = huemulBigDataGov.arguments.GetValue("mes", null, "Debe especificar el parametro mes, ej: mes=12").toInt

    val param_dia = 1
    val param_numMeses = huemulBigDataGov.arguments.GetValue("num_meses", "1").toInt

    /*************** CICLO REPROCESO MASIVO **********************/
    var i: Int = 1
    val Fecha = huemulBigDataGov.setDateTime(param_ano, param_mes, param_dia, 0, 0, 0)
    
    while (i <= param_numMeses) {
      param_ano = huemulBigDataGov.getYear(Fecha)
      param_mes = huemulBigDataGov.getMonth(Fecha)
      println(s"Procesando Año $param_ano, Mes $param_mes ($i de $param_numMeses)")

      //Ejecuta codigo
      val FinOK = process_master(huemulBigDataGov, null, param_ano, param_mes)
      
      if (FinOK)
        i+=1
      else {
        println(s"ERROR Procesando Año $param_ano, Mes $param_mes ($i de $param_numMeses)")
        i = param_numMeses + 1
      }
        
      Fecha.add(Calendar.MONTH, 1)      
    }
    
    
    huemulBigDataGov.close
  }
  
  /**
    masterizacion de archivo [CAMBIAR] <br>
    param_ano: año de los datos  <br>
    param_mes: mes de los datos  <br>
   */
  def process_master(huemulBigDataGov: huemul_BigDataGovernance, ControlParent: huemul_Control, param_ano: Integer, param_mes: Integer): Boolean = {
    val Control = new huemul_Control(huemulBigDataGov, ControlParent, huemulType_Frequency.ANY_MOMENT)    
    
    try {             
      /*************** AGREGAR PARAMETROS A CONTROL **********************/
      Control.AddParamYear("param_ano", param_ano)
      Control.AddParamMonth("param_mes", param_mes)
      
      
      /*************** ABRE RAW DESDE DATALAKE **********************/
      Control.NewStep("Abre DataLake")
      val DF_RAW = new raw_negocio_mes(huemulBigDataGov, Control)
      if (!DF_RAW.open("DF_RAW", Control, param_ano, param_mes, 1, 0, 0, 0))       
        Control.RaiseError(s"error encontrado, abortar: ${DF_RAW.Error.ControlError_Message}")
      
      
      /*********************************************************/
      /*************** LOGICAS DE NEGOCIO **********************/
      /*********************************************************/
      //instancia de clase tbl_comun_negocio 
      val huemulTable = new tbl_comun_negocio(huemulBigDataGov, Control)
      
      Control.NewStep("Generar Logica de Negocio")
      huemulTable.DF_from_SQL("FinalRAW"
                          , s"""SELECT TO_DATE("$param_ano-$param_mes-1") as periodo_mes
                                     ,negocio_id
                                     ,negocio_nombre

                               FROM DF_RAW""")
      
      DF_RAW.DataFramehuemul.DataFrame.unpersist()
      
      //comentar este codigo cuando ya no sea necesario generar estadisticas de las columnas.
      Control.NewStep("QUITAR!!! Generar Estadisticas de las columnas SOLO PARA PRIMERA EJECUCION")
      huemulTable.DataFramehuemul.DQ_StatsAllCols(Control, huemulTable)        
      
      Control.NewStep("Asocia columnas de la tabla con nombres de campos de SQL")
      
      huemulTable.negocio_id.setMapping("negocio_id")
      huemulTable.negocio_nombre.setMapping("negocio_nombre")

      
      
      Control.NewStep("Ejecuta Proceso")    
      if (!huemulTable.executeFull("FinalSaved"))
        Control.RaiseError(s"User: Error al intentar masterizar instituciones (${huemulTable.Error_Code}): ${huemulTable.Error_Text}")
      
      Control.FinishProcessOK
    } catch {
      case e: Exception =>
        Control.Control_Error.GetError(e, this.getClass.getName, null)
        Control.FinishProcessError()
    }
    
    Control.Control_Error.IsOK()
  }
  
}
