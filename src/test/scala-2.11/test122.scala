//import org.gk.db.MetaData._
//import org.gk.db.{InitDatabase, Tables}
//import slick.dbio.DBIO
//import slick.driver.H2Driver.api._
//import slick.jdbc.meta.MTable
//
//import slick.driver.H2Driver.api._
//import slick.jdbc.meta._
//import scala.concurrent.{Future, Await}
//import scala.concurrent.duration.Duration
//import scala.concurrent.ExecutionContext.Implicits.global
///**
// * Created by goku on 2015/8/3.
// */
//object abc{
//  class Suppliers(tag: Tag) extends Table[(Int, String, String, String, String, String)](tag, "SUPPLIERS") {
//    def id = column[Int]("SUP_ID", O.PrimaryKey) // This is the primary key column
//    def name = column[String]("SUP_NAME")
//    def street = column[String]("STREET")
//    def city = column[String]("CITY")
//    def state = column[String]("STATE")
//    def zip = column[String]("ZIP")
//    // Every table needs a * projection with the same type as the table's type parameter
//    def * = (id, name, street, city, state, zip)
//  }
//  val suppliers = TableQuery[Suppliers]
//
//  // Definition of the COFFEES table
//  class Coffees(tag: Tag) extends Table[(String, Int, Double, Int, Int)](tag, "COFFEES") {
//    def name = column[String]("COF_NAME", O.PrimaryKey)
//    def supID = column[Int]("SUP_ID")
//    def price = column[Double]("PRICE")
//    def sales = column[Int]("SALES")
//    def total = column[Int]("TOTAL")
//    def * = (name, supID, price, sales, total)
//    // A reified foreign key relation that can be navigated to create a join
//    def supplier = foreignKey("SUP_FK", supID, suppliers)(_.id)
//  }
//  val coffees = TableQuery[Coffees]
//
//  val setup = DBIO.seq(
//    // Create the tables, including primary and foreign keys
//    (suppliers.schema ++ coffees.schema).create,
//
//    // Insert some suppliers
//    suppliers += (101, "Acme, Inc.",      "99 Market Street", "Groundsville", "CA", "95199"),
//    suppliers += ( 49, "Superior Coffee", "1 Party Place",    "Mendocino",    "CA", "95460"),
//    suppliers += (150, "The High Ground", "100 Coffee Lane",  "Meadows",      "CA", "93966"),
//    // Equivalent SQL code:
//    // insert into SUPPLIERS(SUP_ID, SUP_NAME, STREET, CITY, STATE, ZIP) values (?,?,?,?,?,?)
//
//    // Insert some coffees (using JDBC's batch insert feature, if supported by the DB)
//    coffees ++= Seq(
//      ("Colombian",         101, 7.99, 0, 0),
//      ("French_Roast",       49, 8.99, 0, 0),
//      ("Espresso",          150, 9.99, 0, 0),
//      ("Colombian_Decaf",   101, 8.99, 0, 0),
//      ("French_Roast_Decaf", 49, 9.99, 0, 0)
//    )
//    // Equivalent SQL code:
//    // insert into COFFEES(COF_NAME, SUP_ID, PRICE, SALES, TOTAL) values (?,?,?,?,?)
//  )
//
//  val setupFuture = db.run(setup)
//  def main(args: Array[String]) {
//    val conn = Database.forConfig("DownFileWorkers").createSession().conn
//    val stat = conn.createStatement()
//    val rest = stat.executeQuery("select * from DOWN_FILE_WORK_LIST")
//    while (rest.next()){
//      println(rest.getString(1))
//    }
////    val db = Database.forConfig("DownFileWorkers").
//
//  }
//}