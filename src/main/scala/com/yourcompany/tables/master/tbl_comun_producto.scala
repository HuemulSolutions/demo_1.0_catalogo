package com.yourcompany.tables.master


import com.huemulsolutions.bigdata.common._
import com.huemulsolutions.bigdata.control._
import com.huemulsolutions.bigdata.tables._
import org.apache.spark.sql.types._


class tbl_comun_producto(huemulBigDataGov: huemul_BigDataGovernance, Control: huemul_Control) extends huemul_Table(huemulBigDataGov, Control) with Serializable {
  /**********   C O N F I G U R A C I O N   D E   L A   T A B L A   ****************************************/
  //Tipo de tabla, Master y Reference son catalogos sin particiones de periodo
  this.setTableType(huemulType_Tables.Master)
  //Base de Datos en HIVE donde sera creada la tabla
  this.setDataBase(huemulBigDataGov.GlobalSettings.MASTER_DataBase)
  //Tipo de archivo que sera almacenado en HDFS
  this.setStorageType(huemulType_StorageType.PARQUET)
  //Ruta en HDFS donde se guardara el archivo PARQUET
  this.setGlobalPaths(huemulBigDataGov.GlobalSettings.MASTER_SmallFiles_Path)
  //Ruta en HDFS especifica para esta tabla (Globalpaths / localPath)
  this.setLocalPath("catalogo/")
  //Frecuencia de actualización
  this.setFrequency(huemulType_Frequency.ANY_MOMENT)
  //nuevo desde version 2.0
  //permite guardar versiones de los datos antes de que se vuelvan a ejecutar los procesos (solo para tablas de tipo master y reference)
  this.setSaveBackup(true)
  //nuevo desde versión 2.1
  //permite asignar un código de error personalizado al fallar la PK
  this.setPK_externalCode("COD_ERROR")
  
  /**********   S E T E O   O P T I M I Z A C I O N   ****************************************/
  //nuevo desde version 2.0
  //indica la cantidad de archivos que generará al guardar el fichero (1 para archivos pequeños
  //de esta forma se evita el problema de los archivos pequeños en HDFS
  this.setNumPartitions(1)
  

  /**********   S E T E O   I N F O R M A T I V O   ****************************************/
  //Nombre del contacto de TI
  this.setDescription("Tabla que contiene el catálogo corporativo de productos")
  //Nombre del contacto de negocio
  this.setBusiness_ResponsibleName("Area de Negocio")
  //Nombre del contacto de TI
  this.setIT_ResponsibleName("Area de Sistemas")
   
  /**********   D A T A   Q U A L I T Y   ****************************************/
  //DataQuality: maximo numero de filas o porcentaje permitido, dejar comentado o null en caso de no aplicar
  //this.setDQ_MaxNewRecords_Num(null)  //ej: 1000 para permitir maximo 1.000 registros nuevos cada vez que se intenta insertar
  //this.setDQ_MaxNewRecords_Perc(null) //ej: 0.2 para limitar al 20% de filas nuevas
    
  /**********   S E G U R I D A D   ****************************************/
  //Solo estos package y clases pueden ejecutar en modo full, si no se especifica todos pueden invocar
  this.WhoCanRun_executeFull_addAccess("process_producto", "com.yourcompany.catalogo")
  //Solo estos package y clases pueden ejecutar en modo solo Insert, si no se especifica todos pueden invocar
  //this.WhoCanRun_executeOnlyInsert_addAccess("[[MyclassName]]", "[[my.package.path]]")
  //Solo estos package y clases pueden ejecutar en modo solo Update, si no se especifica todos pueden invocar
  //this.WhoCanRun_executeOnlyUpdate_addAccess("[[MyclassName]]", "[[my.package.path]]")
  

  /**********   C O L U M N A S   ****************************************/
    
  val producto_id: huemul_Columns = new huemul_Columns (StringType, true, "codigo del producto")
          .setIsPK().setDQ_MinLen(2,"DQ_ERROR_001").setDQ_MaxLen(10,"DQ_ERROR_001")
          .securityLevel(huemulType_SecurityLevel.Public)    

