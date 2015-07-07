package core.dao



import reactivemongo.api.commands.WriteResult
import scala.concurrent.Future

import play.api.libs.json._
import play.modules.reactivemongo.json.collection.{JSONCollection}

import reactivemongo.bson._

import scala.concurrent.ExecutionContext.Implicits.global
import play.modules.reactivemongo.json._, ImplicitBSONHandlers._


object DocumentDAO{

  type C = JSONCollection
  
  def find[T](collection: C, query: JsObject = Json.obj(), limit: Int = 0, delay: Int = 1)(implicit reader: Reads[T]): Future[List[T]] = {

    val future = Future((1 to delay).zipWithIndex  )
    val x = future.map {
      _ => collection.find(query).cursor[T]().collect[List](limit)
    }
    val z = x . flatMap {
      y => y
    }
    z
  }

  def findById[T](collection: C, id: String)(implicit reader: Reads[T]): Future[Option[T]] = findOne(collection, DBQueryBuilder.id(id))

  def findById[T](collection: C, id: BSONObjectID)(implicit reader: Reads[T]): Future[Option[T]] = findOne(collection, DBQueryBuilder.id(id))

  def findOne[T](collection: C, query: JsObject)(implicit reader: Reads[T]): Future[Option[T]] = {
    collection.find(query).one[T]
  }

  def insert[T](collection: C, elem: T)(implicit writes: OWrites[T]): Future[WriteResult] = {
    collection.insert(elem)
  }
}
