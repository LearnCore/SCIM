package controllers

import play.api.db.Database
import play.api.mvc._
import play.api.libs.json._

import javax.inject._
import java.sql.ResultSet

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException

class SCIMController @Inject() (db:Database) extends Controller {

  // ----------- Users ----------- //

  def users(filter:Option[String], count:Option[String], startIndex:Option[String]) = Action {
    // TODO: Retrieve paginated User Objects
    // TODO: Allow for an equals and startsWith filters on username
    val query = "SELECT * FROM users" +
      filter.map(" WHERE username LIKE '" + _ + "%'").getOrElse("") +
      count.map(" LIMIT " + _).getOrElse("") +
      startIndex.map(" OFFSET " + _).getOrElse("")
    
    db.withConnection { conn => 
      val stmt = conn.createStatement
      val rs = stmt.executeQuery(query)
      val users: Stream[JsValue] = results(rs)(parseUser)

      Ok(Json.obj("users" -> users))
    }
  }

  def user(uid:String) = Action {
    // TODO: Retrieve a single User Object by ID
    val query = s"SELECT * FROM users WHERE id = '${uid}'"

    db.withConnection { conn => 
      val stmt = conn.createStatement
      val rs = stmt.executeQuery(query)

      if (rs.next())
        Ok(parseUser(rs))
      else
        NotFound
    }
  }

  def createUser() = Action { implicit request: Request[AnyContent] =>
    // TODO: Create a User Object with firstname and lastname metadata
    val id = field[String]("id")
    val username = field[String]("username")
    val firstname = field[String]("firstname")
    val lastname = field[String]("lastname")
    val active = field[Boolean]("active", false)

    try {
      doCreateUser(id, username, firstname, lastname, active)
    } catch {
      case dup: MySQLIntegrityConstraintViolationException => BadRequest(dup.getMessage)
      case _: Throwable => InternalServerError
    }
  }

  def updateUser(uid:String) = Action { implicit request: Request[AnyContent] =>
    // TODO: Update a User Object's firstname, lastname, and active status
    val firstname = fieldOpt[String]("firstname")
    val lastname = fieldOpt[String]("lastname")
    val active = fieldOpt[Boolean]("active")

    doUpdateUser(Some(uid), firstname, lastname, active)
  }

  def deleteUser(uid:String) = Action {
    // TODO: Delete a User Object by ID
    doDeleteUser(uid)
  }

  // ----------- Groups ----------- //

  def groups(count:Option[String], startIndex:Option[String]) = Action {
    // TODO: Retrieve paginated Group Objects
    val query = "SELECT * FROM groups" +
      count.map(" LIMIT " + _).getOrElse("") +
      startIndex.map(" OFFSET " + _).getOrElse("")

    db.withConnection { conn => 
      val stmt = conn.createStatement
      val rs = stmt.executeQuery(query)
      val groups: Stream[JsValue] = results(rs)(parseGroup)

      Ok(Json.obj("groups" -> groups))
    }
  }

  def group(groupId:String) = Action {
    // TODO: Retrieve a single Group Object by ID
    val query = s"""
      SELECT users.* FROM users
        INNER JOIN users_groups
        ON users.id = users_groups.user_id
        WHERE group_id = '${groupId}'"""

    db.withConnection { conn => 
      val stmt = conn.createStatement
      val rs = stmt.executeQuery(query)
      val users: Stream[JsValue] = results(rs)(parseUser)

      Ok(Json.obj(
        "id" -> groupId,
        "users" -> users))
    }
  }

  def patchGroup(groupId:String) = Action { implicit request: Request[AnyContent] =>
    // TODO: Patch a Group Object, modifying its members
    val add = fieldOpt[List[JsValue]]("add")
    val delete = fieldOpt[List[String]]("delete")
    val update = fieldOpt[List[JsValue]]("update")

    if (add.isEmpty && delete.isEmpty && update.isEmpty) {
      BadRequest("empty patch request")
    } else {
      // this should probably be in a transaction to roll back in case of an exception
      try {
        add.map(_.foreach(user => (doCreateUser _).tupled(toUserTuple(user)))) // this ignores the result of calls to doCreateUser
        delete.map(_.foreach(doDeleteUser)) // this ignores the result of calls to doDeleteUser
        update.map(_.foreach(user => (doUpdateUser _).tupled(toUserOptTuple(user)))) // this ignores the result of calls to doUpdateUser
        Ok
      } catch {
        case dup: MySQLIntegrityConstraintViolationException => BadRequest(dup.getMessage)
        case _: Throwable => InternalServerError
      }
    }
  }