  val producto_nombre: huemul_Columns = new huemul_Columns (StringType, true, "Nombre del producto")
          .setDQ_MinLen(5,"DQ_ERROR_002").setDQ_MaxLen(100,"DQ_ERROR_002").setMDM_EnableOldValue()
          .securityLevel(huemulType_SecurityLevel.Public)  


  //**********Atributos adicionales de DataQuality 
  /*
            .setIsPK()         //por default los campos no son PK
            .setIsUnique("COD_ERROR") //por default los campos pueden repetir sus valores
            .setNullable() //por default los campos no permiten nulos
            .setDQ_MinDecimalValue(Decimal.apply(0),"COD_ERROR")
            .setDQ_MaxDecimalValue(Decimal.apply(200.0),"COD_ERROR")
            .setDQ_MinDateTimeValue("2018-01-01","COD_ERROR")
            .setDQ_MaxDateTimeValue("2018-12-31","COD_ERROR")
            .setDQ_MinLen(5,"COD_ERROR")
            .setDQ_MaxLen(100,"COD_ERROR")
  */
  //**********Otros atributos
  /*
            .setDefaultValues("'string'") // "10" // "'2018-01-01'"
  				  .encryptedType("tipo")
  */
    
  //**********Ejemplo para aplicar DataQuality de Integridad Referencial
  //val i[[tbl_PK]] = new [[tbl_PK]](huemulBigDataGov,Control)
  //val fk_[[tbl_PK]] = new huemul_Table_Relationship(i[[tbl_PK]], false)
  //          .setNotification(huemulType_DQNotification.ERROR)
  //          .setExternalCode("COD_ERROR")
  //fk_[[tbl_PK]].AddRelationship(i[[tbl_PK]].[[PK_Id]], [[LocalField]_Id)
    
  //**********Ejemplo para agregar reglas de DataQuality Avanzadas  -->ColumnXX puede ser null si la validacion es a nivel de tabla
  //**************Parametros
  //********************  ColumnXXColumna a la cual se aplica la validacion, si es a nivel de tabla poner null
  //********************  Descripcion de la validacion, ejemplo: "Consistencia: Campo1 debe ser mayor que campo 2"
  //********************  Formula SQL En Positivo, ejemplo1: campo1 > campo2  ;ejemplo2: sum(campo1) > sum(campo2)  
  //********************  CodigoError: Puedes especificar un codigo para la captura posterior de errores, es un numero entre 1 y 999
  //********************  QueryLevel es opcional, por default es "row" y se aplica al ejemplo1 de la formula, para el ejmplo2 se debe indicar "Aggregate"
  //********************  Notification es opcional, por default es "error", y ante la aparicion del error el programa falla, si lo cambias a "warning" y la validacion falla, el programa sigue y solo sera notificado
  //********************  SaveErrorDetails: es opcional, true para guardar el detalle de filas que no cumplen con DQ. valor por default es true
  //********************  DQ_ExternalCode: es opcional, indica el código de DataQuality externo para enlazar con herramientas de gobierno
  //val DQ_NombreRegla: huemul_DataQuality = new huemul_DataQuality(columnaXX,"Descripcion de la validacion", "Campo_1 > Campo_2",1)
  //          .setQueryLevel(huemulType_DQQueryLevel.Row)
  //          .setNotification(huemulType_DQNotification.WARNING_EXCLUDE)
  //          .setSaveErrorDetails(true)
  //**************Adicionalmente, puedes agregar "tolerancia" a la validacion, es decir, puedes especiicar 
  //************** numFilas = 10 para permitir 10 errores (al 11 se cae)
  //************** porcentaje = 0.2 para permitir una tolerancia del 20% de errores
  //************** ambos parametros son independientes (condicion o), cualquiera de las dos tolerancias que no se cumpla se gatilla el error o warning
  //DQ_NombreRegla.setTolerance(numfilas, porcentaje)
  //DQ_NombreRegla.setDQ_ExternalCode("COD_ERROR")
    
  this.ApplyTableDefinition()
}

