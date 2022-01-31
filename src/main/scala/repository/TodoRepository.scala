package repository

import cats.effect.IO
import doobie.util.transactor.Transactor
import fs2.Stream
import model.{Importance, SourceCode, SourceCodeExists, SourceCodeExistsError, SourceCodeLookup, SourceCodeNotFound, SourceCodeNotFoundError, Todo, TodoNotFoundError}
import doobie._
import doobie.implicits._

class SourceCodeRepository(transactor: Transactor[IO]) {
  def getSourceCode(sourceCode: String): IO[Either[SourceCodeNotFoundError.type, SourceCodeLookup]] = {
    sql"SELECT source_code FROM source_code WHERE source_code = $sourceCode".query[SourceCodeLookup].option.transact(transactor).map {
      case Some(sourceCode) => Right(sourceCode)
      case None => Left(SourceCodeNotFoundError)
    }
  }

  def isNonExistentSourceCode(sourceCode: SourceCodeLookup): IO[Either[SourceCodeExistsError.type, SourceCodeNotFound.type]] = {
    sql"SELECT TRUE FROM source_code WHERE source_code = ${sourceCode.sourceCode}".query[SourceCodeExists].option.transact(transactor).map {
      case Some(_) => Left(SourceCodeExistsError)
      case None    => Right(SourceCodeNotFound)
    }
  }

  def createSourceCode(sourceCode: SourceCode): IO[SourceCode] = {
    sql"INSERT INTO source_code (source_code, mid13, mid10, first2, activity_type, activity_source, activity_source2, activity_source3, activity_source4, activity_source5, advertised_rate, brand_code, pct, end2) VALUES (${sourceCode.sourceCode}, ${sourceCode.mid13}, ${sourceCode.mid10}, ${sourceCode.first2}, ${sourceCode.activityType}, ${sourceCode.activitySource}, ${sourceCode.activitySource2}, ${sourceCode.activitySource3}, ${sourceCode.activitySource4}, ${sourceCode.activitySource5}, ${sourceCode.advertisedRate}, ${sourceCode.brandCode}, ${sourceCode.pct}, ${sourceCode.end2})".update.withUniqueGeneratedKeys[Long]("id").transact(transactor).map { id =>
      sourceCode.copy(id = Some(id))
    }
  }
}

class TodoRepository(transactor: Transactor[IO]) {
  private implicit val importanceMeta: Meta[Importance] = Meta[String].timap(Importance.unsafeFromString)( _.value)

  def getTodos: Stream[IO, Todo] = {
    sql"SELECT id, description, importance FROM todo".query[Todo].stream.transact(transactor)
  }

  def getTodo(id: Long): IO[Either[TodoNotFoundError.type, Todo]] = {
    sql"SELECT id, description, importance FROM todo WHERE id = $id".query[Todo].option.transact(transactor).map {
      case Some(todo) => Right(todo)
      case None => Left(TodoNotFoundError)
    }
  }

  def createTodo(todo: Todo): IO[Todo] = {
    sql"INSERT INTO todo (description, importance) VALUES (${todo.description}, ${todo.importance})".update.withUniqueGeneratedKeys[Long]("id").transact(transactor).map { id =>
      todo.copy(id = Some(id))
    }
  }

  def deleteTodo(id: Long): IO[Either[TodoNotFoundError.type, Unit]] = {
    sql"DELETE FROM todo WHERE id = $id".update.run.transact(transactor).map { affectedRows =>
      if (affectedRows == 1) {
        Right(())
      } else {
        Left(TodoNotFoundError)
      }
    }
  }

  def updateTodo(id: Long, todo: Todo): IO[Either[TodoNotFoundError.type, Todo]] = {
    sql"UPDATE todo SET description = ${todo.description}, importance = ${todo.importance} WHERE id = $id".update.run.transact(transactor).map { affectedRows =>
      if (affectedRows == 1) {
        Right(todo.copy(id = Some(id)))
      } else {
        Left(TodoNotFoundError)
      }
    }
  }
}