  // ----------- Utilities ----------- //

  // taken from https://stackoverflow.com/questions/9636545/treating-an-sql-resultset-like-a-scala-stream
  private def results[T](resultSet: ResultSet)(f: ResultSet => T): Stream[T] = {
    new Iterator[T] {
      def hasNext = resultSet.next()
      def next() = f(resultSet)
    }.toStream
  }

  private def parseUser(rs: ResultSet): JsValue =
    Json.obj(
      "id" -> rs.getString("id"),
      "username" -> rs.getString("username"),
      "firstname" -> rs.getString("firstname"),
      "lastname" -> rs.getString("lastname"),
      "active" -> (if (rs.getInt("active") == 1) "true" else "false")
    )

  private def parseGroup(rs: ResultSet): JsValue =
    Json.obj(
      "id" -> rs.getString("id")
    )

  private def toUserTuple(user: JsValue) =
    (
      (user \ "id").asOpt[String].getOrElse(null),
      (user \ "username").asOpt[String].getOrElse(null),
      (user \ "firstname").asOpt[String].getOrElse(null),
      (user \ "lastname").asOpt[String].getOrElse(null),
      (user \ "active").asOpt[Boolean].getOrElse(false))
  
  private def toUserOptTuple(user: JsValue) =
    (
      (user \ "id").asOpt[String],
      (user \ "firstname").asOpt[String],
      (user \ "lastname").asOpt[String],
      (user \ "active").asOpt[Boolean])

  private def doCreateUser(id: String, username: String, firstname: String, lastname: String, active: Boolean) = {
    if (id == null) {
      BadRequest("missing id")
    } else if (username == null) {
      BadRequest("missing username")
    } else if (firstname == null) {
      BadRequest("missing firstname")
    } else if (lastname == null) {
      BadRequest("missing lastname")
    } else {
      val query = s"""
        INSERT INTO users (id, username, firstname, lastname, active)
          VALUES
        ('${id}', '${username}', '${firstname}', '${lastname}', ${active});
      """

      db.withConnection { conn => 
        val stmt = conn.createStatement
        val rc = stmt.executeUpdate(query)

        if (rc == 1)
          Ok
        else
          InternalServerError("failed to create user")
      }
    }    
  }

  private def doDeleteUser(id: String) = {
    val query = s"DELETE FROM users WHERE id = '${id}'"

    db.withConnection { conn => 
      val stmt = conn.createStatement
      val rc = stmt.executeUpdate(query)

      if (rc == 1)
        Ok
      else
        NotFound
    }
  }

  private def doUpdateUser(id: Option[String], firstname: Option[String], lastname: Option[String], active: Option[Boolean]) = {
    if (id.isEmpty) {
      BadRequest("missing id")
    } else if (firstname.isEmpty && lastname.isEmpty && active.isEmpty) {
      BadRequest("must pass at least one of firstname, lastname, active to update")
    } else {
      val query = "UPDATE users SET" +
        (firstname.map(" firstname = '" + _ + "',").getOrElse("") +
         lastname.map(" lastname = '" + _ + "',").getOrElse("") +
         active.map(" active = " + _ + ",").getOrElse("")).dropRight(1) + // dropRight removes extra trailing comma
        s" WHERE id = '${id.get}'"
      
      db.withConnection { conn => 
        val stmt = conn.createStatement
        val rc = stmt.executeUpdate(query)

        if (rc == 1)
          Ok("user updated")
        else
          NotFound
      }
    }
  }

  private def field[T: Reads](field: String, default: T = null)(implicit request: Request[AnyContent]) = fieldOpt[T](field).getOrElse(default)
  private def fieldOpt[T: Reads](field: String)(implicit request: Request[AnyContent]) = request.body.asJson.map(_ \ field).flatMap(_.asOpt[T])

}
